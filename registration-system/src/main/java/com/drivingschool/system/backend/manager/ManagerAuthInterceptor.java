package com.drivingschool.system.backend.manager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ManagerAuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_MANAGER_ID = "managerId";

    /** Role name string, e.g. ADMIN — kept in session with login for reliable sidebar rendering. */
    public static final String SESSION_MANAGER_ROLE = "managerRole";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (ManagerSessionSupport.managerIdFromSession(request.getSession().getAttribute(SESSION_MANAGER_ID))
                == null) {
            response.sendRedirect(request.getContextPath() + "/login/professional");
            return false;
        }
        return true;
    }
}
