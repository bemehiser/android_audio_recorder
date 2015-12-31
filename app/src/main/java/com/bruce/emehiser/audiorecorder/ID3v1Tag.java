package com.bruce.emehiser.audiorecorder;

import java.security.InvalidParameterException;

/**
 * Created by Bruce Emehiser on 12/23/2015.
 *
 * Wrapper used to contain the information
 * about an audio track
 */
public class ID3v1Tag {

    /**
     * Metadata for track
     */
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private String mYear;
    private String mComment;
    private char mGenre;

    /**
     * Default constructor
     */
    public ID3v1Tag() {

        // all tracks are set to default
        mTitle = "Unknown";
        mArtist = "Unknown";
        mAlbum = "Unknown";
        mYear = "";
        mComment = "";
        mGenre = '0';
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {

        if(title.length() > 30) {
            throw new InvalidParameterException("Title has max length of 30 characters");
        }
        mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {

        if(artist.length() > 30) {
            throw new InvalidParameterException("Artist has max length of 30 characters");
        }
        mArtist = artist;
    }

    public String getAlbum() {

        return mAlbum;
    }

    public void setAlbum(String album) {

        if(album.length() > 30) {
            throw new InvalidParameterException("Album has max length of 30 characters");
        }
        mAlbum = album;
    }

    public char getGenre() {
        return mGenre;
    }

    public void setGenre(char genre) {
        mGenre = genre;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {

        if(year.length() > 4) {
            throw new InvalidParameterException("Year has max length of 4 characters");
        }
        mYear = year;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {

        if(comment.length() > 30) {
            throw new InvalidParameterException("Comment has max length of 30 characters");
        }

        mComment = comment;
    }

    /**
     * Get the byte[] representation of this tag
     * @return The byte[] representation of the ID3v1 tag
     */
    public byte[] getTag() {

        byte[] tag = new byte[128];

        // write tag
        writeTag(tag, "TAG".getBytes(), 0, 3);

        // write title
        writeTag(tag, mTitle.getBytes(), 3, 30);

        // write artist
        writeTag(tag, mArtist.getBytes(), 33, 30);

        // write album
        writeTag(tag, mAlbum.getBytes(), 63, 30);

        // write year
        writeTag(tag, mYear.getBytes(), 93, 4);

        // write comment
        writeTag(tag, mComment.getBytes(), 97, 30);

        // write genre
        writeTag(tag, new byte[] {(byte) mGenre}, 127, 1);

        return tag;
    }

    /**
     * Copy bytes from byte array to position in second byte array
     * pad extra spaces with ascii '0's
     * @param tag The byte array being copied into
     * @param subTag The byte array being copied from
     * @param start The start position in the byte array being copied into
     * @param length The max number of bytes to copy/pad
     */
    private void writeTag(byte[] tag, byte[] subTag, int start, int length) {

        // copy bytes from subTag to tag in given position
        int i;
        for(i = 0; i < subTag.length && i < length; i ++) {
            tag[start + i] = subTag[i];
        }
        // if there are extra positions, pad them with ascii zeros
        for(; i < length; i ++) {
            tag[start + i] = '0';
        }
    }

    @Override
    public String toString() {
        return String.format("Title:%s Artist:%s Album:%s Year:%s Comment:%s Genre:%c", mTitle, mArtist, mAlbum, mYear, mComment, mGenre);
    }
}