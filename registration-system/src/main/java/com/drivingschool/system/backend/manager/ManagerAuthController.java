package com.drivingschool.system.backend.manager;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class ManagerAuthController {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerAuthController(ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login/professional")
    public String showProfessionalLogin() {
        return "landing/professional-login";
    }

    @PostMapping("/login/professional")
    public String login(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        String login = StringUtils.hasText(email) ? email.trim() : (username != null ? username.trim() : "");
        if (!StringUtils.hasText(login)) {
            model.addAttribute("error", "Please enter your email and password.");
            return "landing/professional-login";
        }
        var found = managerRepository.findByEmailIgnoreCase(login);
        if (found.isEmpty()) {
            found = managerRepository.findByUsername(login);
        }
        if (found.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            return "landing/professional-login";
        }
        boolean matches;
        try {
            matches = passwordEncoder.matches(password, found.get().getPasswordHash());
        } catch (IllegalArgumentException ex) {
            matches = false;
        }
        if (!matches) {
            model.addAttribute("error", "Invalid email or password.");
            return "landing/professional-login";
        }
        Manager mgr = found.get();
        ManagerRoleResolver.ensureRolePersisted(mgr, managerRepository);
        session.setAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID, mgr.getId());
        session.setAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ROLE, mgr.getRole().name());
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/manager/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID);
        session.removeAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ROLE);
        return "redirect:/login/professional?loggedOut";
    }
}
