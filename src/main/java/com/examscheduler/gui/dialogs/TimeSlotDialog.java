package com.examscheduler.gui.dialogs;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.TimeSlot;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotDialog extends Stage {

    public TimeSlotDialog(TimeSlot slot) {
        setTitle(slot == null ? "Add Time Slot" : "Edit Time Slot");
        initModality(Modality.APPLICATION_MODAL);

        DatePicker datePicker = new DatePicker();
        TextField startField = new TextField();
        startField.setPromptText("HH:MM (e.g. 09:00)");
        TextField endField = new TextField();
        endField.setPromptText("HH:MM (e.g. 11:00)");

        if (slot != null) {
            datePicker.setValue(slot.getDate());
            startField.setText(slot.getStartTime().toString());
            endField.setText(slot.getEndTime().toString());
        }

        Button save = new Button("Save");
        save.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            String startStr = startField.getText().trim();
            String endStr = endField.getText().trim();

            if (date == null || startStr.isEmpty() || endStr.isEmpty()) {
                alert("Date, start time and end time are required!");
                return;
            }

            LocalTime startTime;
            LocalTime endTime;
            try {
                startTime = LocalTime.parse(startStr);
                endTime = LocalTime.parse(endStr);
            } catch (Exception ex) {
                alert("Time format must be HH:MM (e.g. 09:00)");
                return;
            }

            if (!endTime.isAfter(startTime)) {
                alert("End time must be after start time!");
                return;
            }

            TimeSlot newSlot = new TimeSlot(date, startTime, endTime);

            // Duplicate kontrolü: Aynı tarih ve aynı zaman aralığı
            boolean exists = DataStore.getTimeSlots().stream()
                    .anyMatch(ts -> ts.equals(newSlot));

            if (slot == null && exists) {
                alert("This time slot already exists!");
                return;
            }

            if (slot == null) {
                DataStore.getTimeSlots().add(newSlot);
            } else {
                slot.setDate(date);
                slot.setStartTime(startTime);
                slot.setEndTime(endTime);
            }

            close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Date:"), datePicker);
        grid.addRow(1, new Label("Start Time:"), startField);
        grid.addRow(2, new Label("End Time:"), endField);
        grid.addRow(3, save, cancel);

        setScene(new Scene(grid));
        sizeToScene();
        setResizable(false);
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}