package edu.temple.simpletunes;

import android.util.Log;

import java.util.Comparator;
import java.util.Scanner;

public class MusicTrackComparator implements Comparator<MusicTrack> {
    public MusicTrackComparator(){
    }
    @Override
    public int compare(MusicTrack t1, MusicTrack t2) {
        String s1 = t1.getTrack();
        String s2 = t2.getTrack();
        if(s1 != null && s2 != null){
            Scanner one = new Scanner(s1);
            int i1;
            if(one.hasNextInt()){
                i1 = one.nextInt();
                Scanner two = new Scanner(s2);
                int i2;
                if(two.hasNextInt()){
                    i2 = two.nextInt();
                    return Integer.compare(i1, i2);
                }
            }
        }
        s1 = t1.getName();
        s2 = t2.getName();
        if(s1 != null && s2 != null){
            return s1.compareTo(s2);
        }else{
            return 0;
        }
    }
}
