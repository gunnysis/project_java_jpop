package lyricsystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;



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
        String result = switch (serviceType) {
            case "japanese" -> lyric.getLyricJapanese();
            case "romaji" -> lyric.getLyricRomaji();
            case "words" -> String.valueOf(new Word(lyric.getTitle()));
            default -> "";
        };
        return result;
    }

    public void showTextArea(String songTitle, String serviceType) {
        String filePath = "src/main/resources/lyrics/" + songTitle.toLowerCase() + ".json";  // 절대 경로로 수정

        File file = new File(filePath);
        if (!file.exists()) {
            uiInitializer.describeLabel.setText("No Lyrics JSON file found");
            return;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            lyric = (Lyric) readFromFile(inputStream, Lyric.class);
            String infoOfTextArea = handleLyricType(serviceType);

            if ("words".equals(serviceType)) {
                uiInitializer.describeLabel.setText(lyric.getTitle() + " song's word and Korean meaning\nIf not exist words file, make words file.");
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
        try (FileInputStream fileInputStream = new FileInputStream(uploadFile)) {
            File destinationFile = new File("src/main/resources/lyrics/"+uploadFile.getName());
            byte[] buffer = new byte[4096];

            try(FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
                for (int bytesRead; (bytesRead = fileInputStream.read(buffer)) != -1;) {
                    fileOutputStream.write(buffer,0,bytesRead);
                }
                uiInitializer.describeLabel.setText("Successfully uploaded "+uploadFile.getName());
                uiInitializer.describeLabel.setStyle("-fx-text-fill: grey;");
            }
        } catch (IOException e) {
            uiInitializer.describeLabel.setText("Failed to upload "+uploadFile.getName());
            uiInitializer.describeLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

}
