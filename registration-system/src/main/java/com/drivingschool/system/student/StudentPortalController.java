package com.drivingschool.system.student;

import com.drivingschool.system.backend.it25102533.Student;
import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.backend.it25102534.Instructor;
import com.drivingschool.system.backend.it25102534.InstructorRepository;
import com.drivingschool.system.backend.it25102535.VehicleRepository;
import com.drivingschool.system.backend.it25102536.Lesson;
import com.drivingschool.system.backend.it25102536.LessonRepository;
import com.drivingschool.system.backend.it25102537.LessonPackage;
import com.drivingschool.system.backend.it25102537.PackageRepository;
import com.drivingschool.system.backend.it25102537.Payment;
import com.drivingschool.system.backend.it25102537.PaymentRepository;
import com.drivingschool.system.backend.it25102543.CourseFeedback;
import com.drivingschool.system.backend.it25102543.CourseFeedbackRepository;
import com.drivingschool.system.backend.it25102543.InstructorFeedback;
import com.drivingschool.system.backend.it25102543.InstructorFeedbackRepository;
import com.drivingschool.system.backend.it25102543.ProgressNote;
import com.drivingschool.system.backend.it25102543.ProgressRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentPortalController {

    private static final String T = "student/";

    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final InstructorRepository instructorRepository;
    private final VehicleRepository vehicleRepository;
    private final PackageRepository packageRepository;
    private final PaymentRepository paymentRepository;
    private final ProgressRepository progressRepository;
    private final InstructorFeedbackRepository instructorFeedbackRepository;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentPortalController(
            StudentRepository studentRepository,
            LessonRepository lessonRepository,
            InstructorRepository instructorRepository,
            VehicleRepository vehicleRepository,
            PackageRepository packageRepository,
            PaymentRepository paymentRepository,
            ProgressRepository progressRepository,
            InstructorFeedbackRepository instructorFeedbackRepository,
            CourseFeedbackRepository courseFeedbackRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.lessonRepository = lessonRepository;
        this.instructorRepository = instructorRepository;
        this.vehicleRepository = vehicleRepository;
        this.packageRepository = packageRepository;
        this.paymentRepository = paymentRepository;
        this.progressRepository = progressRepository;
        this.instructorFeedbackRepository = instructorFeedbackRepository;
        this.courseFeedbackRepository = courseFeedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Student loadStudent(HttpSession session) {
        Student s = (Student) session.getAttribute(StudentAuthInterceptor.SESSION_STUDENT);
        return studentRepository.findById(s.getId()).orElseThrow();
    }

    @GetMapping("/student/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(HttpSession session, Model model) {
        Student student = loadStudent(session);
        model.addAttribute("student", student);
        List<Lesson> mine = lessonRepository.findByStudent_IdOrderByLessonTimeAsc(student.getId());
        long upcoming = mine.stream()
                .filter(l -> l.getLessonTime() != null
                        && !LocalDateTime.now().isAfter(l.getLessonTime())
                        && !"Cancelled".equals(l.getStatus()))
                .count();
        model.addAttribute("upcomingCount", upcoming);
        return T + "dashboard";
    }

    @GetMapping("/student/profile")
    @Transactional(readOnly = true)
    public String profileForm(HttpSession session, Model model) {
        model.addAttribute("student", loadStudent(session));
        return T + "profile";
    }

    @PostMapping("/student/profile")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam int age,
            @RequestParam(required = false) String licenseType,
            @RequestParam(required = false) String studentType,
            HttpSession session) {
        Student s = loadStudent(session);
        s.setName(name.trim());
        s.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
        s.setAddress(StringUtils.hasText(address) ? address.trim() : null);
        s.setAge(age);
        s.setLicenseType(StringUtils.hasText(licenseType) ? licenseType.trim() : null);
        s.setStudentType(StringUtils.hasText(studentType) ? studentType.trim() : null);
        studentRepository.save(s);
        session.setAttribute(StudentAuthInterceptor.SESSION_STUDENT, studentRepository.findById(s.getId()).orElseThrow());
        return "redirect:/student/profile?saved";
    }

    @PostMapping("/student/profile/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {
        Student s = loadStudent(session);
        if (s.getPasswordHash() == null) {
            model.addAttribute("error", "No password is set on this account. Use legacy login or contact your school.");
            model.addAttribute("student", s);
            return T + "profile";
        }
        if (!passwordEncoder.matches(currentPassword, s.getPasswordHash())) {
            model.addAttribute("error", "Current password is incorrect.");
            model.addAttribute("student", s);
            return T + "profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
            model.addAttribute("student", s);
            return T + "profile";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("student", s);
            return T + "profile";
        }
        s.setPasswordHash(passwordEncoder.encode(newPassword));
        studentRepository.save(s);
        session.setAttribute(StudentAuthInterceptor.SESSION_STUDENT, studentRepository.findById(s.getId()).orElseThrow());
        return "redirect:/student/profile?pwd";
    }

    @GetMapping("/student/book-lesson")
    @Transactional(readOnly = true)
    public String bookForm(Model model, HttpSession session) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("allInstructors", instructorRepository.findAll());
        model.addAttribute("allVehicles", vehicleRepository.findAll());
        model.addAttribute("student", loadStudent(session));
        model.addAttribute("minTime", LocalDateTime.now().toString().substring(0, 16));
        return T + "book-lesson";
    }

    @PostMapping("/student/book-lesson")
    @Transactional
    public String bookSubmit(
            @RequestParam Long instructorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime lessonTime,
            @RequestParam String vehicleType,
            @RequestParam(required = false) Long vehicleId,
            HttpSession session,
            Model model) {
        Student student = loadStudent(session);
        if (lessonTime.isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Choose a future date and time.");
            return bookForm(model, session);
        }
        if (lessonRepository.existsByInstructor_IdAndLessonTime(instructorId, lessonTime)) {
            model.addAttribute("error", "That instructor is already booked at this time. Pick another slot.");
            return bookForm(model, session);
        }
        if (lessonRepository.existsByStudent_IdAndLessonTime(student.getId(), lessonTime)) {
            model.addAttribute("error", "You already have a lesson at this time.");
            return bookForm(model, session);
        }
        Lesson lesson = new Lesson();
        lesson.setStudent(student);
        lesson.setInstructor(instructorRepository.findById(instructorId).orElseThrow());
        lesson.setLessonTime(lessonTime);
        lesson.setVehicleType(vehicleType);
        if (vehicleId != null) {
            vehicleRepository.findById(vehicleId).ifPresent(lesson::setVehicle);
        }
        lesson.setStatus("Scheduled");
        lessonRepository.save(lesson);
        return "redirect:/student/my-lessons?booked";
    }

    @GetMapping("/student/my-lessons")
    @Transactional(readOnly = true)
    public String myLessons(HttpSession session, Model model, @RequestParam(required = false) String error) {
        Student student = loadStudent(session);
        List<Lesson> all = lessonRepository.findByStudent_IdOrderByLessonTimeAsc(student.getId());
        model.addAttribute("lessons", all);
        model.addAttribute("student", student);
        model.addAttribute("now", LocalDateTime.now());
        if (error != null) {
            model.addAttribute("error", "You can only change your own lessons.");
        }
        return T + "my-lessons";
    }

    @GetMapping("/student/lesson/reschedule")
    @Transactional(readOnly = true)
    public String rescheduleForm(@RequestParam Long id, HttpSession session, Model model) {
        Student student = loadStudent(session);
        Lesson lesson = lessonRepository.findById(id).orElseThrow();
        if (!lesson.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/my-lessons?error";
        }
        if ("Cancelled".equals(lesson.getStatus())) {
            return "redirect:/student/my-lessons";
        }
        model.addAttribute("lesson", lesson);
        model.addAttribute("allInstructors", instructorRepository.findAll());
        model.addAttribute("minTime", LocalDateTime.now().toString().substring(0, 16));
        return T + "reschedule-lesson";
    }

    @PostMapping("/student/lesson/reschedule")
    @Transactional
    public String rescheduleSubmit(
            @RequestParam Long lessonId,
            @RequestParam Long instructorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime lessonTime,
            HttpSession session,
            Model model) {
        Student student = loadStudent(session);
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        if (!lesson.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/my-lessons?error";
        }
        if (lessonTime.isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Choose a future date and time.");
            model.addAttribute("lesson", lesson);
            model.addAttribute("allInstructors", instructorRepository.findAll());
            model.addAttribute("minTime", LocalDateTime.now().toString().substring(0, 16));
            return T + "reschedule-lesson";
        }
        if (lessonRepository.existsByInstructor_IdAndLessonTimeAndIdNot(instructorId, lessonTime, lessonId)) {
            model.addAttribute("error", "That instructor is already booked at this time.");
            model.addAttribute("lesson", lesson);
            model.addAttribute("allInstructors", instructorRepository.findAll());
            model.addAttribute("minTime", LocalDateTime.now().toString().substring(0, 16));
            return T + "reschedule-lesson";
        }
        lesson.setInstructor(instructorRepository.findById(instructorId).orElseThrow());
        lesson.setLessonTime(lessonTime);
        lessonRepository.save(lesson);
        return "redirect:/student/my-lessons?rescheduled";
    }

    @PostMapping("/student/lesson/cancel")
    @Transactional
    public String cancelLesson(@RequestParam Long id, HttpSession session) {
        Student student = loadStudent(session);
        Lesson lesson = lessonRepository.findById(id).orElseThrow();
        if (!lesson.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/my-lessons?error";
        }
        lesson.setStatus("Cancelled");
        lessonRepository.save(lesson);
        return "redirect:/student/my-lessons?cancelled";
    }

    @GetMapping("/student/packages")
    @Transactional(readOnly = true)
    public String packagesPage(HttpSession session, Model model) {
        model.addAttribute("packages", packageRepository.findAll());
        model.addAttribute("student", loadStudent(session));
        return T + "packages";
    }

    @PostMapping("/student/purchase")
    @Transactional
    public String purchase(@RequestParam Long packageId, HttpSession session) {
        Student student = loadStudent(session);
        LessonPackage pkg = packageRepository.findById(packageId).orElseThrow();
        double discount = student.calculateDiscount(pkg.getBasePrice());
        double amountPaid = pkg.getBasePrice() - discount;
        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setLessonPackage(pkg);
        payment.setAmountPaid(amountPaid);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        return "redirect:/student/payment-history?success";
    }

    @GetMapping("/student/payment-history")
    @Transactional(readOnly = true)
    public String paymentHistory(HttpSession session, Model model) {
        Student student = loadStudent(session);
        model.addAttribute("payments", paymentRepository.findByStudent_Id(student.getId()));
        model.addAttribute("student", student);
        return T + "payment-history";
    }

    @GetMapping("/student/progress")
    @Transactional(readOnly = true)
    public String progress(HttpSession session, Model model) {
        Student student = loadStudent(session);
        List<ProgressNote> notes = progressRepository.findByStudent_IdOrderByDateDesc(student.getId());
        model.addAttribute("notes", notes);
        model.addAttribute("student", student);
        return T + "progress";
    }

    @GetMapping("/student/feedback")
    @Transactional(readOnly = true)
    public String feedbackForm(HttpSession session, Model model) {
        model.addAttribute("student", loadStudent(session));
        model.addAttribute("instructors", instructorRepository.findAll());
        return T + "feedback";
    }

    @PostMapping("/student/feedback/instructor")
    @Transactional
    public String submitInstructorFeedback(
            @RequestParam Long instructorId,
            @RequestParam int rating,
            @RequestParam(required = false) String comments,
            HttpSession session) {
        Student student = loadStudent(session);
        InstructorFeedback fb = new InstructorFeedback();
        fb.setInstructor(instructorRepository.findById(instructorId).orElseThrow());
        fb.setRating(Math.min(5, Math.max(1, rating)));
        fb.setComments(StringUtils.hasText(comments) ? comments.trim() : null);
        instructorFeedbackRepository.save(fb);
        return "redirect:/student/feedback?sent=instructor";
    }

    @PostMapping("/student/feedback/course")
    @Transactional
    public String submitCourseFeedback(
            @RequestParam String courseName,
            @RequestParam int rating,
            @RequestParam(required = false) String comments,
            HttpSession session) {
        loadStudent(session);
        CourseFeedback fb = new CourseFeedback();
        fb.setCourseName(courseName.trim());
        fb.setRating(Math.min(5, Math.max(1, rating)));
        fb.setComments(StringUtils.hasText(comments) ? comments.trim() : null);
        courseFeedbackRepository.save(fb);
        return "redirect:/student/feedback?sent=course";
    }
}
