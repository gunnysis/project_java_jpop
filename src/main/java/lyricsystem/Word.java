package lyricsystem;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Token;

import static lyricsystem.ServiceLyrics.readFromFile;

@Data
public class Word {
    public String titleOfLyric;
    Map words;
    private Map cachedWords;
    private Map<String, String> translationCache = new HashMap<>();
    Translate translate;
    List<String> termsToTranslate;
    JapaneseTokenizer tokenizer;
    final Pattern SPATIALCHARS_PATTERN = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}\\s]");
    // Initialize the Kuromoji tokenizer
    Tokenizer kuromojiTokenizer = new Tokenizer();


    Word(String title) {
        this.titleOfLyric = title;
    }



    public String getContent() {
        if (cachedWords == null) {
            loadWords();
        }
        StringBuilder result = new StringBuilder();

        cachedWords.forEach((word, meaning) -> result.append(word).append(": ").append(meaning).append("\n"));


        return result.toString();
    }

    public String getContentForRevision() {
        StringBuilder result = new StringBuilder();
        if(cachedWords == null) {
            loadWords();
        }

        result.append("{").append("\n");
        StringJoiner joiner = new StringJoiner(",\n");
        cachedWords.forEach((word, meaning) ->
                joiner.add("  " + "\"" + word + "\": \"" + meaning + "\"")
        );
        result.append(joiner);
        result.append("\n").append("}");

        return result.toString();
    }

    private void loadWords() {
        try {
            cachedWords = (Map) readFromFile("src/main/resources/words/" + this.titleOfLyric + "-words" + ".json", Map.class);
        } catch (IOException e) {
            try {
                extractWords(this.titleOfLyric);
                cachedWords = (Map) readFromFile("src/main/resources/words/" + this.titleOfLyric + "-words" + ".json", Map.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String katakanaToHiragana(String katakana) {
        StringBuilder hiragana = new StringBuilder();
        for (char c : katakana.toCharArray()) {
            // Katakana Unicode range is '゠'(U+30A0) to 'ヿ'(U+30FF)
            // Hiragana Unicode range is U+3040(unassigned code point) to U+309F('ゟ')
            if (c >= '゠' && c <= 'ヿ') {
                hiragana.append((char) (c - 0x60)); // Subtract 96 to change it to the matching Hiragana character if c is in the Katakana range.
            } else {
                hiragana.append(c);
            }
        }
        return hiragana.toString();
    }

    public String convertToHiragana(Tokenizer kuromojiTokenizer, String kanjiText) {
        StringBuilder hiragana = new StringBuilder();

        // Tokenize the input text and convert each token to Hiragana
        for (Token token : kuromojiTokenizer.tokenize(kanjiText)) {
            String reading = token.getReading();
            if (reading != null) {
                hiragana.append(katakanaToHiragana(reading));
            } else {
                // If the token is already in Hiragana or is non-Kanji, add it directly
                hiragana.append(token.getSurface());
            }
        }
        return hiragana.toString();
    }



    private void translateWords() throws IOException {
        termsToTranslate = new ArrayList<>();

        try {
            tokenizer.reset();
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            while (tokenizer.incrementToken()) {
                String term = charTermAttribute.toString();
                if (!SPATIALCHARS_PATTERN.matcher(term).find() && !translationCache.containsKey(term)) {
                    termsToTranslate.add(term);
                }
            }

            tokenizer.end();

            termsToTranslate.parallelStream().forEach(term -> {
                try {
                    translate = TranslateOptions.newBuilder().setCredentials(ServiceAccountCredentials
                                    .fromStream(new FileInputStream("src/main/resources/key/google_translate_key.json")))
                            .build().getService();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // term 변수 값을 히라가나 값으로 변환 후 String hiraganaOfTerm 변수의 변수값으로 할당
                String allHiraganaWord = convertToHiragana(kuromojiTokenizer, term);
                String translated = translate.translate(term, Translate.TranslateOption.targetLanguage("ko")).getTranslatedText();
                String infoOfterm = "[" + allHiraganaWord + "]" + " : " + translated;
                translationCache.put(term, infoOfterm);
            });

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tokenizer.close();
        } /* Translate words to Korean meanings */
    }

    public void extractWords(String songTitle) throws IOException {
        String filePath = "src/main/resources/lyrics/" + songTitle.toLowerCase() + "-lyric" + ".json";
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("No Lyrics JSON file found");
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            Lyric lyric = (Lyric) readFromFile(inputStream, Lyric.class);

            tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL);
            tokenizer.setReader(new StringReader(lyric.getLyricJapanese()));

            translateWords();

            String fileName = String.format("%s-words.json", songTitle);
            Path storeFilePath = Paths.get("src", "main", "resources", "words", fileName).toAbsolutePath();

            try (BufferedWriter fileWriter = Files.newBufferedWriter(storeFilePath)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(translationCache, fileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            } // Write words data to json file
        } // Read lyric file
    }


}