package com.icc490.bike.desktop;

import com.icc490.bike.desktop.model.Record;
import com.icc490.bike.desktop.model.RecordRequest;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class App extends Application {
    private ApiClient apiClient;
    private TextArea recordDisplayArea;
    private TextField studentIdField;
    private TextField studentNameField;
    private TextField bicycleDescriptionField;

    @Override
    public void start(Stage stage) {
        apiClient = new ApiClient();
        stage.setTitle("Gestión de Estacionamiento de Biciletas UFRO");

        recordDisplayArea = new TextArea();
        recordDisplayArea.setEditable(false);
        recordDisplayArea.setPrefHeight(250);

        Button loadRecordsButton = new Button("Actualizar registros");
        loadRecordsButton.setOnAction(e -> loadRecords());

        studentIdField = new TextField();
        studentIdField.setPromptText("Id Estudiante");
        studentNameField = new TextField();
        studentNameField.setPromptText("Nombre Estudiante");
        bicycleDescriptionField = new TextField();
        bicycleDescriptionField.setPromptText("Descripción Bicicleta");

        Button createRecordButton = new Button("Crear nuevo registro");
        createRecordButton.setOnAction(e -> createRecord());

        //Diseño de la ventana
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(
                new Label("Registros Bicicletas"),
                recordDisplayArea,
                loadRecordsButton,
                new Separator(),
                new Label("Datos para Nuevo Registro:"),
                studentIdField,
                studentNameField,
                bicycleDescriptionField,
                createRecordButton
        );

        Scene scene = new Scene(root, 650, 600);
        stage.setScene(scene);
        stage.show();

        loadRecords();
    }

    /**
     * Carga y muestra los registros de bicileta desde la API
     * Operación asíncrona para evitar bloqueos de la IU.
     */
    private void loadRecords() {
        recordDisplayArea.setText("Cargando registros...");
        apiClient.getAllRecords().thenAccept(records -> {
            Platform.runLater(() -> {
                if (records != null && !records.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Object record : records) {
                        sb.append(record.toString()).append("\n");
                    }
                    recordDisplayArea.setText(sb.toString());
                } else {
                    recordDisplayArea.setText("Error al cargar");
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                recordDisplayArea.setText("Error al cargar registros: " + ex.getMessage());
                showAlert("Error de Carga", "No se pudieron cargar los registros.");
            });
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Solicita crear nuevo registro de bici a la API
     * Verifica que los campos no estén vacíos
     */
    private void createRecord() {
        String studentId = studentIdField.getText().trim();
        String studentName = studentNameField.getText().trim();
        String bicycleDescription = bicycleDescriptionField.getText().trim();

        if (studentId.isEmpty() || studentName.isEmpty() || bicycleDescription.isEmpty()) {
            showAlert("Error de entrada", "Todos los campos son obligatorios.");
            return;
        }

        RecordRequest request = new RecordRequest(studentId, studentName, bicycleDescription);

        apiClient.createRecord(request).thenAccept(newRecord -> {
            Platform.runLater(() -> {
                if (newRecord != null) {
                    showAlert("Éxito", "Registro creado con ID: " + newRecord.getId());
                    studentIdField.clear();;
                    studentNameField.clear();
                    bicycleDescriptionField.clear();
                    loadRecords();
                } else {
                    showAlert("Error", "No fue posible crear el registro.");
                }
            });
        }).exceptionally(ex  -> {
            Platform.runLater(() -> {
                showAlert("Error de API", "Error al crear registro: " + ex.getMessage());
            });
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Ventana de alerta de info de usuario
     */
    private void  showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] arg) {
        launch();
    }
}