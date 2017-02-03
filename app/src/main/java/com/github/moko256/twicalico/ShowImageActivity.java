package com.github.moko256.twicalico;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.Objects;

import twitter4j.MediaEntity;

/**
 * Created by moko256 on 2016/06/26.
 *
 * @author moko256
 */
public class ShowImageActivity extends AppCompatActivity {
    public static String FRAG_MEDIA_ENTITIES="MediaEntities";
    private static String FRAG_POSITION="position";

    MediaEntity[] mediaEntities;

    Toolbar toolbar;
    ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        mediaEntities=(MediaEntity[]) getIntent().getSerializableExtra(FRAG_MEDIA_ENTITIES);
        int position=getIntent().getIntExtra(FRAG_POSITION,0);

        toolbar=(Toolbar) findViewById(R.id.activity_show_image_toolbar);
        toolbar.inflateMenu(R.menu.activity_show_image_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId()==R.id.activity_show_image_download){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                    } else {
                        contentDownload();
                    }
                } else {
                    contentDownload();
                }
            }
            return true;
        });

        pager= (ViewPager) findViewById(R.id.activity_show_image_view_pager);
        pager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager(),mediaEntities,this));
        pager.setCurrentItem(position);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager=null;
        toolbar=null;
        mediaEntities=null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
            contentDownload();
        } else {
            Toast.makeText(this,"Permission Denided.",Toast.LENGTH_LONG).show();
        }
    }

    private void contentDownload(){
        String path="";
        String ext="";
        MediaEntity mediaEntity=mediaEntities[pager.getCurrentItem()];
        switch (mediaEntity.getType()){
            case "video":
                for(MediaEntity.Variant variant : mediaEntity.getVideoVariants()){
                    if(Objects.equals(variant.getContentType(), "video/mp4")){
                        path=variant.getUrl();
                        ext="mp4";
                    }
                }
                break;

            case "animated_gif":
                path=mediaEntity.getVideoVariants()[0].getUrl();
                ext="mp4";
                break;

            case "photo":
            default:
                String[] pathSplitWithDot=mediaEntity.getMediaURLHttps().split("\\.");
                path=mediaEntity.getMediaURLHttps()+":orig";
                ext=pathSplitWithDot[pathSplitWithDot.length-1];
                break;
        }
        DownloadManager manager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(path));
        String fileName=String.valueOf(mediaEntity.getId())+"."+ext;
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/twicalico/"+fileName
        );
        request.setTitle(fileName);
        manager.enqueue(request);
    }

    public static Intent getIntent(Context context, MediaEntity[] entities , int position){
        return new Intent(context,ShowImageActivity.class)
                .putExtra(FRAG_MEDIA_ENTITIES,entities)
                .putExtra(FRAG_POSITION,position);
    }
}
