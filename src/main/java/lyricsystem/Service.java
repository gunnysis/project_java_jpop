package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;

import java.io.*;

public class Service {
    Lyric lyric;
    ObjectMapper objectMapper = new ObjectMapper();
    static Translate translate = TranslateOptions.newBuilder().setApiKey(Dotenv.load().get("GOOGLE_API_KEY")).build().getService();
    Main main;

    Service(Main main) {
        this.main = main;
    }

    private String handleLyricType(String serviceType) {

        String result = switch (serviceType) {
            case "japanese" -> lyric.getLyricJapanese();
            case "romaji" -> lyric.getLyricRomaji();
            case "words" -> String.valueOf(new Word(lyric.getTitle()));
            default -> "";
        };
        return result;
    }

    public void showTextArea(String songTitle, String serviceType) {
        songTitle = songTitle.toLowerCase();
        try (InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json")) {
            if (inputStream == null) {
                Platform.runLater(() -> main.describeLabel.setText("No Lyrics JSON file found"));
            }
            lyric = objectMapper.readValue(inputStream, Lyric.class);
            String infoOfTextArea = handleLyricType(serviceType);

            if ("words".equals(serviceType)) {
                Platform.runLater(() -> {
                    main.describeLabel.setText(lyric.getTitle() + " song's word and korean meaning\nIf not exist words file, make words file.");
                    main.describeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
                });
            } else {
                Platform.runLater(() -> main.describeLabel.setText("Artist: " + lyric.getArtist()));
                Platform.runLater(() -> main.describeLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 16px;"));
            }
            Platform.runLater(() -> main.textArea.setText(infoOfTextArea));


        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Not exist this song");
        }
    }

    public boolean uploadLyricFile(File uploadFile) throws FileNotFoundException {
        try (FileInputStream fileInputStream = new FileInputStream(uploadFile)) {
            File targetFile = new File("src/main/resources/lyrics/"+uploadFile.getName());
            byte[] buffer = new byte[4096];
            int bytesRead;
            try(FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer,0,bytesRead);
                }
                Platform.runLater(() -> main.describeLabel.setText("Successfully uploaded "+uploadFile.getName()));
                Platform.runLater(() -> main.describeLabel.setStyle("-fx-text-fill: grey;"));


            }
        } catch (IOException e) {
            Platform.runLater(() -> main.describeLabel.setText("Failed to upload "+uploadFile.getName()));
            Platform.runLater(() -> main.describeLabel.setStyle("-fx-text-fill: red;"));
            e.printStackTrace();
        }


        return true;
    }



}
