//Refer: https://blog.csdn.net/imhxl/article/details/52190451, https://github.com/Zhangsongsong/AudioRecord
package com.example.nreader.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton
public class AudioRecorder {
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    //44100Hz is currently the only rate that is guaranteed to work on all devices
    //but other rates such as 22050, 16000, and 11025 may work on some devices.
    public static final int SAMPLE_RATE = 44100;
    //CHANNEL_IN_MONO is guaranteed to work on all devices.
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //PCM 16 bit per sample. Guaranteed to be supported by devices.
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private AudioRecord audioRecord;
    private volatile boolean isRecording = false;

    private AudioRecorder() {
    }

    private static class SingletonHolder {
        private static AudioRecorder instance = new AudioRecorder();
    }

    public static AudioRecorder getInstance() {
        return SingletonHolder.instance;
    }

    public synchronized void init() {
        //New audio data can be read from this buffer in smaller chunks than this size.
        if (audioRecord == null)
            audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG,
                    AUDIO_FORMAT, MIN_BUFFER_SIZE * 2);
        Log.d("MIN_BUFFER_SIZE", Integer.toString(MIN_BUFFER_SIZE));
    }

    public synchronized void release() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    public synchronized void startRecord(final File dir, final String fileName) {
        if (isRecording)
            return;
        isRecording = true;
        audioRecord.startRecording();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                File pcmFile = new File(dir, fileName);
                File wavFile = new File(dir, fileName + Common.SUFFIX_WAV);
                if (writePCMFile(pcmFile))
                    writeWAVFile(pcmFile, wavFile);
                if (pcmFile.exists())
                    pcmFile.delete();
            }
        });
    }

    private void writeWAVFile(File pcmFile, File wavFile) {
        byte[] audioData = new byte[MIN_BUFFER_SIZE];
        WaveHeader header = new WaveHeader((int) pcmFile.length());
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(pcmFile));
            bos = new BufferedOutputStream(new FileOutputStream(wavFile));
            bos.write(header.getHeader());
            while(bis.read(audioData) != -1){
                bos.write(audioData);
            }
        } catch (IOException e) {
            Log.e("writeWAVFile", e.getMessage());
        } finally {
            if (bis != null){
                try {
                    bis.close();
                } catch (IOException e) {
                    Log.e("bis_close", e.getMessage());
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.e("bos_close", e.getMessage());
                }
            }
        }

    }

    private boolean writePCMFile(File pcmFile) {
        byte[] audioData = new byte[MIN_BUFFER_SIZE];
        BufferedOutputStream bos = null;
        try {
            bos =  new BufferedOutputStream(new FileOutputStream(pcmFile));
            while (isRecording) {
                int readCount = audioRecord.read(audioData, 0, MIN_BUFFER_SIZE);
                if (readCount >= 0) {
                    bos.write(audioData);
                }
                Log.d("readCount", Integer.toString(readCount));
            }
            return true;
        } catch (IOException e) {
            Log.e("writePCMFile", e.getMessage());
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.e("bos_close", e.getMessage());
                }
            }
        }
    }

    public synchronized void stopRecord() {
        if (!isRecording)
            return;
        audioRecord.stop();
        isRecording = false;
    }

    //https://www.cnblogs.com/ranson7zop/p/7657874.html
    private class WaveHeader {
        public byte chunkID[] = {'R', 'I', 'F', 'F'};
        public int chunkSize; //from format to file end: 36 + PCM size
        public byte format[] = {'W', 'A', 'V', 'E'};
        public byte fmtChunkID[] = {'f', 'm', 't', ' '};
        public int fmtChunkSize = 16; //from audioFormat to bitsPerSample: 16
        public short audioFormat = 0x0001; //0x0001: PCM
        public short numChannels = 0x0001; //0x0001: CHANNEL_IN_MONO, 0x0002: CHANNEL_IN_STEREO
        public int sampleRate = SAMPLE_RATE;
        public int byteRate = numChannels * sampleRate * 16 / 8;
        public short blockAlign = (short) (numChannels * 16 / 8);
        public short bitsPerSample = 16; //AudioFormat.ENCODING_PCM_16BIT
        public byte dataChunkID[] = {'d','a','t','a'};
        public int dataChunkSize; //PCM size

        public WaveHeader(int dataChunkSize) {
            this.chunkSize = dataChunkSize + 36;
            this.dataChunkSize = dataChunkSize;
        }

        public byte[] getHeader() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(chunkID);
            writeInt(bos, chunkSize);
            bos.write(format);
            bos.write(fmtChunkID);
            writeInt(bos, fmtChunkSize);
            writeShort(bos,audioFormat);
            writeShort(bos, numChannels);
            writeInt(bos, sampleRate);
            writeInt(bos, byteRate);
            writeShort(bos, blockAlign);
            writeShort(bos, bitsPerSample);
            bos.write(dataChunkID);
            writeInt(bos,dataChunkSize);
            bos.flush();
            byte[] ret = bos.toByteArray();
            bos.close();
            return ret;
        }

        private void writeShort(ByteArrayOutputStream bos, int n) throws IOException {
            byte[] buf = new byte[2];
            buf[0] = (byte) (n & 0xff);
            buf[1] = (byte) ((n >> 8) & 0xff);
            bos.write(buf);
        }


        private void writeInt(ByteArrayOutputStream bos, int n) throws IOException {
            byte[] buf = new byte[4];
            buf[0] = (byte) (n & 0xff);
            buf[1] = (byte) ((n >> 8) & 0xff);
            buf[2] = (byte) ((n >> 16) & 0xff);
            buf[3] = (byte) ((n >> 24) & 0xff);
            bos.write(buf);
        }
    }
}
