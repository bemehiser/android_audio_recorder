package com.bruce.emehiser.audiorecorder;

import java.security.InvalidParameterException;

/**
 * Created by Bruce Emehiser on 12/23/2015.
 *
 * Edit Track used to contain the information necessary to
 * cut a section from a longer track
 */
public class EditTrack extends ID3v1Tag {

    /**
     * Starting position of new track
     */
    private int mStartTime;

    /**
     * Ending position of new track
     */
    private int mEndTime;

    /**
     * Total length of track
     */
    private int mLength;

    /**
     * Default value constructor
     */
    public EditTrack() {

        mStartTime = 0;
        mEndTime = 0;
    }

    /**
     * @param startTimeInMills The starting time of the new track
     */
    public void setStartPosition(int startTimeInMills) {

        if (startTimeInMills < 0) {
            throw new InvalidParameterException("Start time cannot be less than zero");
        }

        // assign variables
        mStartTime = startTimeInMills;
    }

    /**
     * Add the position to the current track
     * @param endTimeInMills Position of end cut
     */
    public void setEndPosition(int endTimeInMills) {

        // track must have end time > 0
        if(endTimeInMills < 0) {
            throw new InvalidParameterException("End time cannot be less than zero");
        }
        // end position must be greater than start position
        if(endTimeInMills <= mStartTime) {
            throw new InvalidParameterException("End time must be greater than start time");
        }

        // assign variables
        mEndTime = endTimeInMills;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }
}
