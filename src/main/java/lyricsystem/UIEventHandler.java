package lyricsystem;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIEventHandler {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Label describeLabel;
    File defaultLyricFile;
    Stage stage = UIInitializer.stage;
    ServiceLyrics serviceLyrics;
    private  Map<String, ButtonAction> buttonActions = new HashMap<>();


    UIEventHandler(UIInitializer uiInitializer) {
        inputBox = uiInitializer.inputBox;
        serviceTypeBox = uiInitializer.serviceTypeBox;
        describeLabel = uiInitializer.describeLabel;
        defaultLyricFile = uiInitializer.defaultLyricFile;
        serviceLyrics =  new ServiceLyrics(uiInitializer);
        initializeButtonActions();
    }

    private void initializeButtonActions() {
        buttonActions.put("Enter", () -> {
            String title = inputBox.getText();
            String type = serviceTypeBox.getValue();
            if (title == null || title.isEmpty()) {
                describeLabel.setText("Input Title of jpop song");
            } else {
                serviceLyrics.showTextArea(title, type);
            }
        });
        buttonActions.put("Input Lyric File", () -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            try {
                serviceLyrics.uploadLyricFile(file);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonActions.put("Download Default Lyric File", () -> {
            defaultLyricFile = new File("src/main/resources/lyrics/default-lyric.json");
            try (FileInputStream defaultLyricJsonFile = new FileInputStream(defaultLyricFile)) {
                downloadFile(defaultLyricJsonFile, stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }


    public void handleButtonClick(List<Button> buttons) {
        for (Button button : buttons) {
            button.setOnAction(e -> {
                ButtonAction buttonAction = buttonActions.get(button.getText());
                if (buttonAction != null) {
                    buttonAction.execute();
                }
            });
        }
    }

    private void downloadFile(FileInputStream fileInputStream, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(defaultLyricFile.getName());
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile != null) {
            try(FileOutputStream fileOutputStream = new FileOutputStream(saveFile)) {
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
