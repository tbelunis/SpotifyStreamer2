package org.example.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class TrackPreviewService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final String TAG = this.getClass().getSimpleName();

    private MediaPlayer mMediaPlayer;
    private int mCurrentPosition;
    private String mTrackUrl;
    private ArrayList<Top10TracksResult> mTracks;
    private final IBinder mPlayerBinder = new TrackPreviewBinder();
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initializePlayer();
    }

    public void initializePlayer() {
        Log.d(TAG, "initializePlayer");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackpreviewWakelock");
        mWakeLock.acquire();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }


    public void setTrackUrl(String trackUrl) {
        mTrackUrl = trackUrl;
    }

    public void playSong(String trackUrl) {
        Log.d(TAG, "playSong: " + trackUrl);
        Uri trackuri = Uri.parse(trackUrl);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), trackuri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        mMediaPlayer.pause();
    }

    public void resumePlaying() {
        mMediaPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mTracks = intent.getParcelableArrayListExtra(Constants.TRACKS);
        mCurrentPosition = intent.getIntExtra(Constants.TRACK_TO_PLAY, 0);
        return mPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
//        mMediaPlayer.release();
        mWakeLock.release();
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Track Preview Service onDestroy");
        super.onDestroy();
        mMediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared called");
        mMediaPlayer.start();
    }

    public class TrackPreviewBinder extends Binder {
        TrackPreviewService getService() {
            Log.d(TAG, "getService");
            return TrackPreviewService.this;
        }
    }
}
