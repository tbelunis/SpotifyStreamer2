/*
 * Copyright (C) 2015 Tom Belunis
 */
package org.example.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


/**
 * Main entry point to the app. Displays the artist search fragment
 */
public class MainActivity extends ActionBarActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check to see if the fragment already exists. If it does, use it.
        FragmentManager fm = getFragmentManager();
        String SEARCH_FRAGMENT = "ArtistSearchFragment";
        ArtistSearchFragment artistSearchFragment = (ArtistSearchFragment) fm.findFragmentByTag(SEARCH_FRAGMENT);

        // If the fragment doesn't exist, create a new one and replace the
        // fragment container with the fragment.
        if (artistSearchFragment == null) {
            artistSearchFragment = new ArtistSearchFragment();
            artistSearchFragment.setRetainInstance(true);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.activity_main_fragment_container, artistSearchFragment, SEARCH_FRAGMENT);
            ft.commit();
        }
    }
}
