package com.example.shou.googleimagesearch;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by shou on 3/25/2017.
 */

public class ImageSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.shou.googleimagesearch.ImageSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
// this function copied from developer android manual: https://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
    public ImageSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
