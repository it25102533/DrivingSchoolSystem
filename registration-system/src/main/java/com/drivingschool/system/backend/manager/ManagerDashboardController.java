package com.drivingschool.system.backend.manager;

import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.backend.it25102534.InstructorRepository;
import com.drivingschool.system.backend.it25102535.VehicleRepository;
import com.drivingschool.system.backend.it25102536.LessonRepository;
import com.drivingschool.system.backend.it25102537.PaymentRepository;
import com.drivingschool.system.backend.it25102543.CourseFeedbackRepository;
import com.drivingschool.system.backend.it25102543.InstructorFeedbackRepository;
import com.drivingschool.system.backend.it25102543.ProgressRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ManagerDashboardController {

    private final ManagerRepository managerRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final VehicleRepository vehicleRepository;
    private final LessonRepository lessonRepository;
    private final PaymentRepository paymentRepository;
    private final ProgressRepository progressRepository;
    private final CourseFeedbackRepository courseFeedbackRepository;
    private final InstructorFeedbackRepository instructorFeedbackRepository;

    public ManagerDashboardController(
            ManagerRepository managerRepository,
            StudentRepository studentRepository,
            InstructorRepository instructorRepository,
            VehicleRepository vehicleRepository,
            LessonRepository lessonRepository,
            PaymentRepository paymentRepository,
            ProgressRepository progressRepository,
            CourseFeedbackRepository courseFeedbackRepository,
            InstructorFeedbackRepository instructorFeedbackRepository) {
        this.managerRepository = managerRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.vehicleRepository = vehicleRepository;
        this.lessonRepository = lessonRepository;
        this.paymentRepository = paymentRepository;
        this.progressRepository = progressRepository;
        this.courseFeedbackRepository = courseFeedbackRepository;
        this.instructorFeedbackRepository = instructorFeedbackRepository;
    }

    @GetMapping("/manager/dashboard")
    public String dashboard(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String forbidden) {
        Long id = ManagerSessionSupport.managerIdFromSession(
                session.getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID));
        if (id == null) {
            return "redirect:/login/professional";
        }
        Manager manager = managerRepository.findById(id).orElseThrow();
        ManagerRoleResolver.ensureRolePersisted(manager, managerRepository);
        if (manager.getRole() == ManagerRole.FLEET_MANAGER) {
            return "redirect:/manager/fleet/dashboard";
        }
        model.addAttribute("manager", manager);
        if (forbidden != null) {
            model.addAttribute("accessDenied", true);
        }

        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("instructorCount", instructorRepository.count());
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("lessonCount", lessonRepository.count());

        if (manager.getRole() == ManagerRole.ADMIN) {
            model.addAttribute("paymentCount", paymentRepository.count());
            model.addAttribute("courseFeedbackCount", courseFeedbackRepository.count());
            model.addAttribute("instructorFeedbackCount", instructorFeedbackRepository.count());
        }
        if (manager.getRole() == ManagerRole.OPERATIONS_LEAD) {
            model.addAttribute("progressNoteCount", progressRepository.count());
        }

        return "manager/dashboard";
    }

    @GetMapping("/manager/fleet/dashboard")
    public String fleetDashboard(HttpSession session, Model model) {
        Long id = ManagerSessionSupport.managerIdFromSession(
                session.getAttribute(ManagerAuthInterceptor.SESSION_MANAGER_ID));
        if (id == null) {
            return "redirect:/login/professional";
        }
        Manager manager = managerRepository.findById(id).orElseThrow();
        ManagerRoleResolver.ensureRolePersisted(manager, managerRepository);
        if (manager.getRole() != ManagerRole.FLEET_MANAGER) {
            return "redirect:/manager/dashboard";
        }

        model.addAttribute("manager", manager);
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("lessonCount", lessonRepository.count());

        return "manager/fleet-dashboard";
    }
}
