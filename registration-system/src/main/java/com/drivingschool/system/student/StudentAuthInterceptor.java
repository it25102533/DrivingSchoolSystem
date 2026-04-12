package com.drivingschool.system.student;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class StudentAuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_STUDENT = "loggedInStudent";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (request.getSession().getAttribute(SESSION_STUDENT) == null) {
            response.sendRedirect(request.getContextPath() + "/login?required");
            return false;
        }
        return true;
    }
}
