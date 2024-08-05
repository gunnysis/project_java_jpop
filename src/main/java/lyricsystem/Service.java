package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Service {
    Lyric lyric;
    ObjectMapper objectMapper = new ObjectMapper();
    public static String infoOfService =
            "========================================\n" +
                    "1: search lyrics \n" +
                    "========================================\n";

    public String infoOfLyricsType =
            "========================================\n" +
                    "1: Japanese 2: Romaji \n" +
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
            if (lyricType > 2) {
                System.out.println("Lyric type not valid");
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

        } catch (IllegalArgumentException | java.io.IOException e) {
            System.out.println("Not exist this song");
        }

    }

    public void extractWords(String songTitle) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getResourceAsStream("/lyrics/"+songTitle+".json");
        Lyric lyric = objectMapper.readValue(inputStream, Lyric.class);

        JapaneseTokenizer tokenizer = new JapaneseTokenizer(null, false, JapaneseTokenizer.Mode.NORMAL);
        tokenizer.setReader(new StringReader(lyric.getLyricJapanese()));

        List<String> words = new ArrayList<>();

        try {
            tokenizer.reset();
            CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

            while (tokenizer.incrementToken()) {
                if (!charTermAttribute.toString().equals("\n") && !charTermAttribute.toString().equals(" ")  && !charTermAttribute.toString().equals("\n\n")) {
                    words.add(charTermAttribute.toString());
                }
            }

            tokenizer.end();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tokenizer.close();
        }

        File file = new File( "src/main/resources/words/" + songTitle + "_words" + ".json");
        if (!file.exists()) {
            file.createNewFile();
            objectMapper.writeValue(new File("src/main/resources/words/" + songTitle + "_words" + ".json"), words);
        }
    }
}
