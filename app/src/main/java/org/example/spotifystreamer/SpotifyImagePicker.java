/*
 * Copyright (C) 2015 Tom Belunis
 */

package org.example.spotifystreamer;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Chooses the URL desired image from the list of images returned
 * in the Spotify searches
 */
class SpotifyImagePicker {
    private List<Image> mImages;

    public SpotifyImagePicker(List<Image> images) {
        mImages = images;
    }

    /**
     * The images returned by Spotify are in descending order by width
     * the thumbnail image will be the smallest image. Find the smallest image
     * width and return the URL.
     *
     * @return url of the thumbnail image
     */
    public String getArtistThumbnailUrl() {
        String imageUrl = null;

        if (mImages != null && !mImages.isEmpty()) {
            Image image = mImages.get(mImages.size() - 1);
            imageUrl = image.url;
        }
        return imageUrl;
    }

    /**
     * Return the URL for the image that is closest to the desired width
     * in widthInPixels.
     *
     * @param widthInPixels desired width in pixels for the image
     * @return url of image closest to desired width
     */
    public String getImageForSize(int widthInPixels) {
        String imageUrl = null;
        int difference = Integer.MAX_VALUE;

        if (mImages != null) {
            for (Image image : mImages) {
                if (imageUrl == null) {
                    imageUrl = image.url;
                    difference = image.width - widthInPixels;
                    if (difference == 0) {
                        break;
                    }
                } else {
                    int newDifference = image.width - widthInPixels;
                    if (newDifference > 0 && newDifference < difference) {
                        imageUrl = image.url;
                        difference = newDifference;
                    }
                }

            }
        }
        return imageUrl;
    }
}
