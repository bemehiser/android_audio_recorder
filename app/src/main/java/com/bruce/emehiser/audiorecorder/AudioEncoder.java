package com.bruce.emehiser.audiorecorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Bruce Emehiser on 12/20/2015.
 *
 * Class used to edit audio files
 */
public class AudioEncoder {

    private byte[] mAudioBytes;

    public class Builder {

        File audioFile;




    }

    public AudioEncoder(File audioFile, int bitRate, int channels) {

        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 41000, 2, MediaRecorder.AudioEncoder.AAC, 16000);

        // get the audio file
        try {
            // get input stream used for reading the file
            FileInputStream inputStream = new FileInputStream(audioFile);
        } catch (IOException e) {
            Log.e("AudioEncoder", "Error opening file from input stream " + e.toString());
        }
    }




}
