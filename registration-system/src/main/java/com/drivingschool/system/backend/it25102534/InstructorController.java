package com.drivingschool.system.backend.it25102534;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InstructorController {

    private static final String T = "frontend/it25102534/";

    private final InstructorRepository instructorRepository;

    public InstructorController(InstructorRepository instructorRepository) {
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/addInstructor")
    public String showInstructorForm(Model model) {
        model.addAttribute("instructor", new Instructor());
        return T + "add-instructor";
    }

    @PostMapping("/saveInstructor")
    public String saveInstructor(Instructor instructor) {
        instructorRepository.save(instructor);
        return "redirect:/instructors";
    }

    @GetMapping("/instructors")
    public String viewInstructors(Model model) {
        model.addAttribute("allInstructors", instructorRepository.findAll());
        return T + "instructor-list";
    }
}
