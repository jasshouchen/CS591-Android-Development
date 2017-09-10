package com.example.shou.googleimagesearch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by shou on 4/15/2017.
 */

public class TravelListAdapter extends RecyclerView.Adapter<TravelListAdapter.ViewHolder>{
    Context mContext;
    private ArrayList<Place> place_versions;
    public TravelListAdapter(Context context, ArrayList<Place> place_versions) {
        this.mContext = context;
        this.place_versions = place_versions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_places, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.placeName.setText(place_versions.get(position).getPlaceName());
//        holder.placeName.setText(place.placeName);
        System.out.println("++++++++++++" + place_versions.get(position).getPlaceImage());
        Picasso.with(mContext).load(place_versions.get(position).getPlaceImage()).resize(120,60)
                .into(holder.placeImage);
//        Bitmap photo = getBitmapFromURL(place.getImageBitMap());
//        System.out.println("......................................................HAHAHA");
//        Palette.generateAsync(photo, new Palette.PaletteAsyncListener() {
//            public void onGenerated(Palette palette) {
//                int mutedLight = palette.getMutedColor(mContext.getResources().getColor(android.R.color.black));
//                holder.placeNameHolder.setBackgroundColor(mutedLight);
//            }
//        });

    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
    @Override
    public int getItemCount() {
        return place_versions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
//        public LinearLayout placeHolder;
//        public LinearLayout placeNameHolder;
        public TextView placeName;
        public ImageView placeImage;

        public ViewHolder(View itemView) {
            super(itemView);
//            placeHolder = (LinearLayout) itemView.findViewById(R.id.mainHolder);
            placeName = (TextView) itemView.findViewById(R.id.placeName);
//            placeNameHolder = (LinearLayout) itemView.findViewById(R.id.placeNameHolder);
            placeImage = (ImageView) itemView.findViewById(R.id.placeImage);
        }

//        @Override
//        public void onClick(View v) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(itemView, getPosition());
//            }
//        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
//
//    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
//        this.mItemClickListener = mItemClickListener;
//    }


}




