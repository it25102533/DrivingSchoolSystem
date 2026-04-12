package com.drivingschool.system.backend.it25102543;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<ProgressNote, Long> {

    List<ProgressNote> findByStudent_Id(Long studentId);

    List<ProgressNote> findByStudent_IdOrderByDateDesc(Long studentId);

    void deleteByStudent_Id(Long studentId);
}
