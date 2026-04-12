package com.drivingschool.system.backend.it25102536;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByStudent_IdOrderByLessonTimeAsc(Long studentId);

    boolean existsByInstructor_IdAndLessonTime(Long instructorId, LocalDateTime lessonTime);

    boolean existsByStudent_IdAndLessonTime(Long studentId, LocalDateTime lessonTime);

    boolean existsByInstructor_IdAndLessonTimeAndIdNot(Long instructorId, LocalDateTime lessonTime, Long id);

    boolean existsByStudent_IdAndLessonTimeAndIdNot(Long studentId, LocalDateTime lessonTime, Long id);

    boolean existsByInstructor_Id(Long instructorId);

    void deleteByStudent_Id(Long studentId);

    @Query(
            "SELECT DISTINCT l FROM Lesson l LEFT JOIN FETCH l.student LEFT JOIN FETCH l.instructor "
                    + "LEFT JOIN FETCH l.vehicle ORDER BY l.lessonTime ASC")
    List<Lesson> findAllForAdminSchedule();

    @Query(
            "SELECT l FROM Lesson l LEFT JOIN FETCH l.student LEFT JOIN FETCH l.instructor "
                    + "LEFT JOIN FETCH l.vehicle WHERE l.id = :id")
    Optional<Lesson> findByIdWithDetails(@Param("id") Long id);
}
