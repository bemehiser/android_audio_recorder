package com.bruce.emehiser.audiorecorder;

/**
 * Created by Bruce Emehiser on 12/23/2015.
 *
 * Wrapper used to contain the information
 * about an audio track
 */
public class Track {

    /**
     * Metadata for track
     */
    private String mArtist;
    private String mAlbum;
    private String mTitle;
    private String mGenre;
    private int mYear;
    private String mDescription;
    private int mLength;

    /**
     * Default constructor
     */
    public Track() {

        // all tracks are set to default
        mArtist = "Unknown";
        mAlbum = "Unknown";
        mTitle = "Unknown";
        mGenre = "Unknown";
        mYear = 0;
        mDescription = "Unknown";
        mLength = 0;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String genre) {
        mGenre = genre;
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        mYear = year;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
    }
}