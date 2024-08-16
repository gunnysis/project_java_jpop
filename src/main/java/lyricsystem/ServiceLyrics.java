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

                jsonObject = manualJsonFix(jsonContent.toString());
            }

            String fileName = uploadFile.getName();
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
            File destinationFile = new File("src/main/resources/lyrics/" + fileNameWithoutExtension + "-lyric" + ".json");

            try (FileWriter fileWriter = new FileWriter(destinationFile)) {
                if (isJsonFixed) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(jsonObject, fileWriter);
                } else {
                    fileWriter.write(jsonContent.toString());
                }
                uiInitializer.describeLabel.setText(isJsonFixed ? "Successfully uploaded and fixed " + uploadFile.getName() : "Successfully uploaded " + uploadFile.getName());
                uiInitializer.describeLabel.setStyle("-fx-text-fill: grey;");
            } catch (FileNotFoundException e) {
                uiInitializer.describeLabel.setText("File not found");
                uiInitializer.describeLabel.setStyle("-fx-text-fill: red;");
                System.out.println("An FileNotFound error occurred: " + e.getMessage());
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
        int startIndex = jsonString.indexOf("\"" + fieldName + "\":") + fieldName.length() + 3; // "title": "r -> r's index

        int endIndex = getEndIndex(jsonString, startIndex);

        String value = jsonString.substring(startIndex, endIndex).trim(); // startIndex ~ endIndex -1
        return (value.startsWith("\"")) ? value.substring(1) : value;
    }

    private static int getEndIndex(String jsonString, int startIndex) {
        int endIndex = jsonString.indexOf("\",", startIndex); // Ex. "artist": "SEKAI NO OWARI",

        // The reason the endIndex == -1 condition is repeated is to handle the next possible scenario
        // when endIndex wasn't found in the previous step (i.e., when it's -1).
        if (endIndex == -1) {
            endIndex = jsonString.indexOf("\"\n", startIndex); // Ex. arigatou gozaimazu"
        }

        if (endIndex == -1) {
            endIndex = jsonString.length() - 1; // Ex. }
        }
        return endIndex;
    }


}
