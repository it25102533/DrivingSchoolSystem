package com.drivingschool.system.backend.it25102537;

import com.drivingschool.system.backend.it25102533.Student;
import com.drivingschool.system.backend.it25102533.StudentRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private static final String T = "frontend/it25102537/";

    private final PackageRepository packageRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(
            PackageRepository packageRepository,
            StudentRepository studentRepository,
            PaymentRepository paymentRepository) {
        this.packageRepository = packageRepository;
        this.studentRepository = studentRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/packages")
    public String viewPackages(Model model) {
        model.addAttribute("packages", packageRepository.findAll());
        return T + "view-packages";
    }

    @PostMapping("/purchase")
    public String processPayment(@RequestParam Long studentId, @RequestParam Long packageId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        LessonPackage pkg = packageRepository.findById(packageId).orElseThrow();
        double discount = student.calculateDiscount(pkg.getBasePrice());
        double amountPaid = pkg.getBasePrice() - discount;

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setLessonPackage(pkg);
        payment.setAmountPaid(amountPaid);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        return "redirect:/payment-history?success";
    }

    @GetMapping("/payment-history")
    public String paymentHistory(Model model) {
        model.addAttribute("payments", paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate")));
        return T + "payment-history";
    }
}
