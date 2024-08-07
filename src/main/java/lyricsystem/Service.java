package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;

public class Service {
    Lyric lyric;
    Word word;
    ObjectMapper objectMapper = new ObjectMapper();
    static Translate translate = TranslateOptions.newBuilder().setApiKey(Dotenv.load().get("GOOGLE_API_KEY")).build().getService();
    Main main;

    Service(Main main) {
        this.main = main;
    }

    private String handleLyricType(String serviceType) {
        String result = "";
        switch (serviceType) {
            case "japanese":
                result = lyric.getLyricJapanese();
                break;
            case "romaji":
                result = lyric.getLyricRomaji();
                break;
            case "words":
                result = String.valueOf(new Word(lyric.getTitle()));
                break;
        }
        return result;
    }

    public void searchLyric(String songTitle, String serviceType) {
        songTitle = songTitle.toLowerCase();
        try (InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json")) {
            if (inputStream == null) {
                Platform.runLater(() -> main.errorLabel.setText("No Lyrics JSON file found"));
            }
            lyric = objectMapper.readValue(inputStream, Lyric.class);

            Platform.runLater(() -> main.textArea.setText(handleLyricType(serviceType)));
            Platform.runLater(() -> main.errorLabel.setText(lyric.getArtist()));
            Platform.runLater(() -> main.errorLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 16px;"));
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Not exist this song");
        }

    }



}
