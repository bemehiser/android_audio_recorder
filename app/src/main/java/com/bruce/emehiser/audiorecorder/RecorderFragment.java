package com.bruce.emehiser.audiorecorder;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Recorder {@link Fragment} for recording raw audio
 *
 * @author Bruce Emehiser
 */
public class RecorderFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    /**
     * Image resource IDs for button
     */
    enum ImageResource {

        STOP(R.drawable.music_stop),
        RECORD(R.drawable.music_record),
        PAUSE(R.drawable.music_pause);

        /**
         * Resource ID
         */
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

    /**
     * User interface elements
     */
    private ImageView mRecordButton;
    private TextView mRecordingStatusText;
    private ProgressBar mRecordingStatusProgressBar;
    private TextView mRecordingLengthText;
    private EditText mTitleEditText;

    /**
     * Audio recorder used for recording a raw byte stream
     */
    private AudioRecord mAudioRecord;

    /**
     * Audio profile for recorded audio
     */
    private static final int RECORDER_BPP = 16;
    public static final int RECORDER_SAMPLE_RATE = 11025;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;
    private int mBufferSize;

    public static final String OUTPUT_RAW_FILE = "temp.raw";
    public static final String OUTPUT_WAV_FILE = "temp.wav";

    /**
     * File manager, used for getting a valid file to record to
     */
    private FileManager mFileManager;

    /**
     * Boolean value representing the current STATE of the recorder
     * as Recording or Not Recording
     */
    private boolean mRecording;

    /**
     * Async Task for recording audio
     */
    private AsyncTask<Void, Integer, Void> mRecordingTask;

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

        // set buffer size
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

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
    private void startRecording(){

        // set up audio recorder
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, mBufferSize);

        // check that recorder is ready to start
        if(mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            // start recorder
            mAudioRecord.startRecording();
        }

        // set recording to true
        mRecording = true;

        // capture and write audio data in async task
        mRecordingTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // buffer to hold temporary data
                byte data[] = new byte[mBufferSize];
                // temporary file
                String fileName = OUTPUT_RAW_FILE;
                // output stream to temporary raw file
                FileOutputStream outputStream = null;

                try {
                    outputStream = new FileOutputStream(mFileManager.getOutputFile(fileName));
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error opening output stream with fileName " + fileName);
                }

                // error code initialized to zero
                int read;

                // start time of recording
                long recordingStartTime = System.currentTimeMillis();

                // check output stream exists
                if(outputStream != null){
                    while(mRecording){
                        // read data from audio recorder
                        read = mAudioRecord.read(data, 0, mBufferSize);

                        // check that read did not fail
                        if(read != AudioRecord.ERROR_INVALID_OPERATION){
                            try {
                                // write to output file
                                outputStream.write(data);
                            } catch (IOException e) {
                                Log.e(TAG, "Error writing to output file stream attached to " + fileName);
                            }
                        }

                        // post progress
                        // NOTE: This will overflow when the time is greater than 24 days
                        publishProgress((int) (System.currentTimeMillis() - recordingStartTime));
                    }

                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing output stream");
                    }
                }

                // null return from async task
                return null;
            }

            /**
             * Updates UI elements of recording progress
             * @param progress The recording progress in milliseconds
             */
            protected void onProgressUpdate(Integer... progress) {
//                statusText.setText(progress[0].toString());
                mRecordingLengthText.setText(String.format("%d", progress[0]));
            }

            protected void onPostExecute(Void result) {
//                startRecordingButton.setEnabled(true);
//                stopRecordingButton.setEnabled(false);
//                startPlaybackButton.setEnabled(true);
            }
        };

        // run async
        mRecordingTask.execute();
    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    /**
     * Stop and release media recorder
     */
    private void stopRecording() {
        // check for null
        if(mAudioRecord != null) {
            // stop recording
            mRecording = false;
            if(mAudioRecord.getState() != AudioRecord.RECORDSTATE_STOPPED && mAudioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
                mAudioRecord.stop();
            }
            mAudioRecord.release();
            mAudioRecord = null;
            mRecordingTask = null;
            // start progress bar
            mRecordingStatusProgressBar.setVisibility(ProgressBar.INVISIBLE);
            // set the record button
            mRecordButton.setImageResource(ImageResource.RECORD.getResource());
        }

        try {
            copyWaveFile(mFileManager.getFile(OUTPUT_RAW_FILE), mFileManager.getOutputFile(OUTPUT_WAV_FILE));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found " + OUTPUT_RAW_FILE);
        }
//        deleteTempFile();
    }

    private void copyWaveFile(File inputFile, File outputFile){

        // input stream to read from temp.raw file
        FileInputStream input;
        // output stream to write to output.wav file
        FileOutputStream output;
        // total length of audio file
        long totalAudioLength;
        // total size in bytes of audio file plus header
        long totalDataSize;
        // sample rage
        long longSampleRate = RECORDER_SAMPLE_RATE;
        // channels we are recording in
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * (channels / 8);

        byte[] data = new byte[mBufferSize];

        try {
            // open file streams
            input = new FileInputStream(inputFile);
            output = new FileOutputStream(outputFile);
            // get length and size
            totalAudioLength = input.getChannel().size();
            totalDataSize = totalAudioLength + 36;

            Log.i(TAG, "File size: " + totalDataSize);

            // write .wav header
            writeWaveFileHeader(output, totalAudioLength, totalDataSize,
                    longSampleRate, channels, byteRate);

            // copy data
            while(input.read(data) != -1){
                output.write(data);
            }

            // close data streams
            input.close();
            output.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File Not Found Exception " + inputFile + " " + outputFile);
        } catch (IOException e) {
            Log.e(TAG, "Error reading data " + inputFile + " " + outputFile);
        }
    }

    /**
     * Sample rates used for findAudioRecord()
     */
    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

    /**
     * Find a working audio recording source
     * @return The audio source which worked
     */
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(AUDIO_SOURCE, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
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
//                    // get title for new file
//                    Calendar calendar = Calendar.getInstance();
//                    String title = String.format("%s_%d:%d:%d.mp3", mTitleEditText.getText(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
//                    // get path of new file
//                    String path = mFileManager.getOutputFile(title).getAbsolutePath();
                    // start new recording
//                    startRecording(path);
                    startRecording();
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

        // stop the recording
        stopRecording();

        // call to super
        super.onStop();

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
