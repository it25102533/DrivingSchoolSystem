package com.drivingschool.system.backend.it25102537;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStudent_Id(Long studentId);

    void deleteByStudent_Id(Long studentId);

    long countByLessonPackage_Id(Long packageId);
}
