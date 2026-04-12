package com.drivingschool.system.backend.it25102533;



import com.drivingschool.system.backend.manager.Manager;
import com.drivingschool.system.backend.manager.ManagerAuthInterceptor;
import com.drivingschool.system.backend.manager.ManagerRepository;
import com.drivingschool.system.backend.manager.ManagerRole;
import com.drivingschool.system.backend.manager.ManagerRoleResolver;
import com.drivingschool.system.student.StudentAuthInterceptor;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;



@Controller

public class StudentController {



    private static final String T = "frontend/it25102533/";



    private final StudentRepository studentRepository;

    private final PasswordEncoder passwordEncoder;

    private final ManagerRepository managerRepository;



    public StudentController(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            ManagerRepository managerRepository) {

        this.studentRepository = studentRepository;

        this.passwordEncoder = passwordEncoder;

        this.managerRepository = managerRepository;

    }



    @GetMapping("/register")

    public String showForm(Model model) {

        model.addAttribute("student", new Student());

        return T + "register";

    }



    @PostMapping("/saveStudent")

    public String saveStudent(Student student) {

        studentRepository.save(student);

        return "redirect:/register?success";

    }



    @GetMapping("/students")

    public String viewStudents(HttpSession session, Model model) {

        Object raw = session.getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID);
        if (raw != null) {
            Long mid = raw instanceof Number ? ((Number) raw).longValue() : Long.parseLong(raw.toString());
            Optional<Manager> om = managerRepository.findById(mid);
            if (om.isPresent()) {
                Manager m = om.get();
                ManagerRoleResolver.ensureRolePersisted(m, managerRepository);
                if (m.getRole() != ManagerRole.FLEET_MANAGER) {
                    return "redirect:/manager/admin/students";
                }
            }
        }

        model.addAttribute("allStudents", studentRepository.findAll());

        return T + "student-list";

    }



    @GetMapping("/deleteStudent")

    public String deleteStudent(@RequestParam Long id) {

        studentRepository.deleteById(id);

        return "redirect:/students";

    }



    @GetMapping("/editStudent")

    public String showEditForm(@RequestParam Long id, Model model) {

        Student student = studentRepository.findById(id).orElseThrow();

        model.addAttribute("student", student);

        return T + "edit-student";

    }



    @PostMapping("/updateStudent")

    public String updateStudent(Student student) {

        studentRepository.save(student);

        return "redirect:/students";

    }



    @GetMapping({"/login", "/login/student"})

    public String showLoginPage(@RequestParam(required = false) String required, Model model) {

        if (required != null) {

            model.addAttribute("requiredLogin", true);

        }

        return T + "login";

    }



    @PostMapping("/login")

    public String processLogin(

            @RequestParam(required = false) String email,

            @RequestParam(required = false) String password,

            @RequestParam(required = false) String name,

            @RequestParam(required = false) Long studentId,

            HttpSession session,

            Model model) {



        if (StringUtils.hasText(email) && password != null && !password.isEmpty()) {

            return studentRepository

                    .findByEmailIgnoreCase(email.trim())

                    .filter(s -> s.getPasswordHash() != null)

                    .filter(s -> {

                        try {

                            return passwordEncoder.matches(password, s.getPasswordHash());

                        } catch (IllegalArgumentException e) {

                            return false;

                        }

                    })

                    .map(s -> {

                        Student fresh = studentRepository.findById(s.getId()).orElseThrow();

                        session.setAttribute(StudentAuthInterceptor.SESSION_STUDENT, fresh);

                        return "redirect:/student/dashboard";

                    })

                    .orElseGet(() -> {

                        model.addAttribute("error", "Invalid email or password.");

                        return T + "login";

                    });

        }



        if (studentId != null && StringUtils.hasText(name)) {

            return studentRepository

                    .findById(studentId)

                    .filter(s -> s.getName().equalsIgnoreCase(name.trim()))

                    .map(s -> {

                        Student fresh = studentRepository.findById(s.getId()).orElseThrow();

                        session.setAttribute(StudentAuthInterceptor.SESSION_STUDENT, fresh);

                        return "redirect:/student/dashboard";

                    })

                    .orElseGet(() -> {

                        model.addAttribute("error", "Invalid name or student ID.");

                        return T + "login";

                    });

        }



        model.addAttribute("error", "Sign in with email and password, or use your name and student ID (legacy).");

        return T + "login";

    }



    @GetMapping("/logout")

    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";

    }

}


