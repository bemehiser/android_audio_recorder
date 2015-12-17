package com.bruce.emehiser.audiorecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Bruce Emehiser on 12/16/2015.
 *
 * Creates and maintains file directory for application
 * Allows query and simple interactions of file directory
 */
public class FileManager {

    private File mFileManagerDirectory;

    private final String mFileManagerDirectoryName = "AudioRecorder";

    public FileManager() {
        // initialize file manager system
        initialize();
    }

    /**
     * Initialize file directory.
     * If directory does not exist, create one.
     */
    private void initialize() {

        // get root directory
        mFileManagerDirectory = new File(android.os.Environment.getExternalStorageDirectory() + File.separator + mFileManagerDirectoryName);

        // check file exists
        if(mFileManagerDirectory.exists()) {
            // check that file is directory
            if(mFileManagerDirectory.isDirectory()) {
                // directory is already set up
                return;
            }
            else {
                // delete file
                mFileManagerDirectory.delete();
            }
        }
        // make directory
        mFileManagerDirectory.mkdir();
    }

    /**
     * Return all files in the directory
     */
    public List<File> getFiles() {
        // check for null directory
        if(mFileManagerDirectory == null) {
            // if null, create directory
            initialize();
        }

        // get files
        File[] files = mFileManagerDirectory.listFiles();

        // check for empty list
        if(files != null && files.length > 0) {
            // return populated list
            return Arrays.asList(files);
        }
        // return empty list
        return new ArrayList<>();
    }

}
