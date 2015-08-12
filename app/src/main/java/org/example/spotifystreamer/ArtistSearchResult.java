/*
 * Copyright (C) 2015 Tom Belunis
 */

package org.example.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtistSearchResult implements Parcelable {
    private String artistName;
    private String spotifyId;
    private String imageUrl;

    public ArtistSearchResult(String artistName, String spotifyId, String imageUrl) {
        this.artistName = artistName;
        this.spotifyId = spotifyId;
        this.imageUrl = imageUrl;
    }

    private ArtistSearchResult(Parcel parcel) {
        artistName = parcel.readString();
        spotifyId = parcel.readString();
        imageUrl = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(artistName);
        out.writeString(spotifyId);
        out.writeString(imageUrl);
    }

    public static final Parcelable.Creator<ArtistSearchResult> CREATOR;

    static {
        CREATOR = new Creator<ArtistSearchResult>() {

            @Override
            public ArtistSearchResult createFromParcel(Parcel source) {
                return new ArtistSearchResult(source);
            }

            @Override
            public ArtistSearchResult[] newArray(int size) {
                return new ArtistSearchResult[size];
            }
        };
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
