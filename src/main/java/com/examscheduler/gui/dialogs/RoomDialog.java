package com.examscheduler.gui.dialogs;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.Room;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RoomDialog extends Stage {

    public RoomDialog(Room room) {
        setTitle(room == null ? "Add Room" : "Edit Room");
        initModality(Modality.APPLICATION_MODAL);

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField capacityField = new TextField();

        if (room != null) {
            idField.setText(room.getRoomId());
            idField.setDisable(true); // ID değiştirilemez
            nameField.setText(room.getRoomName());
            capacityField.setText(String.valueOf(room.getCapacity()));
        }

        Button save = new Button("Save");
        save.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String capStr = capacityField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || capStr.isEmpty()) {
                alert("All fields are required!");
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capStr);
                if (capacity <= 0) {
                    alert("Capacity must be a positive number!");
                    return;
                }
            } catch (NumberFormatException ex) {
                alert("Capacity must be a valid number!");
                return;
            }

            // Duplicate ID kontrolü (sadece yeni eklemede)
            if (room == null && DataStore.rooms.stream().anyMatch(r -> r.getRoomId().equals(id))) {
                alert("Room ID already exists!");
                return;
            }

            if (room == null) {
                Room newRoom = new Room(id, name, capacity);
                DataStore.rooms.add(newRoom);
            } else {
                room.setRoomName(name);
                room.setCapacity(capacity);
            }

            close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> close());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Room ID:"), idField);
        grid.addRow(1, new Label("Room Name:"), nameField);
        grid.addRow(2, new Label("Capacity:"), capacityField);
        grid.addRow(3, save, cancel);

        setScene(new Scene(grid));
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}