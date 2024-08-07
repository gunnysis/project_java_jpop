package lyricsystem;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.TrackData;


@Data
public class Lyric {
    private String title;
    private String artist;
    private String lyricJapanese;
    private String lyricRomaji;
    private String apiKeyOfMusixMatch = Dotenv.load().get("MUSIXMATCH_API_KEY");
    MusixMatch musixMatch = new MusixMatch(apiKeyOfMusixMatch);
    TrackData trackData;
    Lyrics respondLyric;

    @Override
    public String toString() {
        return "title: "+title+"\nartist: "+artist+"\n"+lyricJapanese+"\nlyricRomaji: "+lyricRomaji;
    }

    public StringBuffer getExternalLyric(String title, String artist) {
        try {
            trackData = musixMatch.getMatchingTrack(title,artist).getTrack();
            respondLyric = musixMatch.getLyrics(trackData.getTrackId());
        } catch (MusixMatchException e) {
            System.out.println("this song doesn;t exist on MusixMatch");
            return null;
        }
        return new StringBuffer(respondLyric.getLyricsBody());
    }



}
