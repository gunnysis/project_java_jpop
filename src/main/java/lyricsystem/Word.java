package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Data
public class Word {
    public String titleOfLyric;
    Map<String,String> words;

    Word(String title) {
        this.titleOfLyric = title;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        try {
            words = (new ObjectMapper()).readValue(new File("src/main/resources/words/"+this.titleOfLyric+"_words"+".json"), Map.class);
        } catch (IOException e) {
            System.out.println("Not exist words file");
            throw new RuntimeException(e);
        }

        words.forEach((word, meaning) -> result.append(word).append(": ").append(meaning).append("\n"));
        return result.toString();
    }
}
