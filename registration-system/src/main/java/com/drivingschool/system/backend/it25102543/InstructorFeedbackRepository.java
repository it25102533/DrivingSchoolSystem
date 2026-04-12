package com.drivingschool.system.backend.it25102543;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstructorFeedbackRepository extends JpaRepository<InstructorFeedback, Long> {

    @Query("SELECT DISTINCT f FROM InstructorFeedback f JOIN FETCH f.instructor")
    List<InstructorFeedback> findAllWithInstructor();
}
