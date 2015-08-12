package org.example.spotifystreamer;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrackPlayerFragment extends DialogFragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private ArrayList<Top10TracksResult> mTracks;
    private int mCurrentPosition;
    private boolean mIsPlaying;
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mPreviousTrack;
    private ImageButton mNextTrack;
    private TextView mArtistName;
    private TextView mAlbumName;
    private TextView mTrackTitle;
    private ImageView mTrackImage;
    private TrackPreviewService mTrackPreviewService;
    private Intent mPlayerIntent;
    private boolean mIsPlayerBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        mTracks = intent.getParcelableArrayListExtra(Constants.TRACKS);
        mCurrentPosition = intent.getIntExtra(Constants.TRACK_TO_PLAY, 0);

        View view = inflater.inflate(R.layout.track_player_fragment, container, false);

        mArtistName = (TextView) view.findViewById(R.id.track_artist_name);
        mAlbumName = (TextView) view.findViewById(R.id.track_album_name);
        mTrackTitle = (TextView) view.findViewById(R.id.track_track_title);
        mTrackImage = (ImageView) view.findViewById(R.id.track_image);
        mPlayButton = (ImageButton) view.findViewById(R.id.track_button_play);
        mPlayButton.setOnClickListener(this);
        mPauseButton = (ImageButton) view.findViewById(R.id.track_button_pause);
        mPauseButton.setOnClickListener(this);
        mPreviousTrack = (ImageButton) view.findViewById(R.id.track_button_prev);
        mPreviousTrack.setOnClickListener(this);
        mNextTrack = (ImageButton) view.findViewById(R.id.track_button_next);
        mNextTrack.setOnClickListener(this);

        setView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mPlayerIntent == null) {
            mPlayerIntent = new Intent(getActivity(), TrackPreviewService.class);
            getActivity().bindService(mPlayerIntent, playerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mPlayerIntent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsPlayerBound) {
            getActivity().unbindService(playerConnection);
            mIsPlayerBound = false;
        }
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackPreviewService.TrackPreviewBinder binder = (TrackPreviewService.TrackPreviewBinder) service;
            mTrackPreviewService = binder.getService();
            mIsPlayerBound = true;
            Log.d(TAG, "onServiceConnected finished");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsPlayerBound = false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.TRACKS, mTracks);
        outState.putInt(Constants.TRACK_TO_PLAY, mCurrentPosition);
        outState.putBoolean("PLAYER_BOUND", mIsPlayerBound);
        outState.putBoolean("IS_PLAYING", mIsPlaying);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList(Constants.TRACKS);
            mCurrentPosition = savedInstanceState.getInt(Constants.TRACK_TO_PLAY);
            mIsPlayerBound = savedInstanceState.getBoolean("PLAYER_BOUND");
            mIsPlaying = savedInstanceState.getBoolean("IS_PLAYING");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlayButton.setVisibility(mIsPlaying ? View.INVISIBLE : View.VISIBLE);
                    mPauseButton.setVisibility(mIsPlaying ? View.VISIBLE : View.INVISIBLE);

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.track_button_play:
                playTrack();
                break;
            case R.id.track_button_pause:
                pauseTrack();
                break;
            case R.id.track_button_prev:
                moveToPreviousTrack();
                break;
            case R.id.track_button_next:
                moveToNextTrack();
                break;
        }
    }

    private void moveToPreviousTrack() {
        if (mCurrentPosition > 0) {
            mCurrentPosition--;
            setView();
            mIsPlaying = false;
            playTrack();
        }
    }

    private void moveToNextTrack() {
        if (mCurrentPosition < mTracks.size()) {
            mCurrentPosition++;
            setView();
            mIsPlaying = false;
            playTrack();
        }
    }

    private void playTrack() {
        Log.d(TAG, "playTrack");
        if (mIsPlaying) {
            mTrackPreviewService.resumePlaying();
        } else {
            mTrackPreviewService.playSong(mTracks.get(mCurrentPosition).getPreviewUrl());
            mPlayButton.setVisibility(View.INVISIBLE);
            mPauseButton.setVisibility(View.VISIBLE);
            mIsPlaying = true;
        }
    }

    private void pauseTrack() {
        mTrackPreviewService.pauseSong();
        mPauseButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
    }

    private void setView() {
        Top10TracksResult track = mTracks.get(mCurrentPosition);
        mArtistName.setText(track.getArtistName());
        mAlbumName.setText(track.getAlbumTitle());
        mTrackTitle.setText(track.getTrackTitle());
        Picasso.with(getActivity())
                .load(track.getImageUrl())
                .into(mTrackImage);
        mPreviousTrack.setEnabled(mCurrentPosition > 0);
        mNextTrack.setEnabled(mCurrentPosition < mTracks.size());
    }
}
