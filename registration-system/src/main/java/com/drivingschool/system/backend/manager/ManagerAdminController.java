package com.drivingschool.system.backend.manager;

import com.drivingschool.system.backend.it25102533.Student;
import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.backend.it25102534.Instructor;
import com.drivingschool.system.backend.it25102534.InstructorRepository;
import com.drivingschool.system.backend.it25102535.Vehicle;
import com.drivingschool.system.backend.it25102535.VehicleRepository;
import com.drivingschool.system.backend.it25102536.Lesson;
import com.drivingschool.system.backend.it25102536.LessonRepository;
import com.drivingschool.system.backend.it25102537.LessonPackage;
import com.drivingschool.system.backend.it25102537.PackageRepository;
import com.drivingschool.system.backend.it25102537.Payment;
import com.drivingschool.system.backend.it25102537.PaymentRepository;
import com.drivingschool.system.backend.it25102543.ProgressRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Unified admin / operations portal under {@code /manager/admin}. Role checks: {@link ManagerRole#FLEET_MANAGER}
 * cannot access; {@link ManagerRole#ADMIN} has full access; {@link ManagerRole#OPERATIONS_LEAD} can manage
 * student directory (without delete), instructors, and lessons — not packages, payments, or system reports.
 * New students are not created here (public sign-up only).
 */
@Controller
@RequestMapping("/manager/admin")
public class ManagerAdminController {

    private final ManagerRepository managerRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final VehicleRepository vehicleRepository;
    private final LessonRepository lessonRepository;
    private final PackageRepository packageRepository;
    private final PaymentRepository paymentRepository;
    private final ProgressRepository progressRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerAdminController(
            ManagerRepository managerRepository,
            StudentRepository studentRepository,
            InstructorRepository instructorRepository,
            VehicleRepository vehicleRepository,
            LessonRepository lessonRepository,
            PackageRepository packageRepository,
            PaymentRepository paymentRepository,
            ProgressRepository progressRepository,
            PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.vehicleRepository = vehicleRepository;
        this.lessonRepository = lessonRepository;
        this.packageRepository = packageRepository;
        this.paymentRepository = paymentRepository;
        this.progressRepository = progressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Optional<Manager> currentManager(HttpSession session) {
        Long id = ManagerSessionSupport.managerIdFromSession(
                session.getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID));
        if (id == null) {
            return Optional.empty();
        }
        return managerRepository.findById(id);
    }

    private String redirectLogin() {
        return "redirect:/login/professional";
    }

    private String redirectForbidden() {
        return "redirect:/manager/dashboard?forbidden";
    }

    /** Fleet cannot use admin tools; others pass. */
    private String requireStaffNotFleet(HttpSession session) {
        Optional<Manager> m = currentManager(session);
        if (m.isEmpty()) {
            return redirectLogin();
        }
        ManagerRoleResolver.ensureRolePersisted(m.get(), managerRepository);
        if (m.get().getRole() == ManagerRole.FLEET_MANAGER) {
            return redirectForbidden();
        }
        return null;
    }

    private boolean isAdmin(Manager m) {
        return m.getRole() == ManagerRole.ADMIN;
    }

    private void putIsAdmin(HttpSession session, Model model) {
        currentManager(session).ifPresent(m -> model.addAttribute("isAdmin", isAdmin(m)));
    }

    private String requireAdmin(HttpSession session) {
        Optional<Manager> m = currentManager(session);
        if (m.isEmpty()) {
            return redirectLogin();
        }
        ManagerRoleResolver.ensureRolePersisted(m.get(), managerRepository);
        if (!isAdmin(m.get())) {
            return redirectForbidden();
        }
        return null;
    }

    // --- Students ---

    @GetMapping("/students")
    @Transactional(readOnly = true)
    public String students(HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        putIsAdmin(session, model);
        model.addAttribute("students", studentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        return "manager/admin/students";
    }

    @GetMapping("/students/new")
    public String newStudentForm(HttpSession session, RedirectAttributes ra) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        ra.addFlashAttribute(
                "error", "New accounts are created by students via sign-up only. You can view and edit existing learners in the directory.");
        return "redirect:/manager/admin/students";
    }

    @GetMapping("/students/{id}/edit")
    @Transactional(readOnly = true)
    public String editStudent(@PathVariable Long id, HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        Student student = studentRepository.findById(id).orElseThrow();
        model.addAttribute("student", student);
        putIsAdmin(session, model);
        return "manager/admin/student-form";
    }

    @PostMapping("/students/save")
    public String saveStudent(
            HttpSession session,
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam int age,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String licenseType,
            @RequestParam(required = false) String studentType,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes ra) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        String em = StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
        if (id == null) {
            ra.addFlashAttribute(
                    "error", "Creating new student accounts from the dashboard is disabled. Students register via the public sign-up page.");
            return "redirect:/manager/admin/students";
        }
        Student s = studentRepository.findById(id).orElseThrow();
        if (StringUtils.hasText(em) && !em.equals(s.getEmail())) {
            if (studentRepository.existsByEmailIgnoreCase(em)) {
                ra.addFlashAttribute("error", "That email is already in use.");
                return "redirect:/manager/admin/students/" + id + "/edit";
            }
        }
        s.setName(name.trim());
        s.setEmail(em);
        s.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
        s.setAge(age);
        s.setAddress(StringUtils.hasText(address) ? address.trim() : null);
        s.setLicenseType(StringUtils.hasText(licenseType) ? licenseType.trim() : null);
        s.setStudentType(StringUtils.hasText(studentType) ? studentType.trim() : "Individual");
        if (StringUtils.hasText(newPassword)) {
            if (newPassword.length() < 6) {
                ra.addFlashAttribute("error", "Password must be at least 6 characters.");
                return "redirect:/manager/admin/students/" + id + "/edit";
            }
            s.setPasswordHash(passwordEncoder.encode(newPassword));
        }
        studentRepository.save(s);
        ra.addFlashAttribute("msg", "Student updated.");
        return "redirect:/manager/admin/students";
    }

    @PostMapping("/students/{id}/delete")
    @Transactional
    public String deleteStudent(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        if (!studentRepository.existsById(id)) {
            return "redirect:/manager/admin/students";
        }
        paymentRepository.deleteByStudent_Id(id);
        lessonRepository.deleteByStudent_Id(id);
        progressRepository.deleteByStudent_Id(id);
        studentRepository.deleteById(id);
        ra.addFlashAttribute("msg", "Student removed.");
        return "redirect:/manager/admin/students";
    }

    // --- Instructors ---

    @GetMapping("/instructors")
    @Transactional(readOnly = true)
    public String instructors(HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("instructors", instructorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        putIsAdmin(session, model);
        return "manager/admin/instructors";
    }

    @GetMapping("/instructors/new")
    public String newInstructor(HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("isNew", true);
        putIsAdmin(session, model);
        return "manager/admin/instructor-form";
    }

    @GetMapping("/instructors/{id}/edit")
    public String editInstructor(@PathVariable Long id, HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("instructor", instructorRepository.findById(id).orElseThrow());
        model.addAttribute("isNew", false);
        putIsAdmin(session, model);
        return "manager/admin/instructor-form";
    }

    @PostMapping("/instructors/save")
    public String saveInstructor(
            HttpSession session,
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) String specialization) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        Instructor ins = id == null ? new Instructor() : instructorRepository.findById(id).orElseThrow();
        ins.setName(name.trim());
        ins.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
        ins.setLicenseNumber(StringUtils.hasText(licenseNumber) ? licenseNumber.trim() : null);
        ins.setSpecialization(StringUtils.hasText(specialization) ? specialization.trim() : null);
        instructorRepository.save(ins);
        return "redirect:/manager/admin/instructors";
    }

    @PostMapping("/instructors/{id}/delete")
    public String deleteInstructor(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        if (lessonRepository.existsByInstructor_Id(id)) {
            ra.addFlashAttribute(
                    "error", "Cannot remove this instructor while lessons are assigned. Cancel or reassign lessons first.");
            return "redirect:/manager/admin/instructors";
        }
        instructorRepository.deleteById(id);
        ra.addFlashAttribute("msg", "Instructor removed.");
        return "redirect:/manager/admin/instructors";
    }

    // --- Lessons ---

    @GetMapping("/lessons")
    @Transactional(readOnly = true)
    public String lessons(HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("lessons", lessonRepository.findAllForAdminSchedule());
        putIsAdmin(session, model);
        return "manager/admin/lessons";
    }

    @GetMapping("/lessons/{id}/edit")
    @Transactional(readOnly = true)
    public String editLesson(@PathVariable Long id, HttpSession session, Model model) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        Lesson lesson = lessonRepository.findByIdWithDetails(id).orElseThrow();
        model.addAttribute("lesson", lesson);
        model.addAttribute("students", studentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("instructors", instructorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("vehicles", vehicleRepository.findAll());
        putIsAdmin(session, model);
        return "manager/admin/lesson-edit";
    }

    @PostMapping("/lessons/save")
    @Transactional
    public String saveLesson(
            HttpSession session,
            @RequestParam Long id,
            @RequestParam Long studentId,
            @RequestParam Long instructorId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime lessonTime,
            @RequestParam String status,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) Long vehicleId,
            RedirectAttributes ra) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        Lesson lesson = lessonRepository.findByIdWithDetails(id).orElseThrow();
        Student st = studentRepository.findById(studentId).orElseThrow();
        Instructor ins = instructorRepository.findById(instructorId).orElseThrow();
        if (lessonRepository.existsByInstructor_IdAndLessonTimeAndIdNot(instructorId, lessonTime, id)) {
            ra.addFlashAttribute("error", "That instructor already has another lesson at this time.");
            return "redirect:/manager/admin/lessons/" + id + "/edit";
        }
        if (lessonRepository.existsByStudent_IdAndLessonTimeAndIdNot(studentId, lessonTime, id)) {
            ra.addFlashAttribute("error", "That student already has another lesson at this time.");
            return "redirect:/manager/admin/lessons/" + id + "/edit";
        }
        lesson.setStudent(st);
        lesson.setInstructor(ins);
        lesson.setLessonTime(lessonTime);
        lesson.setStatus(status);
        lesson.setVehicleType(StringUtils.hasText(vehicleType) ? vehicleType.trim() : null);
        if (vehicleId != null) {
            Vehicle v = vehicleRepository.findById(vehicleId).orElse(null);
            lesson.setVehicle(v);
        } else {
            lesson.setVehicle(null);
        }
        lessonRepository.save(lesson);
        ra.addFlashAttribute("msg", "Lesson updated.");
        return "redirect:/manager/admin/lessons";
    }

    @PostMapping("/lessons/{id}/cancel")
    @Transactional
    public String cancelLesson(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = requireStaffNotFleet(session);
        if (r != null) {
            return r;
        }
        Lesson lesson = lessonRepository.findById(id).orElseThrow();
        lesson.setStatus("Cancelled");
        lessonRepository.save(lesson);
        ra.addFlashAttribute("msg", "Lesson cancelled.");
        return "redirect:/manager/admin/lessons";
    }

    // --- Packages (ADMIN) ---

    @GetMapping("/packages")
    @Transactional(readOnly = true)
    public String packages(HttpSession session, Model model) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("packages", packageRepository.findAll(Sort.by(Sort.Direction.ASC, "packageName")));
        model.addAttribute("isAdmin", true);
        return "manager/admin/packages";
    }

    @PostMapping("/packages/save")
    public String savePackage(
            HttpSession session,
            @RequestParam(required = false) Long id,
            @RequestParam String packageName,
            @RequestParam int numberOfLessons,
            @RequestParam double basePrice,
            RedirectAttributes ra) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        LessonPackage pkg = id == null ? new LessonPackage() : packageRepository.findById(id).orElseThrow();
        pkg.setPackageName(packageName.trim());
        pkg.setNumberOfLessons(numberOfLessons);
        pkg.setBasePrice(basePrice);
        packageRepository.save(pkg);
        ra.addFlashAttribute("msg", "Package saved.");
        return "redirect:/manager/admin/packages";
    }

    @PostMapping("/packages/{id}/delete")
    public String deletePackage(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        long n = paymentRepository.countByLessonPackage_Id(id);
        if (n > 0) {
            ra.addFlashAttribute(
                    "error", "Cannot delete a package that has payment records. Retire it by renaming instead.");
            return "redirect:/manager/admin/packages";
        }
        packageRepository.deleteById(id);
        ra.addFlashAttribute("msg", "Package removed.");
        return "redirect:/manager/admin/packages";
    }

    // --- Payments (ADMIN) ---

    @GetMapping("/payments")
    @Transactional(readOnly = true)
    public String payments(HttpSession session, Model model) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        List<Payment> payments = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate"));
        model.addAttribute("payments", payments);
        model.addAttribute("isAdmin", true);
        return "manager/admin/payments";
    }

    // --- Reports (ADMIN) ---

    @GetMapping("/reports")
    @Transactional(readOnly = true)
    public String reports(HttpSession session, Model model) {
        String r = requireAdmin(session);
        if (r != null) {
            return r;
        }
        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("instructorCount", instructorRepository.count());
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("lessonCount", lessonRepository.count());
        model.addAttribute("paymentCount", paymentRepository.count());
        model.addAttribute("managerCount", managerRepository.count());
        model.addAttribute("students", studentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("managers", managerRepository.findAll());
        model.addAttribute("isAdmin", true);
        return "manager/admin/reports";
    }
}
