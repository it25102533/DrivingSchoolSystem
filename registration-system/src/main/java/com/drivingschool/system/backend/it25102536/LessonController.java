package com.drivingschool.system.backend.it25102536;

import com.drivingschool.system.backend.it25102533.StudentRepository;
import com.drivingschool.system.backend.it25102534.InstructorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LessonController {

    private static final String T = "frontend/it25102536/";

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final LessonRepository lessonRepository;

    public LessonController(
            StudentRepository studentRepository,
            InstructorRepository instructorRepository,
            LessonRepository lessonRepository) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.lessonRepository = lessonRepository;
    }

    @GetMapping("/bookLesson")
    public String showBookingForm(Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("allStudents", studentRepository.findAll());
        model.addAttribute("allInstructors", instructorRepository.findAll());
        return T + "book-lesson";
    }

    @PostMapping("/saveLesson")
    public String saveLesson(Lesson lesson) {
        lesson.setStatus("Scheduled");
        lessonRepository.save(lesson);
        return "redirect:/lessons";
    }

    @GetMapping("/lessons")
    @Transactional(readOnly = true)
    public String viewLessons(Model model) {
        model.addAttribute("allLessons", lessonRepository.findAll());
        return T + "lesson-list";
    }

    @GetMapping("/my-progress")
    public String showMyProgress(HttpSession session) {
        if (session.getAttribute("loggedInStudent") == null) {
            return "redirect:/login";
        }
        return "redirect:/student/dashboard";
    }
}
