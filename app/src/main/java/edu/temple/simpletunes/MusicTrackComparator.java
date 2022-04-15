package edu.temple.simpletunes;

import java.util.Comparator;

public class MusicTrackComparator implements Comparator<MusicTrack> {
    public MusicTrackComparator(){
    }
    @Override
    public int compare(MusicTrack t1, MusicTrack t2) {
        String s1 = t1.getTrack();
        String s2 = t2.getTrack();
        if(s1 != null && s2 != null){
            return s1.compareTo(s2);
        }else{
            s1 = t1.getName();
            s2 = t2.getName();
            if(s1 != null && s2 != null){
                return s1.compareTo(s2);
            }else{
                return 0;
            }
        }
    }
}
