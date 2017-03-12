package com.github.moko256.twicalico;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by moko256 on 2016/11/14.
 *
 * @author moko256
 */

public class LicensesActivity extends AppCompatActivity {

    ActionBar actionBar;
    TextView licenseTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);

        licenseTextView = (TextView) findViewById(R.id.license_text);

        if (getIntent()!=null){
            String libName=getIntent().getStringExtra("library_name");
            if (libName!=null){
                try {
                    InputStream textStream;

                    String path;

                    switch (libName){
                        case "support_v4":
                        case "support_v7":
                        case "support_v14":
                        case "support_design":
                            path = "license_android_support.txt";
                            break;

                        default:
                            path = "license_"+libName+".txt";
                            break;
                    }

                    textStream=getAssets().open(path);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(textStream));
                    StringBuilder builder = new StringBuilder();

                    String lineText;

                    while ((lineText = reader.readLine()) != null){
                        builder.append(lineText).append("\n");
                    }

                    licenseTextView.setText(builder.toString());

                    reader.close();
                    textStream.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        actionBar=null;
        licenseTextView=null;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return false;
    }

}