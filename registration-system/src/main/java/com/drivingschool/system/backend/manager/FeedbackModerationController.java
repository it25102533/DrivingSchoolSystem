package com.drivingschool.system.backend.manager;

import com.drivingschool.system.backend.it25102543.CourseFeedbackRepository;
import com.drivingschool.system.backend.it25102543.InstructorFeedbackRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/feedback")
public class FeedbackModerationController {

    private final CourseFeedbackRepository courseFeedbackRepository;
    private final InstructorFeedbackRepository instructorFeedbackRepository;

    public FeedbackModerationController(
            CourseFeedbackRepository courseFeedbackRepository,
            InstructorFeedbackRepository instructorFeedbackRepository) {
        this.courseFeedbackRepository = courseFeedbackRepository;
        this.instructorFeedbackRepository = instructorFeedbackRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("courseFeedback", courseFeedbackRepository.findAll());
        model.addAttribute("instructorFeedback", instructorFeedbackRepository.findAllWithInstructor());
        model.addAttribute("isAdmin", true);
        return "manager/feedback-moderation";
    }

    @PostMapping("/course/{id}/delete")
    public String deleteCourse(@PathVariable Long id) {
        courseFeedbackRepository.deleteById(id);
        return "redirect:/manager/feedback?removed=course";
    }

    @PostMapping("/instructor/{id}/delete")
    public String deleteInstructor(@PathVariable Long id) {
        instructorFeedbackRepository.deleteById(id);
        return "redirect:/manager/feedback?removed=instructor";
    }
}
