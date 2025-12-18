package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.Room;
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

import java.util.Optional;

public class RoomManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ListView<Room> roomList = new ListView<>(rooms);

    public RoomManagementView() {
        // Sample Data
        rooms.add(new Room("R101", "Room 101", 30));
        rooms.add(new Room("R102", "Room 102", 25));

        root.setPadding(new Insets(10));

        Label title = new Label("Room Management");
        title.getStyleClass().add("view-title");

        roomList.setCellFactory(new Callback<ListView<Room>, ListCell<Room>>() {
            @Override
            public ListCell<Room> call(ListView<Room> param) {
                return new ListCell<Room>() {
                    @Override
                    protected void updateItem(Room item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getRoomName() + " (Capacity: " + item.getCapacity() + ")");
                        }
                    }
                };
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> showRoomDialog(null));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> {
            Room selectedRoom = roomList.getSelectionModel().getSelectedItem();
            if (selectedRoom != null) {
                showRoomDialog(selectedRoom);
            } else {
                info("No room selected.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Room selectedRoom = roomList.getSelectionModel().getSelectedItem();
            if (selectedRoom != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Room");
                alert.setHeaderText("Are you sure you want to delete " + selectedRoom.getRoomName() + "?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    rooms.remove(selectedRoom);
                }
            } else {
                info("No room selected.");
            }
        });

        HBox buttons = new HBox(8, addBtn, editBtn, deleteBtn);
        VBox left = new VBox(10, title, new Label("Rooms"), roomList, buttons);
        left.setPadding(new Insets(0, 10, 0, 0));

        root.setLeft(left);
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public Parent getView() {
        return root;
    }

    private void showRoomDialog(Room room) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(room == null ? "Add Room" : "Edit Room");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Room ID");
        if (room != null) {
            idField.setText(room.getRoomId());
            idField.setEditable(false);
        }

        TextField nameField = new TextField();
        nameField.setPromptText("Room Name");
        if (room != null) {
            nameField.setText(room.getRoomName());
        }

        Spinner<Integer> capacitySpinner = new Spinner<>(1, 100, 30);
        if (room != null) {
            capacitySpinner.getValueFactory().setValue(room.getCapacity());
        }

        grid.add(new Label("Room ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Room Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Capacity:"), 0, 2);
        grid.add(capacitySpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    if (!validateRoom(idField.getText(), nameField.getText())) {
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (room == null) {
                    return new Room(idField.getText(), nameField.getText(), capacitySpinner.getValue());
                } else {
                    room.setRoomName(nameField.getText());
                    room.setCapacity(capacitySpinner.getValue());
                    return room;
                }
            }
            return null;
        });

        Optional<Room> result = dialog.showAndWait();
        result.ifPresent(r -> {
            if (room == null) {
                rooms.add(r);
            } else {
                roomList.refresh();
            }
        });
    }

    private boolean validateRoom(String id, String name) {
        String errorMessage = "";
        if (id == null || id.trim().isEmpty()) {
            errorMessage += "Room ID cannot be empty.\n";
        }
        if (name == null || name.trim().isEmpty()) {
            errorMessage += "Room name cannot be empty.\n";
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
