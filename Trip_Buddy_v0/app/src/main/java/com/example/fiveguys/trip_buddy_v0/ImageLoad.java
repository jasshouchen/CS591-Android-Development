package com.example.fiveguys.trip_buddy_v0;

/**
 * Created by shou on 4/26/2017.
 */
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.provider.SearchRecentSuggestions;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fiveguys.trip_buddy_v0.groupchannel.CreateGroupChannelActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Created by shou on 3/25/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import static java.security.AccessController.getContext;

/**
 * a pulic class adapter extends from BaseAdapter, passing view from adapter to gridview in main
 * activity
 */
public class ImageLoad extends BaseAdapter {
    private Context mContext;
    private String[] images2;
    private String[] descriptions2;
    private  Integer[] numberlist2;
    private FirebaseDatabase database;
    private DataSnapshot dataSnapshot;
    private String uid;
    private ArrayList<List<String>> matchNum;
    private EditPlayerAdapterCallback callback;

    // ImageLoad constructor passing from main activity to adapter, passes snapshot from Main
    // activity
    public ImageLoad(Context c, DataSnapshot Ref, List<String> images,List<String> descriptions,
                     EditPlayerAdapterCallback edt) {
        callback = edt;
        mContext = c;
        descriptions2 = new String[descriptions.size()];
        dataSnapshot = Ref;
        images2 = new String[images.size()];
        numberlist2 = new Integer[images.size()];
        for (int i=0;i<descriptions.size(); i++) {
            descriptions2[i] = descriptions.get(i);
            images2[i] = images.get(i);
        }

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return descriptions2.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.gridview_text_img, null);
        } else {
            grid = (View)convertView;
        }
        TextView textView = (TextView) grid.findViewById(R.id.textView);
        final ImageView imageView = (ImageView)grid.findViewById(R.id.imageView);
        Button btndelete = (Button)grid.findViewById(R.id.btnDelete);
        final TextView matchNumber = (TextView) grid.findViewById(R.id.matchNumber);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        matchNum = new ArrayList<List<String>>();
        if(user != null)
        uid = user.getUid(); // get uid for current user
        database = FirebaseDatabase.getInstance();
                DataSnapshot tripref = dataSnapshot.child("users").child(uid).child("trips"); //
        // goes to users table in firebase to do query
                for (DataSnapshot snapshot : tripref.getChildren()) {
                    String destid = snapshot.getKey(); // only one destination
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        String strt = sp.child("startAddress").getValue(String.class);
                        if (destid != null && strt != null) {
                            DatabaseReference newRef = database.getReference("trips/" + destid + "/" +strt.toString());
                            newRef.addValueEventListener(new ValueEventListener() {
                                List<String> sublist = new ArrayList<String>();
                                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot sp : dataSnapshot.getChildren()) {
                                        if(sp.getValue().equals(true)){
                                            sublist.add(sp.getKey().toString());
                                        }
                                    }
                                    matchNum.add(sublist);
                                    if (position < matchNum.size()) {
                                        matchNumber.setText(matchNum.get(position).size() + "");
                                    }
                                }
                                @Override public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                        }


                    }
                }

        btndelete.setTag(position);
        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                int position=(Integer)view.getTag();
                imageView.getLayoutParams().height = 0;
                callback.deletePressed(position);
                imageView.setClickable(false);
//                imageView.setVisibility(View.GONE);

            }
        });

        textView.setText(descriptions2[position]);
//        matchNumber.setText(numberlist2[position]);
       // Log.d("matchNum", Arrays.toString(matchNum.toArray()));

        try{
            if (images2[position] != null)
            Picasso.with(mContext).load(images2[position]).into(imageView);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return grid;
    }

    public void setCallback(EditPlayerAdapterCallback callback){

        this.callback = callback;
    }


    public interface EditPlayerAdapterCallback {

        public void deletePressed(int position);
    }
}