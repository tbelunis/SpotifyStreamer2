/*
 * Copyright (C) 2015 Tom Belunis
 */

package org.example.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class Top10TracksActivity extends ActionBarActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10tracks);

        // Get the artist's name from the intent and use it as
        // the subtitle in the ActionBar.
        Intent intent = getIntent();
        String artistName = intent.getStringExtra(Constants.ARTIST_NAME);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(artistName);
        }
    }
}
