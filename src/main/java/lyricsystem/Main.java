package lyricsystem;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        Service service = new Service();
        service.executeSystem();

        // System.out.println((new Lyric()).getExternalLyric("Koi", "星野源"));

        //Translation translation = translate.translate("¡Hola Mundo!");
        //System.out.printf("Translated Text:\n\t%s\n", translation.getTranslatedText());


        

    }
}