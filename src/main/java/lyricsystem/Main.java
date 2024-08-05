package lyricsystem;


import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Service service = new Service();
        // service.executeSystem();

        service.extractWords("幻の命");
    }
}