package de.ks.standbein.validation.sample;

import de.ks.standbein.validation.DefaultDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ValidationSampleApp extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    URL resource = getClass().getResource("View.fxml");
    FXMLLoader loader = new FXMLLoader(resource);
    Pane load = loader.load();

    Scene scene = new Scene(load);
    scene.getStylesheets().add(DefaultDecorator.CSS_FILE_PATH);
    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setOnCloseRequest(e -> primaryStage.close());
  }
}

