package com.bruce.emehiser.audiorecorder;


import android.app.Fragment;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
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
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * Edit {@link Fragment} for editing raw audio files
 * @author Bruce Emehiser
 */
public class EditFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EditFragment";
    private static final int SHOW_PROGRESS = 42;
    public static final String TEMP_WAV_FILE = "temp.wav";


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
     * Int representing the total length of the file in bytes
     * Int representing the current position in bytes
     */
    private boolean mPlaying;
    private int mLength;
    private int mPosition;

    /**
     * Media format used for encoding audio in MediaCodec
     */
    MediaFormat mInputFormat;
    MediaFormat mOutputFormat; // member variable


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
//
//        try {
//            File inputFile = mFileManager.getFile(TEMP_WAV_FILE);
//            FileInputStream inputStream = new FileInputStream(inputFile);
//            FileDescriptor fileDescriptor = inputStream.getFD();
//
//            MediaExtractor mediaExtractor = new MediaExtractor();
//            mediaExtractor.setDataSource(fileDescriptor);
//
//            // read and display track count
//            int count = mediaExtractor.getTrackCount();
//            Log.d(TAG, String.format("TRACKS #: %d", count));
//
//            MediaFormat format = mediaExtractor.getTrackFormat(0);
//            String mime = format.getString(MediaFormat.KEY_MIME);
//            Log.d(TAG, format.toString());
//            Log.d(TAG, mime);
//
//            MediaCodec codec = MediaCodec.createDecoderByType(mime);
//            MediaCodec.createEncoderByType(mime);
//            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
//            codec.start();
//
//            ByteBuffer[] codecInputBuffers = codec.getInputBuffers();
//            ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();
//
//            mediaExtractor.selectTrack(0); // <= You must select a track. You will read samples from the media from this track!
//
//            long timeout = 100;
//
//            boolean sawInputEOS = false;
//            boolean sawOutputEOS = false;
//
//            FileOutputStream fileOutputStream = new FileOutputStream(mFileManager.getOutputFile("output.acc"));
//
//            while(true){
//                // input to codec
//                int inputBufIndex = codec.dequeueInputBuffer(timeout);
//                if (inputBufIndex >= 0) {
//                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
//
//                    int sampleSize = mediaExtractor.readSampleData(dstBuf, 0);
//                    long presentationTimeUs = 0;
//                    if (sampleSize < 0) {
//                        sawInputEOS = true;
//                        sampleSize = 0;
//                    } else {
//                        presentationTimeUs = mediaExtractor.getSampleTime();
//                    }
//
//                    codec.queueInputBuffer(inputBufIndex,
//                            0, //offset
//                            sampleSize,
//                            presentationTimeUs,
//                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
//                    if (!sawInputEOS) {
//                        mediaExtractor.advance();
//                    }
//
//                    // output from codec
//                    final int res = codec.dequeueOutputBuffer(info, timeout);
//                    if (res >= 0) {
//                        int outputBufIndex = res;
//                        ByteBuffer buf = codecOutputBuffers[outputBufIndex];
//
//                        final byte[] chunk = new byte[info.size];
//                        buf.get(chunk); // Read the buffer all at once
//                        buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN
//
//                        if (chunk.length > 0) {
////                            audioTrack.write(chunk, 0, chunk.length);
//                        }
//                        codec.releaseOutputBuffer(outputBufIndex, false /* render */);
//
//                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                            sawOutputEOS = true;
//                        }
//                    } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                        codecOutputBuffers = codec.getOutputBuffers();
//                    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                        final MediaFormat oformat = codec.getOutputFormat();
//                        Log.d(TAG, "Output format has changed to " + oformat);
//
//                        mAudioTrack.setPlaybackRate(oformat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
//                    }
//                }
//            }
//
//        } catch (FileNotFoundException e) {
//            // do nothing
//        } catch (IOException e){
//
//        }

        // call to encoder TESTING
//        encode();


