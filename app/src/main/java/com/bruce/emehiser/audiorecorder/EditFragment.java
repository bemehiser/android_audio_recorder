package com.bruce.emehiser.audiorecorder;


import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * Edit {@link Fragment} for editing raw audio files
 * @author Bruce Emehiser
 */
public class EditFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditFragment";
    private static final int SHOW_PROGRESS = 42;
    public static final String TEMP_FILE = "temp.mp3";


    /**
     * User Interface View Elements
     */
    private ListView mTracksListView;
    private ArrayAdapter mTracksListAdapter;
    private ArrayList<EditTrack> mEditTracks;
    private SeekBar mSeekBar;
    private ImageButton mPlayPauseButton;
    private Button mTrackButton;
    private Button mExportButton;

    /**
     * File manager
     */
    private FileManager mFileManager;

    /**
     * Current raw sound File which is being edited
     */
    private File mWaveFile;

    /**
     * Input stream to read data from for playback and editing
     */
    private InputStream mWaveInputStream;

    /**
     * Current Media Player for playing the raw audio
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Handler to post updates for the seek bar when media is playing
     */
    private Handler mHandler;

    /**
     * Boolean representing the current STATE of the playback
     * Boolean representing the current STATE of the pause
     * Int representing the total length of the file in bytes
     * Int representing the current position in bytes
     */
    private boolean mPlaying;
    private boolean mPaused;
    private int mLength;
    private int mPosition;

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    @SuppressWarnings("HandlerLeak")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        // get ui elements from the view
        mTracksListView = (ListView) view.findViewById(R.id.edit_cuts_list_view);
        mSeekBar = (SeekBar) view.findViewById(R.id.edit_seek_bar);
        mPlayPauseButton = (ImageButton) view.findViewById(R.id.edit_play_pause_button);
        mTrackButton = (Button) view.findViewById(R.id.edit_new_cut_button);
        mExportButton = (Button) view.findViewById(R.id.edit_export_button);

        // set on click listeners
        mTrackButton.setOnClickListener(this);
        mPlayPauseButton.setOnClickListener(this);
        mExportButton.setOnClickListener(this);

        // set up list adapter for cuts list view
        mEditTracks = new ArrayList<>();
        mTracksListAdapter = new EditTrackAdapter(getActivity(), mEditTracks);
