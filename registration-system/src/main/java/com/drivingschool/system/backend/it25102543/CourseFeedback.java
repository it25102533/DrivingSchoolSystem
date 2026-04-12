package com.drivingschool.system.backend.it25102543;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_feedback")
public class CourseFeedback extends Feedback {

    private String courseName;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
