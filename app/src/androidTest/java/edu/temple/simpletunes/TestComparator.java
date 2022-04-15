package edu.temple.simpletunes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.provider.Settings;

import androidx.documentfile.provider.DocumentFile;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

@SmallTest
public class TestComparator {

    private MusicTrackComparator mFileComparator;
    MusicTrack[] folder;

    @Before
    public void setUp() {
        mFileComparator = new MusicTrackComparator();
        folder = new MusicTrack[2];
        folder[0] = new MusicTrack(null, DocumentFile.fromFile(new File(Settings.System.DEFAULT_NOTIFICATION_URI.getPath())));
        folder[1] = new MusicTrack(null, DocumentFile.fromFile(new File(Settings.System.DEFAULT_ALARM_ALERT_URI.getPath())));

    }

    @Test
    public void testOrder() {
        String name = folder[1].getName();
        assertEquals("alarm_alert", name);
    }

    @Test
    public void testSortCorrect() {
        Arrays.sort(folder, new MusicTrackComparator());
        String name2 = folder[1].getName();

        assertEquals("notification_sound", name2);
    }


}
