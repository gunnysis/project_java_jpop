package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = Main.class.getResourceAsStream("/lyrics/幻の命.json")) {
            if (inputStream == null) {
                System.out.println("No Lyrics JSON file found");
            }
            Lyric lyric = objectMapper.readValue(inputStream, Lyric.class);
            System.out.print(lyric);

        }catch (Exception e){
            e.printStackTrace();
        }
        // System.out.println("Hello world!");

    }
}