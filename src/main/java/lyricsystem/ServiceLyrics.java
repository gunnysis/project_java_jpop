package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ServiceLyrics {
    Lyric lyric;
    UIInitializer uiInitializer;

    ServiceLyrics(UIInitializer uiInitializer) {
        this.uiInitializer = uiInitializer;
    }

    /**
     * Read from file object.
     *
     * @param <T>       the type parameter
     * @param source    the source
     * @param classType the class type
     * @return the object
     * @throws IOException the io exception
     */
    public static <T> Object readFromFile(Object source, Class<T> classType) throws IOException {
        if (source instanceof InputStream) {
            return new ObjectMapper().readValue((InputStream) source, classType);
        } else if (source instanceof String) {
            return new ObjectMapper().readValue(new File((String) source), classType);
        } else {
            throw new IllegalArgumentException("Unsupported source type: " + source.getClass().getName());
        }
    }

    private String handleLyricType(String serviceType) {
        return switch (serviceType) {
            case "japanese" -> lyric.getLyricJapanese();
            case "romaji" -> lyric.getLyricRomaji();
            case "words" -> String.valueOf(new Word(lyric.getTitle()).getContentForRevision());
            default -> "";
        };
    }

    public void showTextArea(String songTitle, String serviceType) {
        String filePath = "src/main/resources/lyrics/" + songTitle.toLowerCase() + "-lyric" + ".json";  // 절대 경로로 수정

        File file = new File(filePath);
        if (!file.exists()) {
            uiInitializer.describeLabel.setText("No Lyrics JSON file found ");
            return;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            lyric = (Lyric) readFromFile(inputStream, Lyric.class);
            String infoOfTextArea = handleLyricType(serviceType);

            if ("words".equals(serviceType)) {
                uiInitializer.describeLabel.setText(lyric.getTitle() + " song's word and Korean meaning If not exist words file, make words file.");
                uiInitializer.describeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
            } else {
                uiInitializer.describeLabel.setText("Artist: " + lyric.getArtist());
                uiInitializer.describeLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 16px;");
            }

            uiInitializer.textArea.setText(infoOfTextArea);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
            uiInitializer.describeLabel.setText("An error occurred while processing the file.");
        }
    }



    public void uploadLyricFile(File uploadFile) throws FileNotFoundException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(uploadFile))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }

            JsonObject jsonObject = null;
            boolean isJsonFixed = false;

            try {
                jsonObject = JsonParser.parseString(jsonContent.toString()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                isJsonFixed = true;

                uiInitializer.describeLabel.setText("Invalid JSON format detected. Attempting to fix...");
                uiInitializer.describeLabel.setStyle("-fx-text-fill: orange;");
                System.out.println("This file is not a valid JSON file");

                // 유효하지 않은 JSON 데이터를 수동으로 수정하여 재구성
                jsonObject = manualJsonFix(jsonContent.toString());
            }

            // JSON 파일 저장 처리
            String fileName = uploadFile.getName();
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
            File destinationFile = new File("src/main/resources/lyrics/" + fileNameWithoutExtension + "-lyric" + ".json");

            try (FileWriter fileWriter = new FileWriter(destinationFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(jsonObject, fileWriter);
                uiInitializer.describeLabel.setText(isJsonFixed ? "Successfully uploaded and fixed " + uploadFile.getName() : "Successfully uploaded " + uploadFile.getName());
                uiInitializer.describeLabel.setStyle("-fx-text-fill: grey;");
            }

        } catch (IOException e) {
            uiInitializer.describeLabel.setText("Failed to upload " + uploadFile.getName());
            uiInitializer.describeLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private JsonObject manualJsonFix(String jsonString) {
        JsonObject jsonObject = new JsonObject();

        try {
            String title = extractFieldValue(jsonString, "title");
            String artist = extractFieldValue(jsonString, "artist");
            String lyricJapanese = extractFieldValue(jsonString, "lyricJapanese");
            String lyricRomaji = extractFieldValue(jsonString, "lyricRomaji");

            // 추출한 값을 JSON 객체에 추가
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("artist", artist);
            jsonObject.addProperty("lyricJapanese", lyricJapanese);
            jsonObject.addProperty("lyricRomaji", lyricRomaji);

        } catch (Exception e) {
            e.printStackTrace();
            uiInitializer.describeLabel.setText("Failed to fix the invalid JSON format.");
            uiInitializer.describeLabel.setStyle("-fx-text-fill: red;");
        }

        return jsonObject;
    }

    private String extractFieldValue(String jsonString, String fieldName) {
        int startIndex = jsonString.indexOf("\"" + fieldName + "\":") + fieldName.length() + 3;
        int endIndex = jsonString.indexOf("\",", startIndex);

        if (endIndex == -1) {
            endIndex = jsonString.indexOf("\"\n", startIndex);
        }

        if (endIndex == -1) {
            endIndex = jsonString.length() - 1;
        }

        String value = jsonString.substring(startIndex, endIndex).trim();

        // 앞에만 추가된 불필요한 따옴표(")를 제거
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }

        return value;
    }



}
