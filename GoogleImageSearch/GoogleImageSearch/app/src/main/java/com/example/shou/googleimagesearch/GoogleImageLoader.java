package com.example.shou.googleimagesearch;

/**
 * Created by shou on 3/25/2017.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class GoogleImageLoader extends AppCompatActivity{
    SearchResults currSearch;
    List<ImageResult> images;
    String query;
    ImageAdapter adapter;

    public GoogleImageLoader (ImageAdapter adapter) {
        this.adapter = adapter;
        images = new ArrayList<>();
    }
    public String getURL() {
        return query;
    }

    public void loadItems(String query) {
        try {
            String newQuery = URLEncoder.encode(query, "utf-8");
            if (!newQuery.equalsIgnoreCase(this.query)) { // ignore uppercase or lowercase equal
                this.query = newQuery;
                adapter.url_des = this.query;
                currSearch = null;
                images.clear();
                adapter.setImages(images);
                new RequestImageTask().execute();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void updateImages() {
        if (currSearch == null || currSearch.getItems() == null) {
            return;
        }

        images.add(currSearch.getItems());
        System.out.println("image size: " + images.size());
        adapter.setImages(images);
    }

    public void loadMore() {
        if (query != null) {
            new RequestImageTask().execute();
        }
    }
    public void removeItem(int position) {
        images.remove(position);
        adapter.setImages(images);
    }


    public class RequestImageTask extends AsyncTask<Void, Integer, Void> {
        @Override protected Void doInBackground(Void... params) {
            /*
            doInBackground(Params...), invoked on the background thread immediately after onPreExecute() finishes executing. This step is used to perform background computation that can take a long time. The parameters of the asynchronous task are passed to this step. The result of the computation must be returned by this step and will be passed back to the last step. This step can also use publishProgress(Progress...) to publish one or more units of progress. These values are published on the UI thread, in the onProgressUpdate(Progress...) step.
             */
            String key = "AIzaSyAGyWZnhaCdaPZ_YItXvCm0RZa9tbbQL7M";
            String cx = "006227774635939620012:n_itghgx1km";
            int startIndex;
            if (currSearch == null) {
                startIndex = 1;
            } else {
                startIndex = currSearch.getNextIndex();
            }

            URL url = null;


            try {
                url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" +
                                      cx + "&start=" + startIndex + "&q=" + query + "&imgSize=medium&searchType=image&enableImageSearch=true&alt=json&imgSize=icon");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                currSearch = new Gson().fromJson(br, SearchResults.class);
                connection.disconnect();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override protected void onPostExecute(Void Params) {
            super.onPostExecute(Params);
            updateImages();

        }




    }


}
