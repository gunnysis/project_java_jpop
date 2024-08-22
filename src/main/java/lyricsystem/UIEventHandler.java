package lyricsystem;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<String, ButtonAction> buttonActions = new HashMap<>();


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
            String modifyContent = textArea.getText();

            String message = modifyContentOfWordFile(title, modifyContent) ?
                    "Modified to Word Json File" :
                    "Please use letters, numbers, spaces, and most special characters except for double quotes, backslashes, Tab, New Line, Carriage Return, Backspace, Form Feed, null character, Unicode control characters.";
            describeLabel.setText(message);
            describeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
        });
        buttonActions.put("Search Meaning", this::showSearchWindow);
        buttonActions.put("Titles", () -> textArea.setText(showLyricFiles()));
    }

    private String showLyricFiles() {
        String lyricDirPath = "src/main/resources/lyrics";
        File lyricDir = new File(lyricDirPath);
        File[] listOfFiles = lyricDir.listFiles();
        StringBuilder listOfFileName = new StringBuilder();

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
        meaningLabel.setWrapText(true);
        VBox root = new VBox(15, panel, meaningLabel);
        root.setPadding(new Insets(20));
        Stage dialog = new Stage();

        searchButton.setOnAction( e -> {
            String searchText = searchTextField.getText();
            if (searchText.isEmpty()) {
                Platform.runLater(() -> meaningLabel.setText("Input word"));
            } else {
                try {
                    final String SearchedMeaningOfWord = searchMeaningOfWord(searchText);
                    Platform.runLater(() -> meaningLabel.setText(SearchedMeaningOfWord));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Find");
        dialog.setScene(new Scene(root));
        dialog.setWidth(320);
        dialog.setHeight(150);
        dialog.show();
    }

    public String searchMeaningOfWord(String searchText) throws IOException {
        Map<String, String> targetWordFile = readFromFile("src/main/resources/words/" + title + "-words" + ".json", new TypeReference<>() {});
        String meaning = targetWordFile.get(searchText);

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

    public boolean modifyContentOfWordFile(String titleOfSong, String modifedContent) {
        String fileName = titleOfSong + "-words" + ".json";
        File modifyFile = new File("src/main/resources/words/"+ fileName);

        try {
            if (validateJson(modifedContent)) {
                try(FileOutputStream fileOutputStream = new FileOutputStream(modifyFile)) {
                    fileOutputStream.write(modifedContent.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                serviceLyrics.showTextArea(title, type);
                return false;
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    private boolean validateJson(String jsonContent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(jsonContent);
        } catch (JsonParseException | JsonMappingException e) {
            return false;
        }
        return true;
    }

}
