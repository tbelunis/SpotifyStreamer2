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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import org.example.spotifystreamer.ArtistSearchResult;
import org.example.spotifystreamer.Constants;
import org.example.spotifystreamer.SpotifyImagePicker;
import org.example.spotifystreamer.Top10TracksActivity;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.RetrofitError;

import java.util.ArrayList;

/**
 * Present the user with an edit field to enter search criteria. Runs the search and displays the results.
 */
public class ArtistSearchFragment extends Fragment implements TextView.OnEditorActionListener, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private ArtistSearchResultsAdapter mAdapter;

    private ArrayList<ArtistSearchResult> mResults = new ArrayList<ArtistSearchResult>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_search_fragment, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.artist_list_recyclerview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EditText searchQueryText;
        searchQueryText = (EditText) getView().findViewById(R.id.search_text);
        if (searchQueryText != null) {
            searchQueryText.setOnEditorActionListener(this);
        }

        mRecyclerView = (RecyclerView)getView().findViewById(R.id.artist_list_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new ArtistSearchResultsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        setRetainInstance(true);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == (EditorInfo.IME_ACTION_DONE)) {
            SpotifyArtistSearchTask task = new SpotifyArtistSearchTask();
            task.execute(v.getText().toString());
        }
        return false;
    }

    /**
     * If there are search results, save them in the bundle to
     * retrieve when the view is restored
     *
     * @param outState the bundle with the saved state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mResults != null) {
            outState.putParcelableArrayList("artists", mResults);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * If savedInstanceState is not null, then retrieve the search
     * results from the bundle and notify the adapter that the
     * data set has changed.
     *
     * @param savedInstanceState bundle
     */
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mResults = savedInstanceState.getParcelableArrayList("artists");
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }

        }
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * Populate ArrayList of ArtistSearchResult objects with the
     * results of the search. Notify the adapter that the
     * data set has changed.
     *
     * @param pager the ArtistsPager with the search results
     */
    private void loadArtistSearchResults(ArtistsPager pager) {
        mResults.clear();
        Pager<Artist> artists = pager.artists;
        for (Artist artist : artists.items) {
            String imageUrl = null;

            // If we have images in the result, get the url of the artist thumbnail image
            if (artist.images != null && !artist.images.isEmpty()) {
                SpotifyImagePicker imageHandler = new SpotifyImagePicker(artist.images);
                imageUrl = imageHandler.getArtistThumbnailUrl();
            }
            ArtistSearchResult result = new ArtistSearchResult(
                    artist.name,
                    artist.id,
                    imageUrl
            );
            mResults.add(result);
        }
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    /**
     * When the user clicks on an item in the list, start the Top10TracksActivity
     * to retrieve the top 10 tracks for that artist.
     *
     * @param view the list item that was clicked
     */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), Top10TracksActivity.class);
        ArtistSearchResult result = (ArtistSearchResult) view.getTag();

        // Pass the Spotify ID and the artist's name in the intent.
        // Spotify ID is needed for the top tracks search. The artist's
        // name is used in the subtitle of the ActionBar in the
        // Top10TracksActivity.
        intent.putExtra(Constants.SPOTIFY_ID, result.getSpotifyId());
        intent.putExtra(Constants.ARTIST_NAME, result.getArtistName());
        startActivity(intent);
    }

    private class ArtistSearchResultsAdapter extends RecyclerView.Adapter<ArtistSearchResultViewHolder> {
        private Context context;

        public ArtistSearchResultsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public ArtistSearchResultViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.artist_search_list_item, viewGroup, false);
            return new ArtistSearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ArtistSearchResultViewHolder artistSearchResultViewHolder, int i) {
            ArtistSearchResult result = mResults.get(i);
            String artistName = result.getArtistName();
            String imageUrl = result.getImageUrl();

            artistSearchResultViewHolder.artistName.setText(artistName);
            artistSearchResultViewHolder.itemView.setTag(result);

            if (imageUrl != null) {
                Picasso.with(context)
                        .load(imageUrl)
                        .fit()
                        .into(artistSearchResultViewHolder.artistImage);
            }

        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }
    }

    private class ArtistSearchResultViewHolder extends RecyclerView.ViewHolder {
        private ImageView artistImage;
        private TextView artistName;

        public ArtistSearchResultViewHolder(View itemView) {
            super(itemView);
            artistImage = (ImageView)itemView.findViewById(R.id.artist_list_item_imageview);
            artistName = (TextView)itemView.findViewById(R.id.artist_list_item_artist_name);
            itemView.setOnClickListener(ArtistSearchFragment.this);
        }
    }

    private class SpotifyArtistSearchTask extends AsyncTask<String, Void, ArtistsPager> {
        private String mSearchTerm;

        /**
         * Runs the artist search query in the background
         * @param params the search string for the query
         * @return an ArtistsPager containing results from query, if no results then null
         */
        @Override
        protected ArtistsPager doInBackground(String... params) {
            Log.d(TAG, "Running search with search term " + params[0]);
            mSearchTerm = params[0];
            ArtistsPager artistPager = null;
            if (mSearchTerm != null && !mSearchTerm.isEmpty()) {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotify = spotifyApi.getService();
                try {
                    artistPager = spotify.searchArtists(mSearchTerm);
                } catch (RetrofitError e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getResources().getString(R.string.network_problem_detected),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            return artistPager;
        }


        /**
         * If the search returns no results, display a Toast message to the user.
         * Otherwise, load the results.
         * @param artistsPager the result from the doInBackground method
         */
        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            if (artistsPager == null || artistsPager.artists.items.isEmpty()) {
                Context context = getActivity();
                Resources resources = getActivity().getResources();
                String message = String.format(resources.getString(R.string.no_search_results), mSearchTerm);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else {
                loadArtistSearchResults(artistsPager);
            }
        }
    }
}
