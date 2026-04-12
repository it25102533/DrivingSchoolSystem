package com.drivingschool.system.backend.it25102543;

import com.drivingschool.system.backend.it25102533.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProgressController {

    private static final String T = "frontend/it25102543/";

    private final StudentRepository studentRepository;
    private final ProgressRepository progressRepository;

    public ProgressController(StudentRepository studentRepository, ProgressRepository progressRepository) {
        this.studentRepository = studentRepository;
        this.progressRepository = progressRepository;
    }

    @GetMapping("/addProgress")
    public String showAddForm(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("progressNote", new ProgressNote());
        return T + "add-progress";
    }

    @PostMapping("/saveProgress")
    public String saveProgress(ProgressNote progressNote) {
        progressNote.setDate(java.time.LocalDateTime.now());
        progressRepository.save(progressNote);
        return "redirect:/students";
    }

    @GetMapping("/studentReport/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        model.addAttribute("notes", progressRepository.findByStudent_Id(id));
        model.addAttribute("student", studentRepository.findById(id).orElseThrow());
        return T + "student-report";
    }
}
