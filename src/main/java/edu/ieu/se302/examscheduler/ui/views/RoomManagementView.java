package edu.ieu.se302.examscheduler.ui.views;

import com.examscheduler.entity.ExamSession;
import com.examscheduler.entity.Room;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
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
import java.util.stream.Collectors;

public class RoomManagementView {

    private final BorderPane root = new BorderPane();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ListView<Room> roomList = new ListView<>(rooms);
    private final ObservableList<ExamSession> scheduleSessions;
    private final TableView<ExamSession> roomScheduleTable = new TableView<>();
    private Label roomScheduleTitle;

    public RoomManagementView(ObservableList<ExamSession> scheduleSessions) {
        this.scheduleSessions = scheduleSessions;

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
                            // Prefer roomName, fallback to roomId
                            String name = item.getRoomName();
                            if (name == null || name.isBlank()) {
                                name = item.getRoomId();
                            }
                            setText(name + " (Capacity: " + item.getCapacity() + ")");
                        }
                    }
                };
            }
        });
        roomList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateRoomSchedule(newSelection);
        });
        scheduleSessions.addListener((ListChangeListener<ExamSession>) change -> {
            updateRoomSchedule(roomList.getSelectionModel().getSelectedItem());
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
        root.setCenter(buildRoomSchedulePanel());
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public Parent getView() {
        return root;
    }

    private VBox buildRoomSchedulePanel() {
        roomScheduleTitle = new Label("Room Schedule");
        roomScheduleTitle.getStyleClass().add("view-subtitle");

        TableColumn<ExamSession, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getDate()));

        TableColumn<ExamSession, LocalTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimeSlot().getStartTime()));

        TableColumn<ExamSession, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getExam().getCourse().getCourseName()));

        roomScheduleTable.getColumns().addAll(dateCol, timeCol, courseCol);
        roomScheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomScheduleTable.setPlaceholder(new Label("Select a room to view its schedule."));

        VBox panel = new VBox(10, roomScheduleTitle, roomScheduleTable);
        panel.setPadding(new Insets(0, 0, 0, 10));
        return panel;
    }

    private void updateRoomSchedule(Room room) {
        if (room == null) {
            roomScheduleTitle.setText("Room Schedule");
            roomScheduleTable.setItems(FXCollections.observableArrayList());
            return;
        }

        roomScheduleTitle.setText("Room Schedule - " + room.getRoomName());
        roomScheduleTable.setItems(
                FXCollections.observableArrayList(
                        scheduleSessions.stream()
                                .filter(session -> session.getRoom() != null && room.equals(session.getRoom()))
                                .collect(Collectors.toList())
                )
        );
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

        Spinner<Integer> capacitySpinner = new Spinner<>(1, 10000, 30);
        capacitySpinner.setEditable(true);
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
