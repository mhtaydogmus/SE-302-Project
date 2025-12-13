package edu.ieu.se302.examscheduler.ui;

import edu.ieu.se302.examscheduler.ui.views.CourseManagementView;
import edu.ieu.se302.examscheduler.ui.views.ScheduleGenerationView;
import edu.ieu.se302.examscheduler.ui.views.StudentManagementView;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Main window layout (menu + panels + view container) as required in Meeting Report 4, Task 3.
 * Navigation swaps the center content between placeholder views.
 */
public class MainWindow {

    private final BorderPane root = new BorderPane();
    private final StackPane contentPane = new StackPane();

    private final StudentManagementView studentView = new StudentManagementView();
    private final CourseManagementView courseView = new CourseManagementView();
    private final ScheduleGenerationView scheduleView = new ScheduleGenerationView();

    public MainWindow() {
        root.setTop(buildMenuBar());
        root.setCenter(contentPane);
        BorderPane.setMargin(contentPane, new Insets(10));

        // Default view
        show(studentView.getView());
    }

    public Parent getRoot() {
        return root;
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu file = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> root.getScene().getWindow().hide());
        file.getItems().add(exit);

        Menu data = new Menu("Data");
        MenuItem students = new MenuItem("Student Management");
        students.setOnAction(e -> show(studentView.getView()));
        MenuItem courses = new MenuItem("Course Management");
        courses.setOnAction(e -> show(courseView.getView()));
        data.getItems().addAll(students, courses);

        Menu schedule = new Menu("Schedule");
        MenuItem generate = new MenuItem("Schedule Generation");
        generate.setOnAction(e -> show(scheduleView.getView()));
        schedule.getItems().add(generate);

        Menu help = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAboutDialog());
        help.getItems().add(about);

        menuBar.getMenus().addAll(file, data, schedule, help);
        return menuBar;
    }

    private void show(Parent view) {
        contentPane.getChildren().setAll(view);
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Student Exam Scheduling System");
        alert.setContentText(
                "GUI Skeleton (Meeting Report 4 - Task 3)\n" +
                "- Student management view (placeholder)\n" +
                "- Course management view (placeholder)\n" +
                "- Schedule generation view (placeholder)\n\n" +
                "This UI layer is designed to be connected to the core model and scheduling logic in later tasks."
        );
        alert.showAndWait();
    }
}
