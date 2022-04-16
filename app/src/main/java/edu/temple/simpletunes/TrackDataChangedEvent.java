package edu.temple.simpletunes;

import java.util.ArrayList;

/**
 * The TrackDataChangedEvent is used by the event bus communicate with the MainActivity's UI
 * when a new track position or playlist is used.
 */
public class TrackDataChangedEvent {

    private ArrayList<String> trackList = null;
    private final int trackPosition;
    private String singleTrack = null;


    public TrackDataChangedEvent(int trackPosition, ArrayList<String> trackList) {
        this.trackList = trackList;
        this.trackPosition = trackPosition;
    }

    public TrackDataChangedEvent(int trackPosition, String singleTrack) {
        this.singleTrack = singleTrack;
        this.trackPosition = trackPosition;
    }


    /**
     * The SingleTrack method return the current single track that is being played.
     * @return The String containing the file name or null if not playing a folder.
     */
    public String getSingleTrack() {
        return singleTrack;
    }

    /**
     * The getTrackList method return the current names of the audio files in the playlist.
     * @return The arraylist of audio file names or null if single track is being played.
     */
    public ArrayList<String> getTrackList() {
        return trackList;
    }

    /**
     * The getTrackPosition method returns the current track position in the playlist.
     * @return The position of the current track.
     */
    public int getTrackPosition() {
        return trackPosition;
    }

}
