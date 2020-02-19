package com.example.nreader.util;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MediaPlayerHelper {
    private MediaPlayer mediaPlayer;
    private volatile boolean isPlaying = false;
    private LinkedList<String> recordQueue = new LinkedList<>();

    private  MediaPlayerHelper() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
    }

    private static class SingletonHolder {
        private static MediaPlayerHelper instance = new MediaPlayerHelper();
    }

    public static MediaPlayerHelper getInstance() {
        return SingletonHolder.instance;
    }

    public synchronized void play(File dir) {
        play(dir, null);
    }

    public synchronized void play(File dir, List<String> array) {
        if (isPlaying)
            return;
        Log.d("play_dir", dir.toString());
        if (array == null) {
            //play all files under the dir
            for (File f : dir.listFiles()) {
                String s = f.getAbsolutePath();
                if (s.endsWith(Common.SUFFIX_WAV)) {
                    recordQueue.offer(s);
                    Log.d("play_add", s);
                }
            }
        } else {
            for (String s : array) {
                if (!s.endsWith(Common.SUFFIX_WAV))
                    s += Common.SUFFIX_WAV;
                File f = new File(dir, s);
                if (!f.exists())
                    continue;
                Log.d("play_add", s);
                recordQueue.offer(f.getAbsolutePath());
            }
        }
        isPlaying = true;
        playNext();
    }

    private void playNext() {
        if (recordQueue.isEmpty()) {
            isPlaying = false;
            return;
        }
        String next = recordQueue.poll();
        Log.d("playNext", next);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(next);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("playNext", e.getMessage() + next);
            playNext();
        }
    }
}
