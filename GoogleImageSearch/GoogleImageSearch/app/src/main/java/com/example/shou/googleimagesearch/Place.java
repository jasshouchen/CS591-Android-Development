package com.example.shou.googleimagesearch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Created by shou on 4/15/2017.
 */

public class Place {
    public String placeName;
    public String placeimage_url;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlace_version_name(String place_version_name) {
        this.placeName = place_version_name;
    }
    public String getPlaceImage() {
        return placeimage_url;
    }

    public void setPlaceimage_url(String placeimage_url) {
        this.placeimage_url = placeimage_url;
    }
}
