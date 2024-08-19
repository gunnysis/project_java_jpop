package lyricsystem;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class Lyric {
    private String title;
    private String artist;
    private String lyricJapanese;
    private String lyricRomaji;

    @Override
    public String toString() {
        return "title: "+title+"\nartist: "+artist+"\n"+lyricJapanese+"\nlyricRomaji: "+lyricRomaji;
    }
}
