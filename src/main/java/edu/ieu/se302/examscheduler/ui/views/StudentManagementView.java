package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Course;
import com.examscheduler.entity.Enrollment;
import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Student;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class StudentManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Enrollment> enrollments;
    private final ListView<Student> studentList = new ListView<>(students);
    private VBox detailsPanel;
    private Label studentNameLabel;
    private Label studentIdLabel;
    private Label studentEmailLabel;
    private ListView<Course> enrolledCoursesList;
    private TableView<ExamSession> upcomingExamsTable;


    public StudentManagementView(ObservableList<Enrollment> enrollments) {
        this.enrollments = enrollments;
        // Sample Data
        students.add(new Student("101", "Ali", "Veli", "ali@mail.com", "Male"));
        students.add(new Student("102", "Ayşe", "Fatma", "ayse@mail.com", "Female"));

        root.setPadding(new Insets(10));

        Label title = new Label("Student Management");
        title.getStyleClass().add("view-title");

        studentList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentId() + " - " + item.getFirstName() + " " + item.getLastName() + " (" + item.getEmail() + ")");
                }
            }
        });

        studentList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showStudentDetails(newSelection);
            } else {
                hideStudentDetails();
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showStudentDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                showStudentDialog(selectedStudent);
            } else {
                info("No student selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Student");
                alert.setHeaderText("Are you sure you want to delete " + selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    students.remove(selectedStudent);
                    removeEnrollmentsForStudent(selectedStudent);
                }
            } else {
                info("No student selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Students"), studentList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setCenter(left);
        createDetailsPanel();
    }

    private void createDetailsPanel() {
        detailsPanel = new VBox(10);
        detailsPanel.setId("detailsPanel");
        detailsPanel.setPadding(new Insets(15));
        detailsPanel.setStyle("-fx-border-color: #a0a0a0; -fx-border-width: 1; -fx-border-radius: 5;");
        detailsPanel.setVisible(false);

        Label detailsTitle = new Label("Student Details");
        detailsTitle.setId("detailsTitleLabel");
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        GridPane studentInfoGrid = new GridPane();
        studentInfoGrid.setHgap(10);
        studentInfoGrid.setVgap(5);
        studentInfoGrid.add(new Text("Name:"), 0, 0);
        studentNameLabel = new Label();
        studentNameLabel.setId("detailsNameLabel");
        studentInfoGrid.add(studentNameLabel, 1, 0);
        studentInfoGrid.add(new Text("ID:"), 0, 1);
        studentIdLabel = new Label();
        studentIdLabel.setId("detailsStudentIdLabel");
        studentInfoGrid.add(studentIdLabel, 1, 1);
        studentInfoGrid.add(new Text("Email:"), 0, 2);
        studentEmailLabel = new Label();
        studentEmailLabel.setId("detailsEmailLabel");
        studentInfoGrid.add(studentEmailLabel, 1, 2);

        Label enrolledCoursesLabel = new Label("Enrolled Courses");
        enrolledCoursesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        enrolledCoursesList = new ListView<>();
        enrolledCoursesList.setId("detailsCourseList");
        enrolledCoursesList.setPrefHeight(150);

        Label upcomingExamsLabel = new Label("Upcoming Exam Sessions");
        upcomingExamsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        upcomingExamsTable = new TableView<>();
        upcomingExamsTable.setId("detailsExamTable");
        upcomingExamsTable.setPrefHeight(150);
        setupUpcomingExamTable();

        Button editStudentBtn = new Button("Edit Student");
        editStudentBtn.setId("detailsEditStudentBtn");
        editStudentBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) showStudentDialog(selectedStudent);
        });

        Button deleteStudentBtn = new Button("Delete Student");
        deleteStudentBtn.setId("detailsDeleteStudentBtn");
        deleteStudentBtn.setOnAction(e -> {
            Student selectedStudent = studentList.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Student");
                alert.setHeaderText("Are you sure you want to delete " + selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    students.remove(selectedStudent);
                    removeEnrollmentsForStudent(selectedStudent);
                }
            } else {
                info("No student selected.");
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.setId("detailsCloseBtn");
        closeBtn.setOnAction(e -> hideStudentDetails());

        HBox actionButtons = new HBox(10, editStudentBtn, deleteStudentBtn, closeBtn);

        detailsPanel.getChildren().addAll(
                detailsTitle,
                studentInfoGrid,
                new Separator(),
                enrolledCoursesLabel,
                enrolledCoursesList,
                new Separator(),
                upcomingExamsLabel,
                upcomingExamsTable,
                new Separator(),
                actionButtons
        );
        root.setRight(detailsPanel);
    }

    private void showStudentDetails(Student student) {
        studentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
        studentIdLabel.setText(student.getStudentId());
        studentEmailLabel.setText(student.getEmail());

        enrolledCoursesList.setItems(FXCollections.observableArrayList(student.getEnrolledCourses()));
        upcomingExamsTable.setItems(FXCollections.observableArrayList(student.getAssignedSessions()));

        detailsPanel.setVisible(true);
    }

    private void hideStudentDetails() {
        studentList.getSelectionModel().clearSelection();
        detailsPanel.setVisible(false);
    }

    private void setupUpcomingExamTable() {
        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));
        dateCol.setId("detailsExamDateColumn");

        TableColumn<ExamSession, LocalTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));
        timeCol.setId("detailsExamTimeColumn");

        TableColumn<ExamSession, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getRoomName()));
        roomCol.setId("detailsExamRoomColumn");

        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExam().getCourse().getCourseName()));
        courseCol.setId("detailsExamCourseColumn");

        upcomingExamsTable.getColumns().addAll(dateCol, timeCol, roomCol, courseCol);
        upcomingExamsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        upcomingExamsTable.setPlaceholder(new Label("Select a student to view their exam schedule."));
    }

    private void removeEnrollmentsForStudent(Student student) {
        if (enrollments == null || student == null) {
            return;
        }
        enrollments.removeIf(enrollment -> {
            if (!student.equals(enrollment.getStudent())) {
                return false;
            }
            if (enrollment.getStudent() != null) {
                enrollment.getStudent().removeEnrollment(enrollment);
            }
            if (enrollment.getCourse() != null) {
                enrollment.getCourse().removeEnrollment(enrollment);
            }
            return true;
        });
    }


    public ObservableList<Student> getStudents() {
        return students;
    }

    public Parent getView() {
        return root;
    }

    private void showStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Add Student" : "Edit Student");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Student ID");
        if (student != null) {
            idField.setText(student.getStudentId());
            idField.setEditable(false);
        }

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        if (student != null) {
            firstNameField.setText(student.getFirstName());
        }

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        if (student != null) {
            lastNameField.setText(student.getLastName());
        }

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        if (student != null) {
            emailField.setText(student.getEmail());
        }

        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male");
        maleRadio.setToggleGroup(genderGroup);
        RadioButton femaleRadio = new RadioButton("Female");
        femaleRadio.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(10, maleRadio, femaleRadio);

        if (student == null) {
            maleRadio.setSelected(true); 
        } else {
            if ("Female".equals(student.getGender())) {
                femaleRadio.setSelected(true);
            } else {
                maleRadio.setSelected(true);
            }
        }

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Gender:"), 0, 4);
        grid.add(genderBox, 1, 4);


        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType(student == null ? "Add" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    String gender = genderGroup.getSelectedToggle() != null ? ((RadioButton) genderGroup.getSelectedToggle()).getText() : null;
                    if (!validateStudent(idField.getText(), firstNameField.getText(), lastNameField.getText(), emailField.getText(), gender)) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String gender = ((RadioButton) genderGroup.getSelectedToggle()).getText();
                if (student == null) {
                    return new Student(idField.getText(), firstNameField.getText(), lastNameField.getText(), emailField.getText(), gender);
                } else {
                    student.setFirstName(firstNameField.getText());
                    student.setLastName(lastNameField.getText());
                    student.setEmail(emailField.getText());
                    student.setGender(gender);
                    return student;
                }
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (student == null) {
                students.add(s);
            } else {
                studentList.refresh();
                if(detailsPanel.isVisible()){
                    showStudentDetails(s);
                }
            }
        });
    }

    private boolean validateStudent(String id, String firstName, String lastName, String email, String gender) {
        String errorMessage = "";
        if (id == null || id.trim().isEmpty()) {
            errorMessage += "Student ID cannot be empty.\n";
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            errorMessage += "First name cannot be empty.\n";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            errorMessage += "Last name cannot be empty.\n";
        }
        if (email == null || !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorMessage += "Invalid email address.\n";
        }
        if (gender == null) {
            errorMessage += "Gender must be selected.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            error(errorMessage);
            return false;
        }
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
