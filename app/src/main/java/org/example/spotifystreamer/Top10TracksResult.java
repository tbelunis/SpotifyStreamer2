/*
 * Copyright (C) 2015 Tom Belunis
 */

package org.example.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class Top10TracksResult implements Parcelable {
    private String mArtistName;
    private String mListItemImageUrl;
    private String mImageUrl;
    private String mAlbumTitle;
    private String mTrackTitle;
    private String mPreviewUrl;

    public Top10TracksResult(String artistName, String listItemImageUrl, String imageUrl, String albumTitle, String trackTitle, String previewUrl) {
        mArtistName = artistName;
        mListItemImageUrl = listItemImageUrl;
        mImageUrl = imageUrl;
        mAlbumTitle = albumTitle;
        mTrackTitle = trackTitle;
        mPreviewUrl = previewUrl;
    }

    private Top10TracksResult(Parcel parcel) {
        mArtistName = parcel.readString();
        mListItemImageUrl = parcel.readString();
        mImageUrl = parcel.readString();
        mAlbumTitle = parcel.readString();
        mTrackTitle = parcel.readString();
        mPreviewUrl = parcel.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mArtistName);
        out.writeString(mListItemImageUrl);
        out.writeString(mImageUrl);
        out.writeString(mAlbumTitle);
        out.writeString(mTrackTitle);
        out.writeString(mPreviewUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Top10TracksResult> CREATOR;

    static {
        CREATOR = new Creator<Top10TracksResult>() {
            @Override
            public Top10TracksResult createFromParcel(Parcel source) {
                return new Top10TracksResult(source);
            }

            @Override
            public Top10TracksResult[] newArray(int size) {
                return new Top10TracksResult[size];
            }
        };
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getListItemImageUrl() {
        return mListItemImageUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTrackTitle() {
        return mTrackTitle;
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }


}
