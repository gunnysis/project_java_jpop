package lyricsystem;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class UIEventHandler {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Label describeLabel;
//    TextArea textArea;
//    HBox hbox, hbox2;
//    VBox vbox;
//    Scene scene;
    // Service service = new Service(this);
    File defaultLyricFile;
    Stage stage = UIInitializer.stage;
    Service service;

    UIEventHandler(UIInitializer uiInitializer) {
        inputBox = uiInitializer.inputBox;
        serviceTypeBox = uiInitializer.serviceTypeBox;
        describeLabel = uiInitializer.describeLabel;
        defaultLyricFile = uiInitializer.defaultLyricFile;
        service =  new Service(uiInitializer);
    }

    public void handleButtonClick(Button button) {

        switch (button.getText()) {
            case "Enter":
                button.setOnAction(e -> {
                    String title = inputBox.getText();
                    String type = serviceTypeBox.getValue();
                    if (title == null || title.isEmpty()) {
                        describeLabel.setText("Input Title of jpop song");
                    } else {
                        service.showTextArea(title, type);
                    }
                });
                break;
            case "Download Default Lyric File":
                button.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(stage);
                    try {
                        service.uploadLyricFile(file);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                break;
            case "Input Lyric File":
                button.setOnAction(e -> {
                    defaultLyricFile = new File("src/main/resources/lyrics/default-lyric.json");
                    try (FileInputStream defaultLyricJsonFile = new FileInputStream(defaultLyricFile)) {
                        downloadFile(defaultLyricJsonFile, stage);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                break;

        }
    }

    private void downloadFile(FileInputStream fileInputStream, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(defaultLyricFile.getName());
        File targetFile = fileChooser.showSaveDialog(stage);
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
