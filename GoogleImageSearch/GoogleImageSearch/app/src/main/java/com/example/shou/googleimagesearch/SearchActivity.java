package com.example.shou.googleimagesearch;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.provider.SearchRecentSuggestions;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import static com.example.shou.googleimagesearch.ImageAdapter.url_des;
import static com.example.shou.googleimagesearch.ImageAdapter.url_bitmap;


public class SearchActivity extends AppCompatActivity implements  ActionMode.Callback {
    public GoogleImageLoader imageLoader;
    ImageAdapter adapter;
    int selectedPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);


        adapter = new ImageAdapter(this);

        imageLoader = new GoogleImageLoader(adapter);
        Intent intent  = getIntent();
        handleIntent(intent);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        adapter.url_des = imageLoader.query;
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                selectedPosition = pos;
                startSupportActionMode(SearchActivity.this);
                //                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Intent intent = new Intent(getApplicationContext(), ImageAdapter.class );
//                Intent intent2 = new Intent(getApplicationContext(), ImageAdapter.class );
                //                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Bundle bundle = new Bundle();
//                Bundle bundle2 = new Bundle();
//                bundle2.putString("url2", "https://en.wikipedia.org/wiki/" + imageLoader.query);
                //Add your data to bundle
                bundle.putSerializable("url", "https://en.wikipedia.org/wiki/" + imageLoader.query);
                intent.putExtras(bundle);
//                intent2.putExtras(bundle2);
//                startActivity(intent2);
                startActivity(intent);

            }
        });

        gridview.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
//                        imageLoader.loadMore();
                }

            }

            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });

    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it's present
//        getMenuInflater().inflate(R.menu.menu_main, menu);

        // link searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent (Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            /*
            String ACTION_SEARCH
            Activity Action: Perform a search.

            Input: getStringExtra(SearchManager.QUERY) is the text to search for. If empty, simply enter your search results Activity with the search UI activated.

            Output: nothing.

            Constant Value: "android.intent.action.SEARCH"
             */
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                                                                                     ImageSuggestionProvider.AUTHORITY, ImageSuggestionProvider.MODE );
            suggestions.saveRecentQuery(query, null);
            /*
            It will be shown in a smaller font, below the primary
            * suggestion.  When typing, matches in either line of text will be displayed in the list.
            * If you did not configure two-line mode, or if a given suggestion does not have any
            * additional text to display, you can pass null here.
             */
            query = "Boston";
            imageLoader.loadItems(query);
        }
        String query = intent.getStringExtra(SearchManager.QUERY);
        query = "Syria";
        imageLoader.loadItems(query);


    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.deleteimage,menu);

        return true;
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (R.id.action_delete == item.getItemId()) {
            imageLoader.removeItem(selectedPosition);
            mode.finish();
        }
        return false;
    }

    @Override public void onDestroyActionMode(ActionMode mode) {

    }
}
