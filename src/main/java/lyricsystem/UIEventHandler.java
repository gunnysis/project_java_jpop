package lyricsystem;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lyricsystem.ServiceLyrics.readFromFile;


public class UIEventHandler {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Label describeLabel;
    File defaultLyricFile;
    Stage stage = UIInitializer.stage;
    ServiceLyrics serviceLyrics;
    TextArea textArea;
    Button modifyContentButton;
    String title;
    String type;

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
            title = inputBox.getText();
            type = serviceTypeBox.getValue();
            if (title == null || title.isEmpty()) {
                describeLabel.setText("Input Title of jpop song");
                describeLabel.setStyle("-fx-text-fill: red;");
            } else {
                if (type.equals("words")) {
                    modifyContentButton.setDisable(false);
                    textArea.setEditable(true);
                } else {
                    modifyContentButton.setDisable(true);
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
            title = inputBox.getText();
            String type = serviceTypeBox.getValue();
            String modifyContent = textArea.getText();

            modifyContentOfWordFile(title, type, modifyContent);
            describeLabel.setText("Modified to Word Json File");
            describeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
        });
        buttonActions.put("Search Meaning", this::showSearchWindow);
        buttonActions.put("File List", () -> {
            textArea.setText(showLyricFiles());
        });
    }

    private String showLyricFiles() {
        String lyricDirPath = "src/main/resources/lyrics";
        File lyricDir = new File(lyricDirPath);
        File[] listOfFiles = lyricDir.listFiles();
        StringBuffer listOfFileName = new StringBuffer();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && !file.getName().equals("titleNameOfSong.json")) {
                    listOfFileName.append(file.getName(), 0, file.getName().indexOf('-')).append("\n");
                }
            }
        }
        return  listOfFileName.toString();
    }

    public void showSearchWindow() {
        TextField searchTextField = new TextField();
        Button searchButton = new Button("Search");
        HBox panel = new HBox(10, new Label("Word"), searchTextField, searchButton);
        panel.setAlignment(Pos.CENTER);
        Label meaningLabel = new Label();
        VBox root = new VBox(15, panel, meaningLabel);
        root.setPadding(new Insets(20));
        Stage dialog = new Stage();

        searchButton.setOnAction( e -> {
            String searchText = searchTextField.getText();
            String searchedMeaningOfWord = "";
            if (searchText == null && searchText.isEmpty()) {
                Platform.runLater(() -> meaningLabel.setText("Input word"));
            }
            else {
                try {
                    searchedMeaningOfWord = searchMeaningOfWord(searchText);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            final String finalSearchedMeaningOfWord = searchedMeaningOfWord;
            Platform.runLater(() -> meaningLabel.setText(finalSearchedMeaningOfWord));
        });

        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Find");
        dialog.setScene(new Scene(root));
        dialog.setWidth(320);
        dialog.setHeight(150);
        dialog.show();
    }

    public String searchMeaningOfWord(String searchText) throws IOException {
        Map targetWordFile = (Map) readFromFile("src/main/resources/words/" + title + "-words" + ".json", Map.class);
        String meaning = (String) targetWordFile.get(searchText);

        return meaning == null ? "Word not found" : meaning;
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
        String fileName = titleOfSong + "-words" + ".json";
        File modifyFile = new File("src/main/resources/words/"+ fileName);

        try(FileOutputStream fileOutputStream = new FileOutputStream(modifyFile)) {
            fileOutputStream.write(modifedContent.getBytes());
            textArea.setEditable(false);
        } catch (IOException e) {
            describeLabel.setText("Can't Modified to Lyric Json File");
            e.printStackTrace();
        }

         serviceLyrics.showTextArea(titleOfSong, serviceType);
    }

}