//        mTracksListAdapter = new ArrayAdapter<Track>(getActivity(), R.layout.edit_track, android.R.id.text1, mEditTracks);
        mTracksListView.setAdapter(mTracksListAdapter);

        // instantiate file manager
        mFileManager = new FileManager();

        // create the media player
        mMediaPlayer = new MediaPlayer();

        try {
            mWaveFile = mFileManager.getFile(TEMP_FILE);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to find file");
        }

        return view;
    }

    /**
     * Create a cut at the current position of the seek bar
     * If the current Track only has a start time, new cut will
     * be added as the ending time of the track
     * Cut must be at a greater time than all previous cuts
     * @param timeInMills Time to make cut
     */
    private void cut(int timeInMills) {

        if(timeInMills < 0) {
            throw new InvalidParameterException("Track time must be greater than zero");
        }

        // check current tracks to see if one has a start time but no end time
        // if we have tracks
        if(mEditTracks.size() > 0) {
            // get the last one to check
            EditTrack track = mEditTracks.get(mEditTracks.size() - 1);
            // we assume that time 0 is incomplete
            if(track.getEndTime() == 0) {
                // check end time against start time
                if(track.getStartTime() >= timeInMills) {
                    // complain
                    Toast.makeText(getActivity(), "Track end time must be greater than track start time", Toast.LENGTH_SHORT).show();
                    // done with cut
                    return;
                }
                else {
                    // set the end time
                    track.setEndPosition(timeInMills);
                    // done with cut
                    return;
                }
            }
        }
        // else we do not have tracks, or our last track is complete
        // create a new track
        EditTrack editTrack = new EditTrack();
        editTrack.setLength(mMediaPlayer.getDuration());
        editTrack.setStartPosition(timeInMills);
        // add track to list of tracks
        mEditTracks.add(editTrack);
        // notify user
        Toast.makeText(getActivity(), "Track Created Successfully", Toast.LENGTH_SHORT).show();
    }

    private void export() {

        // load entire song from disk
        File inputFile;
        FileInputStream fileInputStream;
        try {
            inputFile = mFileManager.getFile("temp.mp3");
            fileInputStream = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
            Toast.makeText(getActivity(), "Unable to export file. Temp file not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(inputFile.length() > Integer.MAX_VALUE) {
            Toast.makeText(getActivity(), "File too large. Cannot be greater than 2 GB", Toast.LENGTH_SHORT).show();
            return;
        }
        int size = (int) inputFile.length(); // read the size of the input file
        byte[] song = new byte[size]; // this will fail if file is larger than 2 GB because of cast to int

        // read the file to the byte array
        try {
            fileInputStream.read(song, 0, size);
        } catch (IOException e) {
            Log.e(TAG, "Error reading file");
            Log.e(TAG, e.toString());
            return;
        }

        // for every track
        for(EditTrack editTrack : mEditTracks) {

            // edit song
            byte[] editedSong = AudioEditor.edit(song, editTrack);

            // write to file
            File outFile = mFileManager.getOutputFile(editTrack.getTitle() + System.currentTimeMillis() + ".mp3");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                fileOutputStream.write(editedSong);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error opening output file");
            } catch (IOException e) {
                Log.e(TAG, "Error writing output file");
            }
        }


//        // read the first frame
//        byte[] frameData = new byte[4];
//
//        for(int i = 0; i < 4; i ++) {
//            frameData[i] = song[i];
//        }
//
//        // padding size 1 byte for layer 3, and 4 bits for layer 1 and 2
//
//        // read header data
//        int layer = (frameData[1] >> 1) & 3; // layer I, II, or III
//        int bitRateIndex = (frameData[2] >> 4) & 15; // bitrate index
//        int sampleRate = (frameData[2] >> 2) & 3; // sampling rate index
//        int padding = (frameData[2] >> 1) & 1; // 0 if no padding
//
//        int[] bitRates = new int[] {}
//
//        int frameSizeBytes = 144 * bitRate / sampleRate + padding;
    }

    /**
     * Starts playback of the current track
     */
    public void startPlayback() {

        // if media player is null
        if(mMediaPlayer == null) {
            // instantiate new player
            mMediaPlayer = new MediaPlayer();
        }
        else {
            // stop player
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mPaused = false;
            }
            // reset player
            mMediaPlayer.reset();
        }

        try {
            // set data source
            mMediaPlayer.setDataSource(getActivity(), Uri.fromFile(mWaveFile));

            // prepare media player
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // start player
                    mediaPlayer.start();
                    // update state
                    mPlaying = true;
                    // start updating seek bar
                    startHandler();
                    // update
                    mLength = mMediaPlayer.getDuration();
                    // set the seek bar max length
                    mSeekBar.setMax(mLength);
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // player is now stopped
                    stopPlayback();
                }
            });
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found " + TEMP_FILE);
        } catch (IOException e) {
            Log.e(TAG, "IOException reading " + TEMP_FILE);
        }
        // media player is now playing
        // change button image
        mPlayPauseButton.setImageResource(R.drawable.music_pause);
    }

    /**
     * Pause media player
     */
    public void pausePlayback() {

        // check state
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            // pause
            mMediaPlayer.pause();
            // update state
            mPlaying = false;
            mPaused = true;
            // stop handler
            mHandler.removeMessages(SHOW_PROGRESS);
            mHandler = null;
            // update button
            mPlayPauseButton.setImageResource(R.drawable.music_play);
        }
    }

    /**
     * Resume playback of paused track
     * If no track is paused, nothing will happen
     */
    public void resumePlayback() {

        if(mMediaPlayer != null && mPaused) {
            // start playback
            mMediaPlayer.start();
            // start handler
            startHandler();
            // update state
            mPaused = false;
            mPlaying = true;
            // update button image
            mPlayPauseButton.setImageResource(R.drawable.music_pause);
        }
        else if(mPaused) {
            mPaused = false;
        }

    }

    /**
     * Stops playback of the current track
     */
    public void stopPlayback() {

        if(mMediaPlayer != null) {
            // stop player
            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            // release player
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // update state
        mPlaying = false;
        mPaused = false;

        // change button image
        mPlayPauseButton.setImageResource(R.drawable.music_play);
    }

    /**
     * Handler which updates the seek bar
     */
    @SuppressWarnings("HandlerLeak")
    private void startHandler() {

        // check for null handler
        if(mHandler == null) {
            // set up handler
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(mPlaying) {
                        mPosition = mMediaPlayer.getCurrentPosition();
                        // update seek bar and text view
                        mSeekBar.setProgress(mPosition);
//                    mCurrentPositionText.setText(timeToHourMinuteSecond(mPosition));
                        // clear and set next message and delay
                        msg = obtainMessage(SHOW_PROGRESS);
                        mHandler.sendMessageDelayed(msg, 100); // 100 millisecond delay
                    }
                    else {
                        // clear messages
                        removeMessages(SHOW_PROGRESS);
                        // seek to zero
                        mSeekBar.setProgress(0);
                        // set time current time to zero
//                    mCurrentPositionText.setText(timeToHourMinuteSecondeSecond(0));
                        // set button image
                        mPlayPauseButton.setImageResource(R.drawable.music_play);
                    }
                }
            };
        }

        // post message
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.edit_new_cut_button:
                // the new cut button was pressed
                // new cut at current position
                cut(mPosition);
                // update view
                mTracksListAdapter.notifyDataSetChanged();
                break;
            case R.id.edit_play_pause_button:
                // the play pause button was pressed
                if(mPlaying) {
                    // pause
                    pausePlayback();
                }
                else if (mPaused) {
                    // resume
                    resumePlayback();
                }
                else {
                    // start
                    startPlayback();
                }
                break;
            case R.id.edit_export_button:
                // export edit tracks
                export();
                break;
        }
    }

    @Override
    public void onStop() {

        // call to super
        super.onStop();

        // stop the playback
        stopPlayback();

        // release the handler
        mHandler = null;
    }
}
