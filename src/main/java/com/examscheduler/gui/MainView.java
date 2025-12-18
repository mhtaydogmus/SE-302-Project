package com.examscheduler.gui;

import com.examscheduler.data.DataStore;
import com.examscheduler.entity.Course;
import com.examscheduler.entity.Room;
import com.examscheduler.entity.Student;
import com.examscheduler.entity.TimeSlot;
import com.examscheduler.gui.dialogs.CourseDialog;
import com.examscheduler.gui.dialogs.RoomDialog;
import com.examscheduler.gui.dialogs.StudentDialog;
import com.examscheduler.gui.dialogs.TimeSlotDialog;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainView extends BorderPane {

    public MainView() {
        TabPane tabPane = new TabPane();

        tabPane.getTabs().add(createEntityTab("Students", DataStore.students, Student.class));
        tabPane.getTabs().add(createEntityTab("Courses", DataStore.courses, Course.class));
        tabPane.getTabs().add(createEntityTab("Rooms", DataStore.rooms, Room.class));
        tabPane.getTabs().add(createEntityTab("Time Slots", DataStore.timeSlots, TimeSlot.class));

        setCenter(tabPane);
    }

    private <T> Tab createEntityTab(String title, java.util.List<T> list, Class<T> type) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        TableView<T> table = new TableView<>();
        table.setItems(FXCollections.observableList(list));

        // Kolonlar
        if (type == Student.class) {
            addColumn(table, "ID", s -> ((Student) s).getStudentId());
            addColumn(table, "Full Name", s -> ((Student) s).getFullName());
            addColumn(table, "Email", s -> ((Student) s).getEmail());
            addColumn(table, "Enrollments", s -> String.valueOf(((Student) s).getEnrollments().size()));
        } else if (type == Course.class) {
            addColumn(table, "ID", c -> ((Course) c).getCourseId());
            addColumn(table, "Code", c -> ((Course) c).getCourseCode());
            addColumn(table, "Name", c -> ((Course) c).getCourseName());
            addColumn(table, "Credits", c -> String.valueOf(((Course) c).getCredits()));
            addColumn(table, "Students", c -> String.valueOf(((Course) c).getEnrollmentCount()));
        } else if (type == Room.class) {
            addColumn(table, "ID", r -> ((Room) r).getRoomId());
            addColumn(table, "Name", r -> ((Room) r).getRoomName());
            addColumn(table, "Capacity", r -> String.valueOf(((Room) r).getCapacity()));
        } else if (type == TimeSlot.class) {
            addColumn(table, "Date", t -> ((TimeSlot) t).getDate().toString());
            addColumn(table, "Start", t -> ((TimeSlot) t).getStartTime().toString());
            addColumn(table, "End", t -> ((TimeSlot) t).getEndTime().toString());
        }

        // SEÇİM AYARLARI
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().setCellSelectionEnabled(false);

        // DIŞ ÇERÇEVEYİ KALDIR
        table.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        // SATIRI GÜÇLÜ MAVİ YAP + YAZI BEYAZ
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    T rowItem = row.getItem();
                    table.getSelectionModel().select(rowItem);

                    if (event.getClickCount() == 2) {
                        openDialog(type, rowItem, table);
                    }
                }
            });

            // Seçili olduğunda güçlü mavi + beyaz yazı
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.setStyle("-fx-background-color: #0078d7;"); // Güçlü mavi
                } else {
                    row.setStyle("");
                }
            });

            // Hücrelerdeki yazıyı seçiliyken beyaz yap
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (row.isSelected()) {
                    row.getChildrenUnmodifiable().stream()
                            .filter(node -> node instanceof TableCell)
                            .forEach(cell -> ((TableCell<?, ?>) cell).setStyle("-fx-text-fill: white;"));
                }
            });

            return row;
        });

        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");

        add.setOnAction(e -> openDialog(type, null, table));

        edit.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openDialog(type, selected, table);
            } else {
                new Alert(Alert.AlertType.WARNING, "Lütfen düzenlemek için bir öğe seçin").show();
            }
        });

        delete.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (confirm("Bu " + title.substring(0, title.length() - 1).toLowerCase() + " silinsin mi?")) {
                    list.remove(selected);
                    table.refresh();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "Lütfen silmek için bir öğe seçin").show();
            }
        });

        VBox buttonsBox = new VBox(5, add, edit, delete);
        VBox content = new VBox(10, table, buttonsBox);
        content.setStyle("-fx-padding: 15;");

        tab.setContent(content);

        return tab;
    }

    private <T> void addColumn(TableView<T> table, String title, java.util.function.Function<T, String> func) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(func.apply(data.getValue())));
        col.setMinWidth(100);
        table.getColumns().add(col);
    }

    private <T> void openDialog(Class<T> type, T item, TableView<T> table) {
        Stage dialog = null;
        if (type == Student.class) dialog = new StudentDialog((Student) item);
        else if (type == Course.class) dialog = new CourseDialog((Course) item);
        else if (type == Room.class) dialog = new RoomDialog((Room) item);
        else if (type == TimeSlot.class) dialog = new TimeSlotDialog((TimeSlot) item);

        if (dialog != null) {
            dialog.showAndWait();

            // EKLEME VEYA DÜZENLEME SONRASI ZORLA YENİLEME VE SEÇİM
            table.refresh();

            // Yeni eklenen item'ı otomatik seç (eğer yeni ekleme ise)
            if (item == null) {
                // Son eklenen item'ı seç
                int lastIndex = table.getItems().size() - 1;
                if (lastIndex >= 0) {
                    table.getSelectionModel().select(lastIndex);
                    table.scrollTo(lastIndex);
                }
            } else {
                // Düzenlenen item'ı tekrar seç
                table.getSelectionModel().select(item);
                table.scrollTo(item);
            }
        }
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}