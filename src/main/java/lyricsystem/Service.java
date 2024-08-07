package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Service {
    Lyric lyric;
    Word word;
    ObjectMapper objectMapper = new ObjectMapper();
    Translate translate = TranslateOptions.newBuilder().setApiKey(Dotenv.load().get("GOOGLE_API_KEY")).build().getService();
    public static String infoOfService =
            "========================================\n" +
                    "1: search lyrics \n" +
                    "========================================\n";

    public String infoOfLyricsType =
            "========================================\n" +
                    "1: Japanese 2: Romaji 3: Introduce korean meaning of lyric's words\n" +
                    "========================================\n";


    private void displayInfo(String kinds) {
        switch (kinds) {
            case "service":
                System.out.println(infoOfService);
                break;
            case "lyricType":
                System.out.println(infoOfLyricsType);
                break;
        }
    }

    private int getServiceNumber() {
        int serviceNumber = 0;
        displayInfo("service");
        System.out.print("input Service number: ");
        try {
            serviceNumber = new Scanner(System.in).nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Please input only number");
            System.out.print("input Service number: ");
            serviceNumber = (new Scanner(System.in)).nextInt();
        }
        return serviceNumber;
    }

    private int getLyricType() {
        int lyricType = 0;
        displayInfo("lyricType");
        System.out.print("input LyricsType number: ");
        try {
            lyricType = new Scanner(System.in).nextInt();
            if (lyricType > 3) {
                System.out.println("input value is not valid");
                lyricType = new Scanner(System.in).nextInt();
            }
        } catch (InputMismatchException e) {
            System.out.println("Please input only number");
            System.out.print("input Service number: ");
            lyricType = (new Scanner(System.in)).nextInt();
        }
        return lyricType;
    }

    private void handleService() {
        switch (getServiceNumber()) {
            case 1:
                System.out.print("input song name: ");
                String inputTitle = (new Scanner(System.in).nextLine()).trim();
                searchLyric(inputTitle);
                break;

        }
    }

    private void handleLyricType() {
        switch (getLyricType()) {
            case 1:
                System.out.println(lyric.getLyricJapanese());
                break;
            case 2:
                System.out.println(lyric.getLyricRomaji());
                break;
            case 3:
                System.out.println(new Word(lyric.getTitle()));
                break;
            default:
                break;
        }
    }


    public void executeSystem() {
        while (true) {
            handleService();

        }
    }

    private void searchLyric(String songTitle) {
        try (InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json")) {
            if (inputStream == null) {
                System.out.println("No Lyrics JSON file found");
            }
            lyric = objectMapper.readValue(inputStream, Lyric.class);

            displayInfo("LyricsType");
            handleLyricType();

        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Not exist this song");
        }

    }

    public void extractWords(String songTitle) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json");
        Lyric lyric = objectMapper.readValue(inputStream, Lyric.class);

        JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL);
        tokenizer.setReader(new StringReader(lyric.getLyricJapanese()));

        // List<String> words = new ArrayList<>();
        HashMap<String,String> words = new HashMap<>();

        try {
            tokenizer.reset();
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            while (tokenizer.incrementToken()) {
                if (!Arrays.asList("\n","「","」"," ","\n\n").contains(charTermAttribute.toString())) {
                    words.put(charTermAttribute.toString(),translate.translate(charTermAttribute.toString(), Translate.TranslateOption.targetLanguage("ko")).getTranslatedText());
                }
            }

            tokenizer.end();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tokenizer.close();
        }

        File file = new File( "src/main/resources/words/" + songTitle + "_words" + ".json");
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
