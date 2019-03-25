package com.example.cathouse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class CatActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView tvName;
    private static int REQUEST_CAMERA=1;
    private String mFilePath;
    private Bitmap bitmap;
    Bitmap bmp_Icon = null;
    Uri imgUri;
    String localTempImgDir = "MyImg"; // 保存照片的文件夹名称
    String pic_Name = "userID_"; // 相机拍照获得的照片名称
    File dirPicSys; // 本APP的系统相册


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.CatHouse:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.Map:
                Intent intent = new Intent();
                intent.setClass(CatActivity.this, Map.class);
                startActivity(intent);
                return true;

                case R.id.communication:
                    Intent intentCom = new Intent();
                    intentCom.setClass(CatActivity.this, CommunicationActivity.class);
                    startActivity(intentCom);
                    return true;

                case R.id.picture:
                    Intent intentpicture = new Intent();
                    intentpicture.setClass(CatActivity.this, DynamicActivity.class);
                    startActivity(intentpicture);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Only logged in users should access this activity
         */
        setContentView(R.layout.activity_cat);

        //tvName = findViewById(R.id.Username);

        mFilePath = Environment.getExternalStorageDirectory().getPath();// 获取SD卡路径  
        mFilePath = mFilePath + "/" + "temp.jpg";// 指定路径  

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        localTempImgDir = getResources().getString(R.string.app_name);
        createFileInAlbum(); // 创建本APP的系统相册
    }

    private void createFileInAlbum()
     {
           dirPicSys = new File(Environment.getExternalStorageDirectory() + "/" + localTempImgDir); // 获得相册文件夹路径
           if (!dirPicSys.exists())
             { // 在系统相册中创建一个名为应用名称的相册文件夹
                dirPicSys.mkdirs();
              }
      }

    public void Logout(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public void camera(View view){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机  
                //Uri photoUri = Uri.fromFile(new File(mFilePath)); // 传递路径  
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);// 更改系统默认存储路径  
                startActivityForResult(intent, REQUEST_CAMERA);
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mFilePath)));
        startActivityForResult(takeIntent, REQUEST_CAMERA);

    }


}