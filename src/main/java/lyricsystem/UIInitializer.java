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

public class UIInitializer extends Application  {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Button enterButton, outputDefaultJsonFile, inputJsonFile;
    Label describeLabel;
    TextArea textArea;
    HBox hbox, hbox2;
    VBox vbox;
    Scene scene;
    // Service service = new Service(this);
    File defaultLyricFile;
    static Stage stage;


    public void start(Stage stage) {
        UIInitializer.stage = stage;

        inputBox = new TextField();
        inputBox.setPromptText("Input Title of jpop song");
        serviceTypeBox = new ComboBox<>();
        serviceTypeBox.getItems().addAll("japanese", "romaji", "words");
        serviceTypeBox.setValue("japanese");

        enterButton = new Button("Enter");
        outputDefaultJsonFile = new Button("Download Default Lyric File");
        inputJsonFile = new Button("Input Lyric File");



        describeLabel = new Label();
        describeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPromptText("Text Area (Read Only)");

        hbox = new HBox(10, inputBox, serviceTypeBox, enterButton);
        hbox2 = new HBox(10, describeLabel, new HBox(), inputJsonFile, outputDefaultJsonFile);

        HBox.setHgrow(inputBox, Priority.ALWAYS);
        HBox.setHgrow(hbox2.getChildren().get(1), Priority.ALWAYS);

        vbox = new VBox(10, hbox, hbox2, textArea);
        vbox.setPadding(new Insets(10));

        VBox.setVgrow(textArea, Priority.ALWAYS);

        scene = new Scene(vbox, 600, 400);

        UIEventHandler uiEventHandler = new UIEventHandler(this);
        List<Button> buttons = Arrays.asList(enterButton, outputDefaultJsonFile, inputJsonFile);
        uiEventHandler.handleButtonClick(buttons);

        stage.setScene(scene);
        stage.setTitle("Lyrics System");
        stage.show();
    }


}
