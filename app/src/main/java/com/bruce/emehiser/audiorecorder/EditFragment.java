package com.bruce.emehiser.audiorecorder;


import android.app.Fragment;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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


    /**
     * User Interface View Elements
     */
    private ListView mTracksListView;
    private ArrayAdapter mTracksListAdapter;
    private ArrayList<Track> mTracks;
    private SeekBar mSeekBar;
    private ImageButton mPlayPauseButton;
    private Button mTrackButton;

    /**
     * File manager
     */
    private FileManager mFileManager;

    /**
     * Current raw sound File which is being edited
     */
    private File mRawFile;

    /**
     * Input stream to read data from for playback and editing
     */
    private InputStream mRawInputStream;

    /**
     * Current Audio Track for playing the raw audio
     */
    private AudioTrack mAudioTrack;

    /**
     * Handler to post updates for the seek bar when media is playing
     */
    private Handler mHandler;

    /**
     * Boolean representing the current STATE of the playback
     * Int representing the total length of the file in bytes
     * Int representing the current position in bytes
     */
    private boolean mPlaying;
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

        // set up list adapter for cuts list view
        mTracks = new ArrayList<>();
        mTracksListAdapter = new ArrayAdapter<Track>(getActivity(), android.R.layout.simple_list_item_1, mTracks);
        mTracksListView.setAdapter(mTracksListAdapter);

        // set on click listeners
        mTrackButton.setOnClickListener(this);
        mPlayPauseButton.setOnClickListener(this);

        // instantiate file manager
        mFileManager = new FileManager();

        // create the audio track
        int bufferSize = android.media.AudioTrack.getMinBufferSize(11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 11025, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        // set up handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int pos;
                if(mPlaying) {
                    // update seek bar and text view
                    mSeekBar.setProgress(mPosition);
//                    mCurrentPositionText.setText(timeToHourMinuteSecond(mPosition));
                    // clear and set next message and delay
                    msg = obtainMessage(SHOW_PROGRESS);
                    mHandler.sendMessageDelayed(msg, 1000 - (mPosition % 1000));
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
        if(mTracks.size() > 0) {
            // get the last one to check
            Track track = mTracks.get(mTracks.size() - 1);
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
        Track track = new Track(timeInMills);
        // add track to list of tracks
        mTracks.add(track);
        // notify user
        Toast.makeText(getActivity(), "Track Created Successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Starts playback of the current track
     * @param position The position at which to start
     */
    public void startPlayback(int position) {

        try {

            int count = 512 * 1024; // 512 kb

            // read the file
            byte[] byteData = null;
            mRawFile = mFileManager.getFile("temp.raw"); // file might be invalid

            // get file length (this may have issues if the file is more than 2 GB)
            mLength = (int) (mRawFile.length());

            byteData = new byte[(int) count];
            FileInputStream in = null;
            in = new FileInputStream(mRawFile);

            int bytesRead = 0;
            int ret;

            mAudioTrack.play();
            // Write the byte array to the track
            //todo run this in async task
            while (bytesRead < mLength) {
                ret = in.read(byteData, 0, count);
                if (ret != -1) {
                    mAudioTrack.write(byteData, 0, ret);
                    bytesRead += ret;
                } else break;
            }
            in.close();
            mAudioTrack.stop();
            mAudioTrack.release();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
        } catch (IOException f) {
            Log.e(TAG, "Error opening or reading file");
        }
        // set the seek bar max length
        mSeekBar.setMax(mLength);
        // start updating seek bar
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    /**
     * Stops playback of the current track
     */
    public void stopPlayback() {

        mPlaying = false;

        // stop updating seek bar
        mHandler.removeMessages(SHOW_PROGRESS);
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
                cut(5000);
                // update view
                mTracksListAdapter.notifyDataSetChanged();
                break;
            case R.id.edit_play_pause_button:
                // the play pause button was pressed
                startPlayback(0);
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
