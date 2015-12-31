package com.bruce.emehiser.audiorecorder;

public class AudioEditor {

    /**
     * Edit constant bit rate mp3 file
     * @param song The byte[] representation of the mp3 file
     * @param track The track to create
     * @return The edited song with ID3v1 tag included
     * @throws InvalidEditTrackException if unable to parse song with EditTrack
     */
    public static byte[] edit(byte[] song, EditTrack track) throws InvalidEditTrackException {

        // ID3 tag length in bytes
        final int ID3v1TagLength = 128;

        // boolean representing the presence of an ID3v1 tag
        boolean ID3v1Tag = false;
        int songLengthInBytes = song.length;

        // check for ID3v1 tag
        if(song.length >= 128) {

            byte[] tag = new byte[3];
            tag[0] = song[song.length - ID3v1TagLength];
            tag[1] = song[song.length - ID3v1TagLength + 1];
            tag[2] = song[song.length - ID3v1TagLength + 2];

            if(tag[0] == 't' && tag[1] == 'a' && tag[2] == 'g') {
                ID3v1Tag = true;
                songLengthInBytes = song.length - ID3v1TagLength;
            }
        }

        if(track.getLength() <= 0) {
            throw new InvalidEditTrackException("Length is equal to zero");
        }

        // get approximate start and end bytes
        int approximateStartByte = (int) (songLengthInBytes * ((double) track.getStartTime() / (double) track.getLength()));
        int approximateEndByte = (int) (songLengthInBytes * ((double) track.getEndTime() / (double) track.getLength()));

        int startByte = -1;
        int endByte = -1;

        // move through song from starting position looking for valid frame for start byte
        for(int i = approximateStartByte; i < songLengthInBytes - 1 && startByte == -1; i ++) {

            // if we find the frame header pattern
            if((song[i] & 0xFF) == 255 && (((song[i + 1] & 0xFF) >> 5) & 7) == 7) {
                // we have reached the start of a valid frame
                startByte = i;
            }
        }

        // if we actually got a start byte, look for an end byte
        if(startByte != -1) {
            for(int i = approximateEndByte; i < songLengthInBytes - 1 && endByte == -1; i ++) {

                // if we find the frame header pattern
                if((song[i] & 0xFF) == 255 && (((song[i + 1] & 0xFF) >> 5) & 7) == 7) {
                    // we have reached the start of a valid frame
                    endByte = i;
                }
            }
            // if we didn't find an end byte before the end of file, use songLengthInBytes
            if(endByte == -1) {
                endByte = songLengthInBytes;
            }
        }
        else {
            // the track is invalid for the song
            throw new InvalidEditTrackException();
        }

        // create new output song byte array
        int outSongSize = endByte - startByte;
        int outSongSizeWithTag = outSongSize + ID3v1TagLength;
        byte[] outSong = new byte[outSongSizeWithTag];

        // copy bytes from initial song to output song
        for(int i = 0; i < outSongSize; i ++) {
            outSong[i] = song[startByte + i];
        }

        // add ID3v1 tag to last 128 bytes of song
        byte[] tag = track.getTag();

        for(int i = 0; i < 128; i ++) {
            outSong[outSongSize + i] = tag[i];
        }

        return outSong;
    }
}

/**
 * Exception for invalid Edit Track
 */
class InvalidEditTrackException extends RuntimeException {

    public InvalidEditTrackException() {
        this("Invalid Track");
    }

    public InvalidEditTrackException(String description) {
        super(description);
    }

}