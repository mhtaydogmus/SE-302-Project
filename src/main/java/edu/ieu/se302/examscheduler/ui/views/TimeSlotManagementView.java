package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.TimeSlot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class TimeSlotManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<TimeSlot> timeSlots = FXCollections.observableArrayList();
    private final ListView<TimeSlot> timeSlotList = new ListView<>(timeSlots);

    public TimeSlotManagementView() {
        // Sample Data
        timeSlots.add(new TimeSlot(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(11, 0)));
        timeSlots.add(new TimeSlot(LocalDate.now(), LocalTime.of(13, 0), LocalTime.of(15, 0)));


        root.setPadding(new Insets(10));

        Label title = new Label("Time Slot Management");
        title.getStyleClass().add("view-title");

        timeSlotList.setCellFactory(new Callback<ListView<TimeSlot>, ListCell<TimeSlot>>() {
            @Override
            public ListCell<TimeSlot> call(ListView<TimeSlot> param) {
                return new ListCell<TimeSlot>() {
                    @Override
                    protected void updateItem(TimeSlot item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getDate() + " " + item.getStartTime() + " - " + item.getEndTime());
                        }
                    }
                };
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showTimeSlotDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            TimeSlot selectedTimeSlot = timeSlotList.getSelectionModel().getSelectedItem();
            if (selectedTimeSlot != null) {
                showTimeSlotDialog(selectedTimeSlot);
            } else {
                info("No time slot selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            TimeSlot selectedTimeSlot = timeSlotList.getSelectionModel().getSelectedItem();
            if (selectedTimeSlot != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Time Slot");
                alert.setHeaderText("Are you sure you want to delete this time slot?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    timeSlots.remove(selectedTimeSlot);
                }
            } else {
                info("No time slot selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Time Slots"), timeSlotList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public ObservableList<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public Parent getView() {
        return root;
    }

    private void showTimeSlotDialog(TimeSlot timeSlot) {
        Dialog<TimeSlot> dialog = new Dialog<>();
        dialog.setTitle(timeSlot == null ? "Add Time Slot" : "Edit Time Slot");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker();
        if (timeSlot != null) {
            datePicker.setValue(timeSlot.getDate());
        }

        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
        if(timeSlot != null){
            startHourSpinner.getValueFactory().setValue(timeSlot.getStartTime().getHour());
            startMinuteSpinner.getValueFactory().setValue(timeSlot.getStartTime().getMinute());
        }


        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 11);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
        if(timeSlot != null){
            endHourSpinner.getValueFactory().setValue(timeSlot.getEndTime().getHour());
            endMinuteSpinner.getValueFactory().setValue(timeSlot.getEndTime().getMinute());
        }

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Start Time:"), 0, 1);
        grid.add(new HBox(5, startHourSpinner, new Label(":"), startMinuteSpinner), 1, 1);
        grid.add(new Label("End Time:"), 0, 2);
        grid.add(new HBox(5, endHourSpinner, new Label(":"), endMinuteSpinner), 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    LocalDate date = datePicker.getValue();
                    LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
                    LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());
                    if (!validateTimeSlot(date, startTime, endTime)) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDate date = datePicker.getValue();
                LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
                LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

                if (timeSlot == null) {
                    return new TimeSlot(date, startTime, endTime);
                } else {
                    timeSlot.setDate(date);
                    timeSlot.setStartTime(startTime);
                    timeSlot.setEndTime(endTime);
                    return timeSlot;
                }
            }
            return null;
        });

        Optional<TimeSlot> result = dialog.showAndWait();
        result.ifPresent(ts -> {
            if (timeSlot == null) {
                timeSlots.add(ts);
            } else {
                timeSlotList.refresh();
            }
        });
    }

    private boolean validateTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        String errorMessage = "";
        if (date == null) {
            errorMessage += "Date cannot be empty.\n";
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            errorMessage += "End time must be after start time.\n";
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
