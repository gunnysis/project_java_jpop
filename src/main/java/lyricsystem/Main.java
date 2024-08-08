package lyricsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Button enterButton, outputDefaultJsonFile, inputJsonFile;
    Label describeLabel;
    TextArea textArea;
    HBox hbox, hbox2;
    VBox vbox;
    Scene scene;
    Service service = new Service(this);
    File defaultLyricFile;


    @Override
    public void start(Stage primaryStage) {
        // Input Box
        inputBox = new TextField();
        inputBox.setPromptText("Input Title of jpop song");

        // Select Box
        serviceTypeBox = new ComboBox<>();
        serviceTypeBox.getItems().addAll("japanese", "romaji", "words");
        serviceTypeBox.setValue("japanese");

        enterButton = new Button("Enter");

        describeLabel = new Label();
        describeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        outputDefaultJsonFile = new Button("Download Default Lyric File");
        inputJsonFile = new Button("Input Lyric File");

        inputJsonFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            try {
                service.uploadLyricFile(file);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        outputDefaultJsonFile.setOnAction(e -> {
            defaultLyricFile = new File("src/main/resources/default-lyric.json");
            try (FileInputStream defaultLyricJsonFile = new FileInputStream(defaultLyricFile)) {
                downloadFile(defaultLyricJsonFile, primaryStage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPromptText("Text Area (Read Only)");

        hbox = new HBox(10, inputBox, serviceTypeBox, enterButton);
        hbox.setHgrow(inputBox, Priority.ALWAYS);

        hbox2 = new HBox(10, describeLabel, new HBox(), inputJsonFile, outputDefaultJsonFile);
        HBox.setHgrow(hbox2.getChildren().get(1), Priority.ALWAYS);

        vbox = new VBox(10, hbox, hbox2, textArea);
        vbox.setPadding(new Insets(10));
        vbox.setVgrow(textArea, Priority.ALWAYS);

        scene = new Scene(vbox, 600, 400); // 적절한 크기로 설정

        primaryStage.setScene(scene);
        primaryStage.setTitle("Lyrics System");
        primaryStage.show();

        enterButton.setOnAction(e -> {
            String title = inputBox.getText();
            String type = serviceTypeBox.getValue();
            if (title == null || title.isEmpty()) {
                describeLabel.setText("Input Title of jpop song");
            } else {
                service.showTextArea(title, type);
            }
        });
    }

    public static void main(String[] args) {
         launch(args);
    }

    private void downloadFile(FileInputStream fileInputStream, Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(defaultLyricFile.getName());
        File targetFile = fileChooser.showSaveDialog(primaryStage);
        if (targetFile != null) {
            try(FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer,0,bytesRead);
                }
                describeLabel.setText("Success to download Lyric Json File");
                describeLabel.setStyle("fx-text-fill: grey;");
            } catch (IOException e) {
                describeLabel.setText("Can't Download Lyric Json File");
                e.printStackTrace();
            }
        }
    }

}