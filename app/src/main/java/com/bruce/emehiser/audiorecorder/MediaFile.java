package com.bruce.emehiser.audiorecorder;

import android.net.Uri;

import java.io.Serializable;

/**
 * A wrapper class for holding the information of a single media file
 */
public class MediaFile implements Serializable {

    private String mArtist;
    private String mAlbum;
    private String mTitle;
    private String mGenre;
    private String mUriString;
    transient private Uri mUri;

    public MediaFile(){
        mArtist = null;
        mAlbum = null;
        mTitle = null;
        mGenre = null;
        mUriString = null;
        mUri = null;
    }

    public MediaFile(String artist, String album, String title, String genre, Uri uri) {

        if(artist == null || album == null || title == null || genre == null || uri == null) {
            throw new NullPointerException("Incoming parameters cannot be null");
        }
        mArtist = artist;
        mAlbum = album;
        mTitle = title;
        mGenre = genre;
        mUriString = uri.toString();
        mUri = uri;
    }

    public String getArtist() {
        if(mArtist != null)
            return mArtist;
        return "";
    }

    public String getAlbum() {
        if(mAlbum != null)
            return mAlbum;
        return "";
    }

    public String getTitle() {
        if(mTitle != null)
            return mTitle;
        return "";
    }

    public String getGenre() {
        if( mGenre != null )
            return mGenre;
        return "";
    }

    public Uri getUri() {
        if(mUri != null) {
            return mUri;
        }
        mUri = Uri.parse(mUriString);
        if(mUri != null) {
            return mUri;
        }
        // this should maybe be a file not found, or an IO exception
        throw new NullPointerException("Media File Uri does not exist");
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setGenre(String genre) {
        mGenre = genre;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}