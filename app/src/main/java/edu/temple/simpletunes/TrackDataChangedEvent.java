package edu.temple.simpletunes;

import java.util.ArrayList;

/**
 * The TrackDataChangedEvent is used by the event bus communicate with the MainActivity's UI
 * when a new track position or playlist is used. EventBus sourced from
 * https://github.com/greenrobot/EventBus
 */
public class TrackDataChangedEvent {

    /**
     * The current trackList.
     */
    private ArrayList<String> trackList = null;
    /**
     * The current position of the track being played.
     */
    private final int trackPosition;
    /**
     * The name of a single track being played.
     */
    private String singleTrack = null;


    /**
     * The trackDataChangedEvent method is used for signaling that the playlist has changed or
     * the current track position has changed.
     * @param trackPosition The current position of the track.
     * @param trackList The new playlist to show.
     */
    public TrackDataChangedEvent(int trackPosition, ArrayList<String> trackList) {
        this.trackList = trackList;
        this.trackPosition = trackPosition;
    }

    /**
     * The TrackDataChangedEvent is used to signal that a new single track is being played.
     * @param trackPosition The position of the single track, usually 0.
     * @param singleTrack The name of the single track.
     */
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