//        try {
//            AudioEncoder.encode(mFileManager.getOutputFile("output.aac"), mFileManager.getFile(TEMP_WAV_FILE));
//
//        } catch (Exception e) {
//            Log.e(TAG, toString());
//            e.printStackTrace();
//        }
//
//        // get file reference
//        try {
//            mWaveFile = mFileManager.getFile(TEMP_WAV_FILE);
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "File not found " + TEMP_WAV_FILE);
//        }

        encode();

        return view;
    }


//    private void encode() {
//
//        MediaCodec mediaCodec;
//        BufferedOutputStream outputStream;
//        String mediaType = "audio/mp4a-latm";
//
//        File f = mWaveFile;
//
//        try {
//            outputStream = new BufferedOutputStream(new FileOutputStream(f));
//            Log.e("AudioEncoder", "outputStream initialized");
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//        try {
//
//        mediaCodec = MediaCodec.createEncoderByType(mediaType);
//        final int kSampleRates[] = { 8000, 11025, 22050, 44100, 48000 };
//        final int kBitRates[] = { 64000, 128000 };
//        MediaFormat mediaFormat  = MediaFormat.createAudioFormat(mediaType,kSampleRates[3],1);
//        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, kBitRates[1]);
//        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        mediaCodec.start();
//
//
//            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
//            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
//            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
//            if (inputBufferIndex >= 0) {
//                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//                inputBuffer.clear();
//
//                inputBuffer.put(input);
//
//
//                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
//            }
//
//            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
//
////Without ADTS header
//            while (outputBufferIndex >= 0) {
//                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//                byte[] outData = new byte[bufferInfo.size];
//                outputBuffer.get(outData);
//                outputStream.write(outData, 0, outData.length);
//                Log.e("AudioEncoder", outData.length + " bytes written");
//
//                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//
//            }
//        } catch (Exception t) {
//        }
//    }


    private MediaCodec encoder;

    private short audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private short channelConfig = AudioFormat.CHANNEL_IN_MONO;

    private int bufferSize;
    private boolean isEncoding;

    private Thread IOrecorder;

    private Thread IOudpPlayer;

    private boolean setEncoder() throws Exception {
        encoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64 * 1024);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return true;
    }

    private void encode() {

        IOrecorder = new Thread(new Runnable()
        {
            public void run()
            {

                try
                {
                    // set encoder type
                    setEncoder();

                    // input and output streams
                    File inputFile = mFileManager.getFile(TEMP_WAV_FILE);
                    FileInputStream fileInputStream = new FileInputStream(inputFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileManager.getOutputFile("output.aac"));

                    // set buffer size
                    bufferSize = 1024;

                    int wait = 0;

                    int read;
                    byte[] buffer1 = new byte[bufferSize];

                    ByteBuffer[] inputBuffers;
                    ByteBuffer[] outputBuffers;

                    ByteBuffer inputBuffer;
                    ByteBuffer outputBuffer;

                    MediaCodec.BufferInfo bufferInfo;
                    int inputBufferIndex;
                    int outputBufferIndex;

                    byte[] outData;

                    // start encoder
                    encoder.start();

//                    recorder.startRecording();
                    isEncoding = true;

                    int positionInFile = 0;
                    int totalFileLength = (int) inputFile.length();


                    int inputFrames = 0;
                    int outputFrames = 0;

                    while (positionInFile < totalFileLength || inputFrames > outputFrames) {
                        Log.d(TAG, "In main encoder while loop");

////                        read = recorder.read(buffer1, 0, bufferSize);
//                        // todo read from input stream
//                        read = fileInputStream.read(buffer1, 0, bufferSize);
//                        Log.d(TAG, read + " bytes read");
//                        positionInFile += read;

                        //------------------------
                        // input and output buffers
                        inputBuffers = encoder.getInputBuffers();
                        outputBuffers = encoder.getOutputBuffers();
                        // get input buffer index among all input buffers
                        inputBufferIndex = encoder.dequeueInputBuffer(wait); // no wait for input buffers
                        // if we got a buffer
                        if (inputBufferIndex >= 0) // if there are input buffers, fill them
                        {
                            //                        read = recorder.read(buffer1, 0, bufferSize);
                            // todo read from input stream
                            read = fileInputStream.read(buffer1, 0, bufferSize);
                            Log.d(TAG, read + " bytes read");

                            if(read >= 0) { // if we actually read something, then use it
                                positionInFile += read;

                                // get byte buffer
                                inputBuffer = inputBuffers[inputBufferIndex];
                                // make sure buffer is clear
                                inputBuffer.clear();
                                // put bytes from other buffer into it
                                inputBuffer.put(buffer1);

                                // tell input buffer to queue the buffer we just filled
                                encoder.queueInputBuffer(inputBufferIndex, 0, buffer1.length, 0, 0);

                                // add total frames
                                inputFrames ++;
                            }
                        }

                        // get buffer info
                        bufferInfo = new MediaCodec.BufferInfo();
                        // get output buffer index of processed bytes
                        outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, wait); // no wait -1 milliseconds

                        int numBytesDequeued = 0;

                        // if input buffers exist
                        if (outputBufferIndex >= 0) {
                            int outBitsSize   = bufferInfo.size;
                            int outPacketSize = outBitsSize + 7;    // 7 is ADTS size
                            ByteBuffer outBuf = outputBuffers[outputBufferIndex];

                            outBuf.position(bufferInfo.offset);
                            outBuf.limit(bufferInfo.offset + outBitsSize);
                            try {
                                byte[] data = new byte[outPacketSize];  //space for ADTS header included
                                addADTStoPacket(data, outPacketSize);
                                outBuf.get(data, 7, outBitsSize);
                                outBuf.position(bufferInfo.offset);
                                fileOutputStream.write(data, 0, outPacketSize);  //open FileOutputStream beforehand
                            } catch (IOException e) {
                                Log.e(TAG, "failed writing bitstream data to file");
                                e.printStackTrace();
                            }

                            numBytesDequeued += bufferInfo.size;

                            outBuf.clear();
                            encoder.releaseOutputBuffer(outputBufferIndex, false /* render */);

                            // decrement frames
                            outputFrames ++;

                            Log.d(TAG, "  dequeued " + outBitsSize + " bytes of output data.");
                            Log.d(TAG, "  wrote " + outPacketSize + " bytes into output file.");
                        }
                        else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        }
                        else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            outputBuffers = encoder.getOutputBuffers();
                        }
                    }
                    encoder.stop();

                    // flush and close streams
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        IOrecorder.run();
    }

    /**
     *  Add ADTS header at the beginning of each and every AAC packet.
     *  This is needed as MediaCodec encoder generates a packet of raw
     *  AAC data.
     *
     *  Note the packetLen must count in the ADTS header itself.
     **/
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE

        // fill in ADTS data
        packet[0] = (byte)0xFF;
        packet[1] = (byte)0xF9;
        packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
        packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
        packet[4] = (byte)((packetLen&0x7FF) >> 3);
        packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
        packet[6] = (byte)0xFC;
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
        editTrack.setStartPosition(timeInMills);
        // add track to list of tracks
        mEditTracks.add(editTrack);
        // notify user
        Toast.makeText(getActivity(), "Track Created Successfully", Toast.LENGTH_SHORT).show();
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
            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
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
            Log.e(TAG, "File not found " + TEMP_WAV_FILE);
        } catch (IOException e) {
            Log.e(TAG, "IOException reading " + TEMP_WAV_FILE);
        }
        // media player is now playing
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
        }

        // post message
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    /**
     * Exports all tracks in list to individual media files
     */
    private void export() {

        // if there are no tracks
        if(mEditTracks == null) {
            return;
        }
        if(mEditTracks.size() == 0) {
            return;
        }

        // get the size of the wave file minus 44 bytes of wav header
        int fileBytes = (int) mWaveFile.length() - 44;
        int fileLength = mLength; // todo media must have been played

        float byteLengthRatio = (float) fileBytes / (float) fileLength;

        // buffer for copying files of 1024 bytes
        byte[] buffer = new byte[1024];


        // for every track in list
        for(EditTrack track : mEditTracks) {

            try {

                // open file input stream
                FileInputStream fileInputStream = new FileInputStream(mWaveFile);

                // get relative start and end positions
                int startBytePosition = (int) ((float) track.getStartTime() * byteLengthRatio);
                int endBytePosition = (int) ((float) track.getEndTime() * byteLengthRatio);

                String outputFileName = track.getTitle() +"_"+ track.getStartTime() + "_" + track.getEndTime();

                // open file output stream
                FileOutputStream fileOutputStream = new FileOutputStream(mFileManager.getOutputFile(outputFileName));

                // move to beginning of track (error with files over 2 GB)
                int skipped = (int) fileInputStream.skip(startBytePosition + 44);

                // read track and write to file
                int currentByte = skipped;
                int read = 0;
                while(currentByte < endBytePosition && read != -1) {
                    // read from input file
                    int size = 1024 % (endBytePosition - currentByte);
                    Log.i(TAG, "size: " + size);
                    if(size > 0) {
                        read = fileInputStream.read(buffer, 0, size);
                        Log.i(TAG, "read:" + read);
                    }
                    else {
                        read = -1;
                    }

                    // write to output file
                    if(read != -1) {
                        fileOutputStream.write(buffer, 0, read);
                    }

                    currentByte += read;
                    Log.i(TAG, "currentByte:" + currentByte);
                }

                Log.i(TAG, "end - start: " + (endBytePosition - startBytePosition));
                Log.i(TAG, "start: " + startBytePosition);
                Log.i(TAG, "end: " + endBytePosition);
                Log.i(TAG, "Wrote File Size: " + (currentByte - skipped));

            } catch (IOException e) {
                Log.e(TAG, "Error opening input or output file stream");
            }
        }
    }

