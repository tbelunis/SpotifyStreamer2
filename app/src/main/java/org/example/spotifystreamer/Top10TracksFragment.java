/*
 * Copyright (C) 2015 Tom Belunis
 */

package org.example.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Retrieve and display the top 10 tracks for the artist whose Spotify ID
 * is passed in the intent.
 */
public class Top10TracksFragment extends Fragment {
    private static final int WIDTH_IN_PIXELS_FOR_LIST = 200;
    private static final int WIDTH_IN_PIXELS_FOR_PLAYER = 640;
    private final String TAG = this.getClass().getSimpleName();
    private Top10TracksAdapter mAdapter;
    private ArrayList<Top10TracksResult> mResults = new ArrayList<Top10TracksResult>();
    private String mSpotifyId;
    private String mArtistName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        mSpotifyId = intent.getStringExtra(Constants.SPOTIFY_ID);
        mArtistName = intent.getStringExtra(Constants.ARTIST_NAME);
        return inflater.inflate(R.layout.artist_top10tracks_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.track_list_recyclerview);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            mAdapter = new Top10TracksAdapter(getActivity());
            recyclerView.setAdapter(mAdapter);

            /**
             * Start the task to retrieve the top 10 tracks.
             */
            Top10TracksTask task = new Top10TracksTask();
            task.execute(mSpotifyId);
        }

    }

    /**
     * If tracks have been retrieved, save them in the bundle to
     * retrieve when the view is restored
     *
     * @param outState bundle with saved state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mResults != null) {
            outState.putParcelableArrayList("tracks", mResults);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * If savedInstanceState is not null, then retrieve the tracks
     * from the bundle and notify the adapter that the data set
     * has changed.
     *
     * @param savedInstanceState bundle
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mResults = savedInstanceState.getParcelableArrayList("tracks");
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * Load the ArrayList of Top10TracksResult with the tracks
     * retrieved from Spotify
     *
     * @param tracks tracks returned from call to Spotify
     */
    private void loadTop10Tracks(Tracks tracks) {
        mResults.clear();
        for (Track track : tracks.tracks) {
            SpotifyImagePicker imageHandler = new SpotifyImagePicker(track.album.images);
            String listItemImageUrl = imageHandler.getImageForSize(WIDTH_IN_PIXELS_FOR_LIST);
            String imageUrl = imageHandler.getImageForSize(WIDTH_IN_PIXELS_FOR_PLAYER);
            String albumTitle = track.album.name;
            String trackTitle = track.name;
            String previewUrl = track.preview_url;

            Top10TracksResult result = new Top10TracksResult(mArtistName, listItemImageUrl, imageUrl, albumTitle, trackTitle, previewUrl);
            mResults.add(result);
        }
        mAdapter.notifyDataSetChanged();
    }

    private class Top10TracksAdapter extends RecyclerView.Adapter<Top10TracksViewHolder> {
        private Context mContext;

        public Top10TracksAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Top10TracksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.track_list_item, parent, false);
            return new Top10TracksViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Top10TracksViewHolder holder, int position) {
            Top10TracksResult result = mResults.get(position);
            holder.mAlbumTitle.setText(result.getAlbumTitle());
            holder.mTrackTitle.setText(result.getTrackTitle());

            // Place the previewUrl in the tag so it is available when the
            // user clicks on a list item.
            holder.itemView.setTag(position);

            Picasso.with(mContext)
                    .load(result.getListItemImageUrl())
                    .fit()
                    .into(holder.mAlbumImage);
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }
    }

    private class Top10TracksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mAlbumImage;
        private TextView mAlbumTitle;
        private TextView mTrackTitle;

        public Top10TracksViewHolder(View itemView) {
            super(itemView);
            mAlbumImage = (ImageView) itemView.findViewById(R.id.track_list_item_imageview);
            mAlbumTitle = (TextView) itemView.findViewById(R.id.track_list_item_album_name);
            mTrackTitle = (TextView) itemView.findViewById(R.id.track_list_item_track_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), TrackPlayerActivity.class);
            intent.putExtra(Constants.TRACK_TO_PLAY, Integer.parseInt(view.getTag().toString()));
            intent.putExtra(Constants.TRACKS, mResults);
            startActivity(intent);
        }
    }

    private class Top10TracksTask extends AsyncTask<String, Void, Tracks> {

        /**
         * Run the top 10 tracks request in the background.
         *
         * @param params the Spotify Id of the artist
         * @return Tracks object with the results of the call to Spotify
         */
        @Override
        protected Tracks doInBackground(String... params) {
            String spotifyId = params[0];
            Log.d(TAG, "Retrieving tracks for " + spotifyId);
            HashMap<String, Object> queryParams = new HashMap<String, Object>();
            SpotifyApi spotifyApi = new SpotifyApi();

            // The top tracks call to Spotify requires the 2-character country code.
            SpotifyService spotify = spotifyApi.getService();
            queryParams.put(SpotifyService.COUNTRY, "US");

            Tracks tracks = null;
            try {
                tracks = spotify.getArtistTopTrack(spotifyId, queryParams);
            } catch (RetrofitError e) {
                Log.e(TAG, e.getLocalizedMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                getActivity().getResources().getString(R.string.network_problem_detected),
                                Toast.LENGTH_SHORT);
                    }
                });
            }
            return tracks;
        }

        // If no tracks are returned display a Toast message to the user.
        // Otherwise, load the results.
        @Override
        protected void onPostExecute(Tracks tracks) {
            if (tracks == null || tracks.tracks.isEmpty()) {
                Resources resources = getActivity().getResources();
                String message = String.format(resources.getString(R.string.no_tracks_found), mArtistName);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            } else {
                loadTop10Tracks(tracks);
            }
        }
    }
}
