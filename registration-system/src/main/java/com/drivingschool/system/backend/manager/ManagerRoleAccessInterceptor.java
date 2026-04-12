package com.drivingschool.system.backend.manager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * When a professional manager is signed in, restricts URLs to the features allowed for their {@link ManagerRole}.
 * Unsigned users and student sessions are unaffected.
 */
@Component
public class ManagerRoleAccessInterceptor implements HandlerInterceptor {

    private final ManagerRepository managerRepository;

    public ManagerRoleAccessInterceptor(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Long managerId = ManagerSessionSupport.managerIdFromSession(
                request.getSession().getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID));
        if (managerId == null) {
            return true;
        }

        Manager manager = managerRepository.findById(managerId).orElse(null);
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/login/professional");
            return false;
        }
        ManagerRoleResolver.ensureRolePersisted(manager, managerRepository);

        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        String method = request.getMethod();

        if ("/manager/dashboard".equals(path) || "/manager/logout".equals(path)) {
            return true;
        }

        if (path.startsWith("/manager/feedback")) {
            if (manager.getRole() != ManagerRole.ADMIN) {
                response.sendRedirect(request.getContextPath() + "/manager/dashboard?forbidden");
                return false;
            }
            return true;
        }

        if (path.startsWith("/manager/")) {
            return true;
        }

        if (!isStaffFeaturePath(path)) {
            return true;
        }

        boolean allowed = switch (manager.getRole()) {
            case ADMIN -> true;
            case FLEET_MANAGER -> fleetAllowed(path, method);
            case OPERATIONS_LEAD -> operationsAllowed(path, method);
        };

        if (!allowed) {
            response.sendRedirect(request.getContextPath() + "/manager/dashboard?forbidden");
            return false;
        }
        return true;
    }

    private static boolean isStaffFeaturePath(String path) {
        if (path.startsWith("/studentReport/")) {
            return true;
        }
        return switch (path) {
            case "/students", "/register", "/saveStudent", "/deleteStudent", "/editStudent", "/updateStudent",
                    "/instructors", "/addInstructor", "/saveInstructor",
                    "/lessons", "/bookLesson", "/saveLesson",
                    "/packages", "/purchase", "/payment-history",
                    "/vehicles", "/addVehicle", "/saveVehicle",
                    "/addProgress", "/saveProgress" -> true;
            default -> false;
        };
    }

    private static boolean fleetAllowed(String path, String method) {
        if ("/vehicles".equals(path) || "/addVehicle".equals(path) || "/saveVehicle".equals(path)) {
            return true;
        }
        return "/lessons".equals(path) && "GET".equals(method);
    }

    private static boolean operationsAllowed(String path, String method) {
        if ("/lessons".equals(path) || "/bookLesson".equals(path) || "/saveLesson".equals(path)) {
            return true;
        }
        if ("/instructors".equals(path) || "/addInstructor".equals(path) || "/saveInstructor".equals(path)) {
            return true;
        }
        if ("/students".equals(path) && "GET".equals(method)) {
            return true;
        }
        if ("/vehicles".equals(path) && "GET".equals(method)) {
            return true;
        }
        if ("/addProgress".equals(path) || "/saveProgress".equals(path)) {
            return true;
        }
        return path.startsWith("/studentReport/");
    }
}
