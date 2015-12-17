package com.bruce.emehiser.audiorecorder;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link Fragment} which contains a {@link MediaPlayer}.
 *
 * This fragment can be used to play a {@link MediaFile}, and contains
 * a play pause button and seek control
 */
public class PlaybackFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    private static final int SHOW_PROGRESS = 42;
    public static final int SEEK_INTERVAL_IN_MILLS = 10000;
    private static String TAG = "PlaybackFragment";

    // the fragment initialization parameter
    private static final String ARG_MEDIA_FILE = "arg_media_file";

    private OnFragmentInteractionListener mListener;

    // media player for media playback
    private MediaPlayer mMediaPlayer;
    private File mArgumentFile;

    // list of files for display
    private ArrayList<File> mFileList;
    // array adapter for list view
    private ListAdapter mListAdapter;

    // user interface elements
    private ListView mListView;
    private SeekBar mSeekBar;
    private TextView mCurrentPositionText;
    private TextView mTotalLengthText;
    private ImageView mPlayPauseButton;
    private ImageView mReverseButton;
    private ImageView mForwardButton;


    public PlaybackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if there are arguments passed
        if (getArguments() != null) {
            // get file from arguments
            mArgumentFile = (File) getArguments().get(ARG_MEDIA_FILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback, container, false);

        // get views
        mListView = (ListView) view.findViewById(R.id.playback_file_list);
        mSeekBar = (SeekBar) view.findViewById(R.id.playback_seek_bar);
        mCurrentPositionText = (TextView) view.findViewById(R.id.playback_current_position_text);
        mTotalLengthText = (TextView) view.findViewById(R.id.playback_total_length_text);
        mPlayPauseButton = (ImageView) view.findViewById(R.id.playback_play_pause_button);
        mReverseButton = (ImageView) view.findViewById(R.id.playback_reverse_button);
        mForwardButton = (ImageView) view.findViewById(R.id.playback_forward_button);

        // set on click listeners
        mListView.setOnItemClickListener(this);
        mPlayPauseButton.setOnClickListener(this);
        mReverseButton.setOnClickListener(this);
        mForwardButton.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(this);

        // populate list view from files
        FileManager fileManager = new FileManager();
        List<File> files = fileManager.getFiles();
        mFileList = new ArrayList<File>(files);

        //  create adapter
        mListAdapter = new ArrayAdapter<File>(view.getContext(), android.R.layout.simple_list_item_1, mFileList);
        // set adapter
        mListView.setAdapter(mListAdapter);
        // set on click listener
        mListView.setOnItemClickListener(this);

        // if argument was passed, start the media player
        if(mArgumentFile != null) {
            startPlayback(mArgumentFile);
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // validate data
        if(mFileList == null || position < 0 || position > mFileList.size()) {
            return;
        }
        // start playback of item
        startPlayback(mFileList.get(position));
    }

    /**
     * Begins a mMediaPlayer playing
     * @param file music file to play
     */
    private void startPlayback(File file) {

        if(file == null) {
            Log.i(TAG, "startPlayback(File) cannot start playback, file null");
        }

        // check for null or running player
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        // run the media player
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(getActivity(), Uri.fromFile(file));
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // set the total time text
                    mTotalLengthText.setText(timeToHourMinuteSecond(mMediaPlayer.getDuration()));
                    // set seek bar total time
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    // start playback
                    startPlayback();
                }
            });
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Error reading file. Maybe it's not an audio file.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error playing from file");
        }
    }

    /**
     * Start mMediaPlayer playback
     */
    private void startPlayback() {
        // check for null
        if(mMediaPlayer != null) {
            try {
                // start player
                mMediaPlayer.start();
                // set play pause button resource
                mPlayPauseButton.setImageResource(R.drawable.music_pause);
                // start updating seek bar
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            } catch (Exception e) {
                Log.e(TAG, "Media player does not have data source set");
            }
        }
    }

    /**
     * Pause mMediaPlayer if it is playing
     */
    private void pausePlayback() {
        // check for null
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            // pause media player
            mMediaPlayer.pause();
            // set image resource
            mPlayPauseButton.setImageResource(R.drawable.music_play);
            // remove messages
            mHandler.removeMessages(SHOW_PROGRESS);
        }
    }

    /**
     * Stop mMediaPlayer and release it
     */
    private void stopPlayback() {
        // check for null
        if(mMediaPlayer != null) {
            // stop media playback
            if(mMediaPlayer.isPlaying()) {
                // stop playback
                pausePlayback();
            }
            // release player
            mMediaPlayer.release();
        }
    }

    /**
     * Convert time in mills to formatted string of time
     * @param time in mills
     * @return formatted string of time in format h:mm:ss
     */
    private String timeToHourMinuteSecond(int time) {

        // change to time in seconds
        time /= 1000;
        // get seconds
        int sec = time % 60;
        // change to time in minutes
        time /= 60;
        // get minutes
        int min = time % 60;
        // change to time in hours
        time /= 60;
        // get hours
        int hr = time;

        // return formatted string
        return String.format("%d:%02d:%02d", hr, min, sec);
    }

    /**
     * Update mMusicPlayer based on user input from seek bar
     *
     * @param seekBar  The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range 0..max where max
     *                 was set by {link ProgressBar#setMax(int)}. (The default value for max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        // check for null and that input came from user
        if(mMediaPlayer != null && fromUser) {
            // seek to position
            mMediaPlayer.seekTo(progress);
        }
    }

    /**
     * I want the media player to continually update, so I don't use this
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    /**
     * I want the media player to continually update, so I don't use this
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            // play pause button
            case R.id.playback_play_pause_button:
                // play/pause mMediaPlayer
                if(mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        pausePlayback();
                    }
                    else {
                        startPlayback();
                    }
                }
                break;
            case R.id.playback_reverse_button:
                // seek back 10 seconds
                if(mMediaPlayer != null) {
                    int pos = mMediaPlayer.getCurrentPosition() - SEEK_INTERVAL_IN_MILLS;
                    mMediaPlayer.seekTo(pos > 0 ? pos : 0);
                }
                break;
            case R.id.playback_forward_button:
                // see forward 10 seconds
                if(mMediaPlayer != null) {
                    int pos = mMediaPlayer.getCurrentPosition() + SEEK_INTERVAL_IN_MILLS;
                    mMediaPlayer.seekTo(pos < mMediaPlayer.getDuration() ? pos : mMediaPlayer.getDuration());
                }
        }
    }

    /**
     * This block of code will stop being called when the media player stops.
     * Because , the handler and the activity
     * it is attached to will be garbage collected
     */
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                pos = mMediaPlayer.getCurrentPosition();
                // update seek bar and text view
                mSeekBar.setProgress(pos);
                mCurrentPositionText.setText(timeToHourMinuteSecond(pos));
                // clear and set next message and delay
                msg = obtainMessage(SHOW_PROGRESS);
                mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
            }
            else {
                // clear messages
                removeMessages(SHOW_PROGRESS);
                // seek to zero
                mSeekBar.setProgress(0);
                // set time current time to zero
                mCurrentPositionText.setText(timeToHourMinuteSecond(0));
                // set button image
                mPlayPauseButton.setImageResource(R.drawable.music_play);
            }
        }
    };
}
