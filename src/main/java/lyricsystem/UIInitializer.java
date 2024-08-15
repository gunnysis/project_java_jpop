package lyricsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * The type Ui initializer.
 */
public class UIInitializer extends Application  {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Button enterButton, outputDefaultJsonFileButton, inputJsonFileButton, modifyContentButton, searchButton;
    Label describeLabel;
    TextArea textArea;
    HBox hbox, hbox2, hbox3;
    VBox vbox;
    Scene scene;
    File defaultLyricFile;
    static Stage stage;


    public void start(Stage stage) {
        UIInitializer.stage = stage;

        initializeComponents();
        configureComponents();
        configureMainLayout();
        initializeButtonHandlers();
    }

    private void initializeComponents() {
        inputBox = new TextField();
        enterButton = new Button("Enter");
        inputJsonFileButton = new Button("Upload Lyric File");
        modifyContentButton = new Button("Modify Content");
        searchButton = new Button("Search Meaning");
        serviceTypeBox = new ComboBox<>();
        outputDefaultJsonFileButton = new Button("Download Default Lyric File");
        describeLabel = new Label();
        textArea = new TextArea();
    }

    private void configureComponents() {
        inputBox.setPromptText("Input Title of jpop song");
        serviceTypeBox.getItems().addAll("japanese", "romaji", "words");
        serviceTypeBox.setValue("japanese");
        describeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        textArea.setEditable(false);
        textArea.setPromptText("Text Area (Read Only)");
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 20px;");
        modifyContentButton.setDisable(true);
    }

    private void configureMainLayout() {
        hbox = new HBox(10, inputBox, serviceTypeBox, enterButton);
        hbox2 = new HBox(10, describeLabel);
        hbox3 = new HBox(10, inputJsonFileButton, outputDefaultJsonFileButton, modifyContentButton, searchButton);
        vbox = new VBox(10, hbox, hbox2, hbox3, textArea);
        vbox.setPadding(new Insets(10));

        HBox.setHgrow(inputBox, Priority.ALWAYS);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        scene = new Scene(vbox, 600, 500);

        stage.setScene(scene);
        stage.setTitle("Lyrics System");
        stage.show();
    }

    private void initializeButtonHandlers() {
        UIEventHandler uiEventHandler = new UIEventHandler(this);
        List<Button> buttons = Arrays.asList(enterButton, outputDefaultJsonFileButton, inputJsonFileButton, modifyContentButton, searchButton);
        uiEventHandler.handleButtonClick(buttons);
    }

}
