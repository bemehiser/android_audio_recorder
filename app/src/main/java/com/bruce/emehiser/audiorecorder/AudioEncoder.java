package com.bruce.emehiser.audiorecorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncoder {

    /**
     * Explicit value constructor
     * @param outputFile The file to write the encoded audio to
     * @param inputFile The file to read the pcm audio from
     * @throws FileNotFoundException
     */
    public static void encode2(File outputFile, File inputFile) throws FileNotFoundException, IOException {

        String name = "audio/mp4a-latm";

        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream;

        MediaCodec mCodec;
        MediaFormat mFormat;

        // create codec and format
//        mCodec = MediaCodec.createEncoderByType("audio/aac");
//        mFormat = MediaFormat.createAudioFormat("audio/aac", 11025, 1);

        // frequency 11025 kHz

        mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);

        mFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44000, 2);

        // redundant?
        mFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);
//        mFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectELD);
        mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44000);
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, 320000);
        mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);

        // configure codec
        mCodec.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        // get file streams or throw
        fileOutputStream = new FileOutputStream(outputFile);
        fileInputStream = new FileInputStream(inputFile);

        // get media codec
        mCodec = MediaCodec.createByCodecName(name);

        mCodec.configure(mFormat, null, null, 0);
        mCodec.start();

        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();

        // input buffer of 512 kB
        byte[] inputBuffer = new byte[1024 * 512];
        int read = 0;

        // for escaping the loop
        int cur = 0;
        int size = (int) inputFile.length();

        while(cur < size) {

            int inputBufferId = mCodec.dequeueInputBuffer(-1);
            if(inputBufferId >= 0) {
                // todo fill buffer with data
                // create new array

                // read bytes
                read = fileInputStream.read(inputBuffer, 0, inputBuffer.length);

                // update cur
                cur += read;

                // fill buffer
                inputBuffers[inputBufferId].put(inputBuffer, 0, read);
            }
            mCodec.queueInputBuffer(inputBufferId, 0, read, 0, 0);

            // output
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mCodec.dequeueOutputBuffer(bufferInfo, 0);

            // if output buffers are waiting to be processed
            if(outputBufferId >= 0) {
                // todo process output buffers
            }
            else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mCodec.getOutputBuffers();
            }
            else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // subsequent data will conform to new format
                MediaFormat format = mCodec.getOutputFormat();
            }
        }
        mCodec.stop();
        mCodec.release();
    }

    public static void encode(File outputFile, File inputFile) throws IOException {

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        FileInputStream inputStream = new FileInputStream(inputFile);

        MediaCodec codec = MediaCodec.createByCodecName("OMX.google.aac.encoder");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC); //fixed version
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 11025);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();

        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();

        boolean bEndInput = false;
        boolean bEndOutput = false;

        while(true)
        {
            if (!bEndInput)
            {
                int inputBufferIndex = codec.dequeueInputBuffer(0);
                if (inputBufferIndex >= 0)
                {
                    int nLen = readPCM(inputStream, inputBuffers[inputBufferIndex]);//This line read PCM, return 0 if end of data.
                    int nBufLen = inputBuffers[inputBufferIndex].capacity();

                    if (nLen == nBufLen)
                        codec.queueInputBuffer(inputBufferIndex, 0, nLen, 0,  MediaCodec.BUFFER_FLAG_SYNC_FRAME);
                    else if (nLen < nBufLen)
                    {
                        codec.queueInputBuffer(inputBufferIndex, 0, nLen, 0,  MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        bEndInput = true;
                        break;
                    }
                }
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            if (!bEndOutput)
            {
                int outputBufferIndex = codec.dequeueOutputBuffer(info, 0);
                if (outputBufferIndex  >= 0)
                {
                    int outBitsSize   = info.size;
                    Log.d("test", "Offset:"+info.offset);
                    Log.d("test", "Size:"+info.size);
                    Log.d("test", "Time:"+info.presentationTimeUs);
                    Log.d("test", "Flags:" + info.flags);
                    if (info.flags != 0) //fixed version
                    {
                        codec.releaseOutputBuffer(outputBufferIndex, false /* render */);
                        continue;
                    }

                    int outPacketSize = outBitsSize + 7;    // 7 is ADTS size
                    ByteBuffer outBuf = outputBuffers[outputBufferIndex];

                    outBuf.position(info.offset);
                    outBuf.limit(info.offset + outBitsSize);
                    try {
                        byte[] data = new byte[outPacketSize];  //space for ADTS header included
                        addADTStoPacket(data, outPacketSize);
                        outBuf.get(data, 7, outBitsSize);
                        outBuf.position(info.offset);
                        outputStream.write(data, 0, outPacketSize);  //open FileOutputStream beforehand
                    } catch (IOException e) {
                        Log.e("test", "failed writing bitstream data to file");
                        e.printStackTrace();
                    }

                    outBuf.clear();
                    codec.releaseOutputBuffer(outputBufferIndex, false /* render */);
                    Log.d("test", "  dequeued " + outBitsSize + " bytes of output data.");
                    Log.d("test", "  wrote " + outPacketSize + " bytes into output file.");

                    if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    {
                        bEndOutput = true;
                        //break;
                    }
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                {
                    outputBuffers = codec.getOutputBuffers();
                }
                else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                {

                }
            }

            if (bEndInput && bEndOutput)
                break;
        }
    }

    private static int readPCM(FileInputStream inputStream, ByteBuffer byteBuffer){

        int size = byteBuffer.capacity() / 2;

        byte[] buffer = new byte[size];

        try {
            inputStream.read(buffer, 0, size);

        } catch (IOException e) {
            Log.e("readPCM", "Error reading into buffer");
            return 0;
        }

        byteBuffer.put(buffer);

        return size;
    }

    /**
     *  Add ADTS header at the beginning of each and every AAC packet.
     *  This is needed as MediaCodec encoder generates a packet of raw
     *  AAC data.
     *
     *  Note the packetLen must count in the ADTS header itself.
     **/
    private static void addADTStoPacket(byte[] packet, int packetLen) {
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
}