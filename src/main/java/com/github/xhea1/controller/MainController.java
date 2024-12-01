package com.github.xhea1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class MainController {

    @FXML
    private Button loadImageButton;

    @FXML
    private Button processImageButton;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField inputField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        // Initialization code, if needed
        statusLabel.setText("Ready");
    }

    @FXML
    private void handleLoadImage(ActionEvent event) {
        // Handle image loading logic here
        statusLabel.setText("Loading image...");
        // Example: Use FileChooser to select an image and display it in imageView
    }

    @FXML
    private void handleProcessImage(ActionEvent event) {
        // Handle image processing logic here
        statusLabel.setText("Processing image...");
        // TODO: query any existing metadata for this file/gallery from stash using GraphQLService
        // TODO: detect faces using ImageProcessor and find images with the same face
    }

    // Additional methods to interact with the UI or backend logic can be added here
}
