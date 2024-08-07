package lyricsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    TextField inputBox;
    ComboBox<String> serviceTypeBox;
    Button enterButton;
    Label errorLabel;
    TextArea textArea;
    HBox hbox;
    VBox vbox;
    Scene scene;

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

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        textArea = new TextArea();
//        textArea.setEditable(false);
        textArea.setPromptText("Text Area (Read Only)");

        hbox = new HBox(10, inputBox, serviceTypeBox, enterButton);
        hbox.setPadding(new Insets(10));
        hbox.setHgrow(inputBox, Priority.ALWAYS);

        vbox = new VBox(10, hbox, errorLabel, textArea);
        vbox.setPadding(new Insets(10));
        vbox.setVgrow(textArea, Priority.ALWAYS);

        scene = new Scene(vbox, 600, 400); // 적절한 크기로 설정

        primaryStage.setScene(scene);
        primaryStage.setTitle("Lyrics System");
        primaryStage.show();

        Service service = new Service(this);

        enterButton.setOnAction(e -> {
            String title = inputBox.getText();
            String type = serviceTypeBox.getValue();
            if (title == null || title.isEmpty()) {
                errorLabel.setText("Input Title of jpop song");
            } else {
                service.searchLyric(title, type);
            }
        });
    }

    public static void main(String[] args) {
         launch(args);
    }

}