//    private void encode() {
//        try {
//            MediaCodec codec = MediaCodec.createByCodecName(name);
//
//            codec.setCallback(new MediaCodec.Callback() {
//                @Override
//                public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
//                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
//                    // fill inputBuffer with valid data
//                    codec.queueInputBuffer(inputBufferId, ...);
//                }
//
//                /**
//                 * Called when an output buffer becomes available.
//                 *
//                 * @param codec The MediaCodec object.
//                 * @param index The index of the available output buffer.
//                 * @param info  Info regarding the available output buffer {@link MediaCodec.BufferInfo}.
//                 */
//                @Override
//                public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
//                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
//                    MediaFormat bufferFormat = codec.getOutputFormat(index); // option A
//                    // bufferFormat is equivalent to mOutputFormat
//                    // outputBuffer is ready to be processed or rendered
//                    codec.releaseOutputBuffer(index, true);
//                }
//
//                @Override
//                public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
//                    // Subsequent data will conform to new format.
//                    // Can ignore if using getOutputFormat(outputBufferId)
//                    mOutputFormat = format; // option B
//                }
//
//                /**
//                 * Called when the MediaCodec encountered an error
//                 *
//                 * @param codec The MediaCodec object.
//                 * @param e     The {@link MediaCodec.CodecException} object describing the error.
//                 */
//                @Override
//                public void onError(MediaCodec codec, MediaCodec.CodecException e) {
//
//                }
//            });
//            codec.configure(format, …);
//            mOutputFormat = codec.getOutputFormat(); // option B
//            codec.start();
//            // wait for processing to complete
//            codec.stop();
//            codec.release();
//        } catch (IOException e) {
//            Log.e(TAG, "Error creating media codec");
//        }
//    }

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
                startPlayback();
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
