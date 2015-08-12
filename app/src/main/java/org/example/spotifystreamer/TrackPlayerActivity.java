package org.example.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class TrackPlayerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);

        Intent intent = getIntent();
        ArrayList<Top10TracksResult> tracks = intent.getParcelableArrayListExtra(Constants.TRACKS);
        int position = intent.getIntExtra(Constants.TRACK_TO_PLAY, 0);

        TrackPlayerFragment fragment;

        if (getFragmentManager().findFragmentById(R.id.track_player_fragment) == null) {
            fragment = new TrackPlayerFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.player_fragment, fragment).commit();
        } else {
            fragment = (TrackPlayerFragment) getFragmentManager().findFragmentById(R.id.track_player_fragment);
        }

        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.TRACKS, tracks);
        args.putInt(Constants.TRACK_TO_PLAY, position);

    }
}
