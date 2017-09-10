package com.example.shou.googleimagesearch;

/**
 * Created by shou on 3/25/2017.
 */
import com.google.gson.annotations.SerializedName;
public class ImageResult {
    @SerializedName("title")
    private String name;
    private String link;

    public String getName() {return name;}
    public String getUrl() {return link;}
}
