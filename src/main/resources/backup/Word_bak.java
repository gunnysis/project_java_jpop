package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import lombok.Data;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static lyricsystem.Service.translate;

@Data
public class Word_bak {
    private String titleOfLyric;
    Map words;

    Word_bak(String title) {
        this.titleOfLyric = title;
    }

    @Override
    public String toString() {
        Main main = new Main();
        StringBuilder result = new StringBuilder();


        try {
            words = (new ObjectMapper()).readValue(new File("src/main/resources/words/"+this.titleOfLyric+"_words"+".json"), Map.class);
        } catch (IOException e) {
            try {

                extractWords(this.titleOfLyric);
                words = (new ObjectMapper()).readValue(new File("src/main/resources/words/"+this.titleOfLyric+"_words"+".json"), Map.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        Platform.runLater(() -> main.describeLabel.setText("If not exist words file, make words file"));
        words.forEach((word, meaning) -> result.append(word).append(": ").append(meaning).append("\n"));
        return result.toString();
    }

    public void extractWords(String songTitle) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json");
        Lyric lyric = objectMapper.readValue(inputStream, Lyric.class);

        JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL);
        tokenizer.setReader(new StringReader(lyric.getLyricJapanese()));

        HashMap<String,String> words = new HashMap<>();

        try {
            tokenizer.reset();
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            while (tokenizer.incrementToken()) {
                if (!Pattern.compile("[\\p{Punct}\\p{IsPunctuation}\\s]").matcher(charTermAttribute.toString()).find()) {
                    words.put(charTermAttribute.toString(),translate.translate(charTermAttribute.toString(), Translate.TranslateOption.targetLanguage("ko")).getTranslatedText());
                }
            }

            tokenizer.end();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tokenizer.close();
        }

        File file = new File( "src/main/resources/words/" + songTitle + "-words" + ".json");
        FileWriter fileWriter = null;
        Gson gson;
        if (!file.exists()) {
            try {
                file.createNewFile();
                fileWriter = new FileWriter(file.getAbsoluteFile());
                // making json format
                gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(words, fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        }
    }

}
