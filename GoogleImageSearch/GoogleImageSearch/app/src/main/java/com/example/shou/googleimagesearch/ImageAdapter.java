package com.example.shou.googleimagesearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.URL;
import java.util.List;
/**
 * Created by shou on 3/25/2017.
 */

public class ImageAdapter extends BaseAdapter {
    /*
    Common base class of common implementation for an Adapter that can be used in both ListView (by implementing the specialized ListAdapter interface) and Spinner (by implementing the specialized SpinnerAdapter interface).
     */
    private Context mContext;
    public static String url_des = "";
    public static String url_bitmap;
    List<ImageResult> images;
    public ImageAdapter(Context c) {
        mContext = c;
    }
    public void setImages (List<ImageResult> images) {
        this.images = images;
        notifyDataSetChanged();
    }


    public int getCount() {
        if (images != null) {
            return images.size();
        } else {
            return 0;
        }
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public String getUrl_des() {
      return url_des;
    }

    public View getView(final int position, View converterView, ViewGroup vg) {
        ImageView imgView;
        TextView txtView;
        View layout;
        if (converterView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.gridview, vg, false);
            imgView = (ImageView) layout.findViewById(R.id.imageView);
            txtView = (TextView) layout.findViewById(R.id.textView);

        } else {
            layout = converterView;
            imgView = (ImageView)converterView.findViewById(R.id.imageView);
            txtView = (TextView)converterView.findViewById(R.id.textView);
        }
        imgView.buildDrawingCache();
        final Bitmap image= imgView.getDrawingCache();
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(mContext,MainActivity.class);
                Bundle extras = new Bundle();
                extras.putString("imageParcel", images.get(0).getUrl());
                url_bitmap = images.get(0).getUrl();
                intent.putExtras(extras);
                System.out.println(images.get(0).getUrl()+" ...............................");
                mContext.startActivity(intent);
            }
        });
        ImageLoader.getInstance().displayImage(images.get(0).getUrl(), imgView);
        txtView.setText(images.get(0).getName());

        return layout;

    }


}
