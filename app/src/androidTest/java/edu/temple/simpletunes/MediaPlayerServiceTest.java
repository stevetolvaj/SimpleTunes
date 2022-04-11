package edu.temple.simpletunes;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;

import androidx.documentfile.provider.DocumentFile;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ServiceTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


// Referenced from https://developer.android.com/training/testing/other-components/services
@MediumTest
@RunWith(AndroidJUnit4.class)
public class MediaPlayerServiceTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    IBinder binder;
    MediaPlayerService service;

    @Before
    public void setService() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(ApplicationProvider.getApplicationContext(),
                        MediaPlayerService.class);

        serviceIntent.putExtra("Test", "Test message in notification");

        // Bind the service and grab a reference to the binder.
        binder = serviceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        service =
                ((MediaPlayerService.ControlsBinder) binder).getService();

    }

    @Test
    public void testIsPlayingFalse() {
        assertFalse(((MediaPlayerService.ControlsBinder) binder).isPlaying());
    }

    @Test
    public void testRepeatStatusSingleTrack() {
        int status = ((MediaPlayerService.ControlsBinder) binder).repeat();

        assertEquals(2, status);

    }
//    @Test
//    public void testRepeatStatusFolder() {
//        DocumentFile[] folder = new DocumentFile[2];
//        folder[0] = DocumentFile.fromFile(new File(Settings.System.DEFAULT_ALARM_ALERT_URI.getPath()));
//        folder[1] = DocumentFile.fromFile(new File(Settings.System.DEFAULT_ALARM_ALERT_URI.getPath()));
//        ((MediaPlayerService.ControlsBinder) binder).playFolder(folder);
//
//        int status = ((MediaPlayerService.ControlsBinder) binder).repeat();
//
//        assertEquals("it does this", 1, status);
//    }


}
