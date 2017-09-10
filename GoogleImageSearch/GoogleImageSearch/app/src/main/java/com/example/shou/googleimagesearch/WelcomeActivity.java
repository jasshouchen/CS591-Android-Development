package com.example.shou.googleimagesearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by shou on 4/13/2017.
 */

public class WelcomeActivity extends Activity {
    private Button btnSearchImage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomepage);
        btnSearchImage = (Button)findViewById(R.id.btnsearchImage);
        btnSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

}
