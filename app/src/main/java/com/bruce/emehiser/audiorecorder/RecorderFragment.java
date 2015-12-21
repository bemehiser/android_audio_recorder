package com.bruce.emehiser.audiorecorder;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecorderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RecorderFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    enum ImageResource {

        STOP(R.drawable.music_stop),
        RECORD(R.drawable.music_record),
        PAUSE(R.drawable.music_pause);

        private int resource;

        ImageResource(int resource){
            this.resource = resource;
        }

        public int getResource() {
            return resource;
        }
    }

    public static final String TAG = "RecordingFragment";
    private OnFragmentInteractionListener mListener;

    // references
    private ImageView mRecordButton;
    private TextView mRecordingStatusText;
    private ProgressBar mRecordingStatusProgressBar;
    private TextView mRecordingLengthText;
    private EditText mTitleEditText;

    // recorder
    private MediaRecorder mMediaRecorder;
    // file manager
    private FileManager mFileManager;

    // media player is recording
    private boolean mRecording;

    public RecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);

        // get file manager
        mFileManager = new FileManager();

        // get references to gui elements
        mRecordButton = (ImageView) view.findViewById(R.id.recorder_record_button);
        mRecordingStatusProgressBar = (ProgressBar) view.findViewById(R.id.recorder_progress_bar);
        mRecordingLengthText = (TextView) view.findViewById(R.id.recorder_recording_length_text);
        mTitleEditText = (EditText) view.findViewById(R.id.recorder_recording_title_edit_text);

        // set onClick and onLongClick listeners
        mRecordButton.setOnClickListener(this);
        mRecordButton.setOnLongClickListener(this);

        // set recording false
        mRecording = false;

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
     * Start recording audio
     */
    private void startRecording(String filePath) {

        // check for null or empty file path
        if(! (filePath != null && filePath.length() != 0)) {
            Log.e(TAG, "invalid file path " + filePath);
            return;
        }

        // check already recording
        if(mRecording) {
            Toast.makeText(getActivity(), "Already Recording Audio", Toast.LENGTH_SHORT).show();
            return;
        }
        // get new media recorder
        mMediaRecorder = new MediaRecorder();

        // set audio source to use default recording device
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // set to record to mpeg4
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        // set to output to file with name
        mMediaRecorder.setOutputFile(filePath);
        // encode with acc audio
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // set the bit rate to 128 kB/s
        mMediaRecorder.setAudioEncodingBitRate(128000);
        // set the channels to 1 mono
        mMediaRecorder.setAudioChannels(1);
        // prepare and start recorder
        try {
            // prepare
            mMediaRecorder.prepare();
            // start
            startRecording();
            // notify user of start
            Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error preparing and starting media recorder.");
        }
    }

    /**
     * Resume recording
     */
    private void startRecording() {
        // check for null
        if(mMediaRecorder != null) {
            // start recorder
            mMediaRecorder.start();
            // start progress bar
            mRecordingStatusProgressBar.setVisibility(ProgressBar.VISIBLE);
            // set the image
            mRecordButton.setImageResource(ImageResource.STOP.getResource());
            // set recording
            mRecording = true;
        }
    }

    /**
     * Pause the audio recording to later be stopped or resumed
     */
    private void pauseRecording() {
        // check for null
        Toast.makeText(getActivity(), "Pause Not Yet Implemented", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop and release media recorder
     */
    private void stopRecording() {
        // check for null
        if(mMediaRecorder != null) {
            // stop recorder
            mMediaRecorder.stop();
            // release recorder
            mMediaRecorder.release();
            mMediaRecorder = null;
            // start progress bar
            mRecordingStatusProgressBar.setVisibility(ProgressBar.INVISIBLE);
            // set the record button
            mRecordButton.setImageResource(ImageResource.RECORD.getResource());
            // set recording
            mRecording = false;
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.recorder_record_button:
                if(! mRecording) {
                    // get title for new file
                    Calendar calendar = Calendar.getInstance();
                    String title = String.format("%s_%d:%d:%d.mp3", mTitleEditText.getText(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    // get path of new file
                    String path = mFileManager.getOutputFile(title).getAbsolutePath();
                    // start new recording
                    startRecording(path);
                }
                else {
                    // stop existing recording
                    Toast.makeText(getActivity(), "If you really want to stop, long press the stop button", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param view The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {
            case R.id.recorder_record_button:
                if(mRecording) {
                    // stop recording
                    stopRecording();
                }
                break;
        }
        return true;
    }

    @Override
    public void onStop() {

        // call to super
        super.onStop();

        // stop the recording
        stopRecording();
    }

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


}
