
package edu.temple.simpletunes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertNotNull;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class UITest {
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    @Test
    public void RecyclerViewTest() {
        assertNotNull(onView(withId(R.id.recyclerView)));
    }

    @Test
    public void PlayButtonToPauseTest() {
        ViewInteraction playPause = onView(withId(R.id.playPauseButton));
        assertNotNull(playPause);
    }

    @Test
    public void BrowserButtonTest() {
        ViewInteraction browser = onView(withId(R.id.browserButton));
        assertNotNull(browser);
    }

    @Test
    public void LibraryButtonTest() {
        ViewInteraction library = onView(withId(R.id.libraryButton));
        assertNotNull(library);
    }

    @Test
    public void SkipNextButtonTest() {
        ViewInteraction skipNext = onView(withId(R.id.skipNextButton));
        assertNotNull(skipNext);
    }

    @Test
    public void SkipPrevButtonTest() {
        ViewInteraction skipPrev = onView(withId(R.id.skipPrevButton));
        assertNotNull(skipPrev);
    }

    @Test
    public void RepeatButtonTest() {
        ViewInteraction repeat = onView(withId(R.id.repeatButton));
        assertNotNull(repeat);
    }

    @Test
    public void ShuffleButtonTest() {
        ViewInteraction shuffle = onView(withId(R.id.shuffleButton));
        assertNotNull(shuffle);
    }


}
