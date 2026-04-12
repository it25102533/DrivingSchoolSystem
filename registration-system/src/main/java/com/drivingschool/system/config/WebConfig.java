package com.drivingschool.system.config;

import com.drivingschool.system.backend.it25102533.Student;
import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.backend.it25102534.Instructor;
import com.drivingschool.system.backend.it25102534.InstructorRepository;
import com.drivingschool.system.backend.manager.ManagerAuthInterceptor;
import com.drivingschool.system.backend.manager.ManagerRoleAccessInterceptor;
import com.drivingschool.system.student.StudentAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final ManagerAuthInterceptor managerAuthInterceptor;
    private final ManagerRoleAccessInterceptor managerRoleAccessInterceptor;
    private final StudentAuthInterceptor studentAuthInterceptor;

    public WebConfig(
            StudentRepository studentRepository,
            InstructorRepository instructorRepository,
            ManagerAuthInterceptor managerAuthInterceptor,
            ManagerRoleAccessInterceptor managerRoleAccessInterceptor,
            StudentAuthInterceptor studentAuthInterceptor) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.managerAuthInterceptor = managerAuthInterceptor;
        this.managerRoleAccessInterceptor = managerRoleAccessInterceptor;
        this.studentAuthInterceptor = studentAuthInterceptor;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Student.class,
                id -> studentRepository.findById(Long.parseLong(id)).orElse(null));
        registry.addConverter(String.class, Instructor.class,
                id -> instructorRepository.findById(Long.parseLong(id)).orElse(null));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(managerRoleAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/h2-console",
                        "/h2-console/**",
                        "/error",
                        "/login",
                        "/login/**",
                        "/logout",
                        "/signup")
                .order(Ordered.HIGHEST_PRECEDENCE);
        registry.addInterceptor(managerAuthInterceptor)
                .addPathPatterns("/manager/**")
                .excludePathPatterns("/manager/logout")
                .order(Ordered.HIGHEST_PRECEDENCE + 1);
        registry.addInterceptor(studentAuthInterceptor)
                .addPathPatterns("/student/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 2);
    }
}
