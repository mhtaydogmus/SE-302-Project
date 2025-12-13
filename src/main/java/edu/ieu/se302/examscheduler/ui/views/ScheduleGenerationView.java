package edu.ieu.se302.examscheduler.ui.views;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Placeholder view for Schedule Generation screen (Meeting Report 4 - Task 3).
 * Provides basic constraint toggles and a dummy 'Generate' action.
 */
public class ScheduleGenerationView {

    private final BorderPane root = new BorderPane();

    public ScheduleGenerationView() {
        root.setPadding(new Insets(10));

        Label title = new Label("Schedule Generation");
        title.getStyleClass().add("view-title");

        CheckBox noOverlap = new CheckBox("No Overlap");
        noOverlap.setSelected(true);

        CheckBox roomCapacity = new CheckBox("Room Capacity");
        roomCapacity.setSelected(true);

        CheckBox maxExamsPerDay = new CheckBox("Max Exams Per Day");
        maxExamsPerDay.setSelected(true);

        Button generateBtn = new Button("Generate");
        Button validateBtn = new Button("Validate");
        Button clearBtn = new Button("Clear Output");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPromptText("Schedule output and conflict reports will appear here.");

        generateBtn.setOnAction(e -> {
            output.appendText("Generate clicked (placeholder).\n");
            output.appendText("Selected constraints: " +
                    (noOverlap.isSelected() ? "[NoOverlap] " : "") +
                    (roomCapacity.isSelected() ? "[RoomCapacity] " : "") +
                    (maxExamsPerDay.isSelected() ? "[MaxExamsPerDay] " : "") +
                    "\n\n");
        });

        validateBtn.setOnAction(e -> output.appendText("Validate clicked (placeholder).\n\n"));
        clearBtn.setOnAction(e -> output.clear());

        HBox buttons = new HBox(8, generateBtn, validateBtn, clearBtn);

        VBox controls = new VBox(10,
                title,
                new Label("Active Constraints (placeholder)"),
                noOverlap,
                roomCapacity,
                maxExamsPerDay,
                buttons
        );
        controls.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(controls);
        root.setCenter(output);
    }

    public Parent getView() {
        return root;
    }
}
