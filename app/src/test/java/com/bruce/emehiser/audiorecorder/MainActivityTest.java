package com.bruce.emehiser.audiorecorder;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ImageButton;

/**
 * Created by Bruce Emehiser on 12/14/2015.
 *
 * Test for PlaybackFragment
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


    private MainActivity mMainActivity;
    private ImageButton mPlayPauseImageButton;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        mMainActivity = getActivity();

        // TODO: set up fragment manager and playback fragment to test clicks

    }

    /**
     * Tests the preconditions of this test fixture.
     */
    @MediumTest
    public void testPreconditions() {
        assertNotNull("mMainActivity is null", mMainActivity);
        assertNotNull("mPlayPauseImageButton is null", mPlayPauseImageButton);
    }

    /**
     * Tests play pause button in Playback Fragment.
     */
    @MediumTest
    public void testPlayPauseImageButton() {

        boolean f = false;

        assertTrue(f);

        assertNotNull(null);

        assertNotNull(new Object());

        // TODO: get the playback fragment and click buttons on it

        // get the play pause button
        ImageButton playPauseButton = (ImageButton) getActivity().findViewById(R.id.playback_play_pause_button);

        // click the button
        playPauseButton.performClick();

        // check to make sure the image changed
        assertEquals(playPauseButton.getTag(), R.drawable.music_play);
    }

}
