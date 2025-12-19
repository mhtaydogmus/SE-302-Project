package edu.ieu.se302.examscheduler.ui.controllers;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Exam;
import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleController {
    private final ObservableList<Student> students;
    private final ObservableList<Exam> exams;
    private final ObservableList<ExamSession> scheduleSessions;
    private final ListView<Student> studentList;
    private final VBox detailsPanel;
    private final Label detailsNameLabel;
    private final Label detailsIdLabel;
    private final Label detailsEmailLabel;
    private final ListView<String> detailsCourseList;
    private final TableView<ExamSession> detailsExamTable;
    private final Button editStudentButton;
    private final Button deleteStudentButton;
    private final Button closeDetailsButton;

    public ScheduleController(ObservableList<Student> students,
                              ObservableList<Exam> exams,
                              ObservableList<ExamSession> scheduleSessions,
                              ListView<Student> studentList,
                              VBox detailsPanel,
                              Label detailsNameLabel,
                              Label detailsIdLabel,
                              Label detailsEmailLabel,
                              ListView<String> detailsCourseList,
                              TableView<ExamSession> detailsExamTable,
                              Button editStudentButton,
                              Button deleteStudentButton,
                              Button closeDetailsButton) {
        this.students = students;
        this.exams = exams;
        this.scheduleSessions = scheduleSessions;
        this.studentList = studentList;
        this.detailsPanel = detailsPanel;
        this.detailsNameLabel = detailsNameLabel;
        this.detailsIdLabel = detailsIdLabel;
        this.detailsEmailLabel = detailsEmailLabel;
        this.detailsCourseList = detailsCourseList;
        this.detailsExamTable = detailsExamTable;
        this.editStudentButton = editStudentButton;
        this.deleteStudentButton = deleteStudentButton;
        this.closeDetailsButton = closeDetailsButton;

        configureStudentList();
        configureButtons();
        clearDetails();
    }

    private void configureStudentList() {
        studentList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentId() + " - " + item.getFullName());
                }
            }
        });

        studentList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateDetails(newSelection);
            } else {
                clearDetails();
            }
        });
    }

    private void configureButtons() {
        if (closeDetailsButton != null) {
            closeDetailsButton.setOnAction(e -> hideDetails());
        }
        if (deleteStudentButton != null) {
            deleteStudentButton.setOnAction(e -> deleteSelectedStudent());
        }
    }

    private void populateDetails(Student student) {
        detailsNameLabel.setText(student.getFullName());
        detailsIdLabel.setText(student.getStudentId());
        detailsEmailLabel.setText(student.getEmail());

        List<String> courseNames = student.getEnrolledCourses().stream()
                .map(this::formatCourseLabel)
                .collect(Collectors.toList());
        detailsCourseList.setItems(FXCollections.observableArrayList(courseNames));
        detailsExamTable.setItems(FXCollections.observableArrayList(findSessionsForStudent(student)));

        showDetails();
    }

    private void deleteSelectedStudent() {
        Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            students.remove(selectedStudent);
            studentList.getSelectionModel().clearSelection();
            clearDetails();
        }
    }

    private void clearDetails() {
        detailsNameLabel.setText("N/A");
        detailsIdLabel.setText("N/A");
        detailsEmailLabel.setText("N/A");
        detailsCourseList.setItems(FXCollections.observableArrayList());
        detailsExamTable.setItems(FXCollections.observableArrayList());
        hideDetails();
    }

    private void showDetails() {
        detailsPanel.setVisible(true);
        detailsPanel.setManaged(true);
    }

    private void hideDetails() {
        detailsPanel.setVisible(false);
        detailsPanel.setManaged(false);
    }

    private List<ExamSession> findSessionsForStudent(Student student) {
        if (student == null || scheduleSessions == null) {
            return FXCollections.observableArrayList();
        }
        Set<Course> enrolledCourses = new HashSet<>(student.getEnrolledCourses());
        if (enrolledCourses.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        Set<Exam> enrolledExams = new HashSet<>();
        if (exams != null) {
            for (Exam exam : exams) {
                if (exam != null && exam.getCourse() != null && enrolledCourses.contains(exam.getCourse())) {
                    enrolledExams.add(exam);
                }
            }
        }

        List<ExamSession> sessions = new ArrayList<>();
        for (ExamSession session : scheduleSessions) {
            if (session == null || session.getExam() == null) {
                continue;
            }
            Course sessionCourse = session.getExam().getCourse();
            if (enrolledExams.contains(session.getExam()) || (sessionCourse != null && enrolledCourses.contains(sessionCourse))) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    private String formatCourseLabel(Course course) {
        if (course == null) {
            return "N/A";
        }
        String code = course.getCourseCode();
        String name = course.getCourseName();
        if (code == null || code.isBlank()) {
            return name != null ? name : "N/A";
        }
        if (name == null || name.isBlank()) {
            return code;
        }
        return code + " - " + name;
    }
}
