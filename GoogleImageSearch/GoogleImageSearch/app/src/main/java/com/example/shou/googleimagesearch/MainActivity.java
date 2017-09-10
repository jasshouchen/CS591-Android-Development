package com.example.shou.googleimagesearch;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import java.util.ArrayList;

import static com.example.shou.googleimagesearch.ImageAdapter.url_des;
import static com.example.shou.googleimagesearch.ImageAdapter.url_bitmap;

/**
 * Created by shou on 4/15/2017.
 */

public class MainActivity extends Activity {
    private final String[] place_versions = {url_des};
    private String[] place_image_urls = {url_bitmap};
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;
    private TravelListAdapter mAdapter;
    private boolean isListView;
    private Menu menu;

    @Override protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setUpActionBar();

        Bundle bundle = getIntent().getExtras();
        String url= bundle.getString("imageParcel");
        place_image_urls[0] = url;

        initViews();


    }

    public void initViews() {
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        ArrayList placeVersions = prepareData();
        TravelListAdapter adapter = new TravelListAdapter(getApplicationContext(),placeVersions);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList prepareData(){

        ArrayList place_version = new ArrayList<>();
        for(int i=0;i< place_versions.length;i++){
            Place placeVersion = new Place();
            placeVersion.setPlace_version_name(place_versions[i]);
            placeVersion.setPlaceimage_url(place_image_urls[i]);
            place_version.add(placeVersion);
        }
        return place_version;
    }



    private void setUpActionBar() {
        if (toolbar != null) {
            setActionBar(toolbar);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setElevation(7);
        }
    }

}
