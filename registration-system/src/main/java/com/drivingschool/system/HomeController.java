package com.drivingschool.system;

import com.drivingschool.system.backend.it25102533.Student;
import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.student.StudentAuthInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public HomeController(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "landing/home";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String phone,
            @RequestParam int age,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String licenseType,
            @RequestParam(required = false) String studentType,
            HttpSession session,
            Model model) {
        String em = email.trim().toLowerCase();
        if (!StringUtils.hasText(em)) {
            model.addAttribute("error", "Email is required.");
            return "student/signup";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "student/signup";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "student/signup";
        }
        if (studentRepository.existsByEmailIgnoreCase(em)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "student/signup";
        }

        Student s = new Student();
        s.setName(name.trim());
        s.setEmail(em);
        s.setPasswordHash(passwordEncoder.encode(password));
        s.setPhone(StringUtils.hasText(phone) ? phone.trim() : null);
        s.setAge(age);
        s.setAddress(StringUtils.hasText(address) ? address.trim() : null);
        s.setLicenseType(StringUtils.hasText(licenseType) ? licenseType.trim() : null);
        s.setStudentType(StringUtils.hasText(studentType) ? studentType.trim() : "Individual");
        studentRepository.save(s);

        Student managed = studentRepository.findById(s.getId()).orElseThrow();
        session.setAttribute(StudentAuthInterceptor.SESSION_STUDENT, managed);
        return "redirect:/student/dashboard";
    }

    @GetMapping("/about")
    public String about() {
        return "landing/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "landing/contact";
    }

    /** Two-option page: professional vs student (use header Login dropdown, or open this URL). */
    @GetMapping("/login/options")
    public String loginHub() {
        return "landing/login-hub";
    }
}
