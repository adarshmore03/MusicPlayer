package com.example.adplayer;

import android.Manifest;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;


import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.internal.ContextUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity  extends AppCompatActivity {
    ListView listView;
     String[] items;
     ImageView favourites;
     ImageView playlists;





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);

        favourites=findViewById(R.id.favourites);
        favourites.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),favouriteslist.class));
            }
        });


        playlists=findViewById(R.id.playlists);
        playlists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),playlistssong.class));
            }
        });
        runtimePermission();
    }

     public void runtimePermission(){
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
               .withListener(new MultiplePermissionsListener() {
                   @Override
                   public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                       displaySongs();
                   }

                   @Override
                   public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                       permissionToken.continuePermissionRequest();

                   }
               }).check();
    }


    public ArrayList<File> findSong(File f){
        ArrayList<File> arrayList=new ArrayList<>();
        File[] files= f.listFiles();
        if(files!=null) {

            for (File singlefile : files) {
                if (!singlefile.isHidden() && singlefile.isDirectory()) {
                    arrayList.addAll(findSong(singlefile));
                } else {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().startsWith(" ")) {
                        arrayList.add(singlefile);
                    }
                }

            }
        }

        return arrayList;
    }
    public byte[] getAlbumArt(Uri uri){
        MediaMetadataRetriever retriever =new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();

        return art;
    }


    void displaySongs(){
        final ArrayList<File>mySongs=findSong(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for (int i=0;i< mySongs.size();i++){

            items[i] = mySongs.get(i).getName().toString().replace(".mp3","");}

         // ArrayAdapter<String> myAdapter=new ArrayAdapter<String>
        //        (this,android.R.layout.simple_list_item_1,items);
       // listView .setAdapter(myAdapter);

        customAdapter customAdapter=new customAdapter();
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               String songName=(String) listView.getItemAtPosition(i);
               startActivity(new Intent(getApplicationContext(),PlaySong.class)
               .putExtra("songs",mySongs)
               .putExtra("songname",songName)
               .putExtra("pos",i));
            }
        });



    }

    class customAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView= getLayoutInflater().inflate(R.layout.list_item,null);
            TextView textsong =myView.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(items[i]);


           return myView;

        }
    }

}