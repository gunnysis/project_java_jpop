package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static lyricsystem.ServiceLyrics.readFromFile;
import static lyricsystem.ServiceLyrics.translate;


@Data
public class Word {
    public String titleOfLyric;
    Map words;
    Main main;
    private Map cachedWords;
    ObjectMapper objectMapper = new ObjectMapper();


    Word(String title) {
        this.titleOfLyric = title;
    }

    @Override
    public String toString() {
        if (cachedWords == null) {
            loadWords();
        }
        StringBuilder result = new StringBuilder();
        cachedWords.forEach((word, meaning) -> result.append(word).append(": ").append(meaning).append("\n"));
        return result.toString();
    }

    private void loadWords() {
        try {
            cachedWords = (Map) readFromFile("src/main/resources/words/"+this.titleOfLyric+"-words"+".json", Map.class);
        } catch (IOException e) {
            try {
                extractWords(this.titleOfLyric);
                cachedWords = (Map) readFromFile("src/main/resources/words/"+this.titleOfLyric+"-words"+".json", Map.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void extractWords(String songTitle) throws IOException {
        InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json");
        // Lyric lyric = objectMapper.readValue(inputStream, Lyric.class);
        Lyric lyric = (Lyric) readFromFile(inputStream, Lyric.class);

                JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL);
        tokenizer.setReader(new StringReader(lyric.getLyricJapanese()));

        HashMap<String,String> words = new HashMap<>();
        Map<String, String> translationCache = new HashMap<>();
        Pattern spetialCharsPattern = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}\\s]");

        List<String> termsToTranslate = new ArrayList<>();

        try {
            tokenizer.reset();
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            while (tokenizer.incrementToken()) {
                String term = charTermAttribute.toString();
                if (!spetialCharsPattern.matcher(term).find() && !translationCache.containsKey(term)) {
                    termsToTranslate.add(term);
                }
            }

            tokenizer.end();

            termsToTranslate.parallelStream().forEach(term -> {
                String translated = translate.translate(term, Translate.TranslateOption.targetLanguage("ko")).getTranslatedText();
                translationCache.put(term, translated);
                words.put(term, translated);
            });

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tokenizer.close();
        }

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter((new File( "src/main/resources/words/" + songTitle + "-words" + ".json")).getAbsoluteFile()))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(words, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}