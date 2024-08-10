package lyricsystem;

import lombok.Data;


@Data
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
