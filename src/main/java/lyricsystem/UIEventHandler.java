package lyricsystem;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    TextArea textArea;
    Button modifyContentButton;

    private Map<String, ButtonAction> buttonActions = new HashMap<>();


    UIEventHandler(UIInitializer uiInitializer) {
        inputBox = uiInitializer.inputBox;
        serviceTypeBox = uiInitializer.serviceTypeBox;
        describeLabel = uiInitializer.describeLabel;
        defaultLyricFile = uiInitializer.defaultLyricFile;
        textArea = uiInitializer.textArea;
        modifyContentButton = uiInitializer.modifyContentButton;
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
                if (type.equals("words")) {
                    modifyContentButton.setDisable(false);
                    textArea.setEditable(true);
                }
                serviceLyrics.showTextArea(title, type);
            }
        });
        buttonActions.put("Upload Lyric File", () -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            try {
                serviceLyrics.uploadLyricFile(file);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonActions.put("Download Default Lyric File", () -> {
            defaultLyricFile = new File("src/main/resources/lyrics/titleNameOfSong.json");
            try (FileInputStream defaultLyricJsonFile = new FileInputStream(defaultLyricFile)) {
                downloadFile(defaultLyricJsonFile, stage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonActions.put("Modify Content", () -> {
            String title = inputBox.getText();
            String type = serviceTypeBox.getValue();
            String modifyContent = textArea.getText();
            describeLabel.setText(modifyContent);

            String fileName = title + "-words" + ".json";
            File modifyFile = new File("src/main/resources/words/"+ fileName);

            try(FileOutputStream fileOutputStream = new FileOutputStream(modifyFile)) {
                fileOutputStream.write(modifyContent.getBytes());
                describeLabel.setText("Modified to Word Json File");
                describeLabel.setStyle("fx-text-fill: grey;");
                textArea.setEditable(false);
            } catch (IOException e) {
                describeLabel.setText("Can't Modified to Lyric Json File");
                e.printStackTrace();
            }
        });
    }


    public void handleButtonClick(List<Button> buttons) {
        for (Button button : buttons) {
            button.setOnAction(e -> {
                ButtonAction buttonAction = buttonActions.get(button.getText());
                if (buttonAction != null) {
                    try {
                        buttonAction.execute();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
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

    public void modifyContentOfWordFile(String titleOfSong, String serviceType, String modifedContent) throws IOException {
        serviceLyrics.showTextArea(titleOfSong, "words");

         String fileName = titleOfSong + "-words" + ".json";
        Path modifyFilePath = Paths.get("src/main/resources/words/"+ fileName);
         Files.write(modifyFilePath, modifedContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

         serviceLyrics.showTextArea(titleOfSong, serviceType);
    }

}
