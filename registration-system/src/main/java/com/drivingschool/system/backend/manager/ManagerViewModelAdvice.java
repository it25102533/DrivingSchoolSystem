package com.drivingschool.system.backend.manager;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes {@code managerRole} (String, e.g. ADMIN) for manager-area views so templates use one sidebar
 * without each controller repeating the attribute.
 */
@ControllerAdvice(
        assignableTypes = {
            ManagerDashboardController.class,
            ManagerAdminController.class,
            FeedbackModerationController.class
        })
public class ManagerViewModelAdvice {

    private final ManagerRepository managerRepository;

    public ManagerViewModelAdvice(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @ModelAttribute
    public void addManagerRole(HttpSession session, Model model) {
        Object rawId = session.getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID);
        if (rawId == null) {
            return;
        }
        long mid = rawId instanceof Number ? ((Number) rawId).longValue() : Long.parseLong(rawId.toString());
        managerRepository
                .findById(mid)
                .ifPresent(m -> {
                    ManagerRoleResolver.ensureRolePersisted(m, managerRepository);
                    model.addAttribute("managerRole", m.getRole().name());
                });
    }
}
