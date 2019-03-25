package com.example.cathouse;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap;
import android.net.Uri;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap.CompressFormat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.net.URL;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import android.support.v7.app.AlertDialog;

public class DynamicActivity extends AppCompatActivity {

    private ImageView ivDynamicPicture,select_photo;
    private static int REQUEST_CAMERA = 1;
    private String mFilePath;
    private Bitmap bitmap;
    private Button take_photo;
    public static final int TAKE_PHOTO = 1;
    public static final int SELECT_PHOTO = 2;
    private ImageView imageview;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);

        //ivDynamicPicture = (ImageView) findViewById(R.id.ivDynamicPicture);
        //take_photo = (Button) findViewById(R.id.take_photo);
        select_photo = (ImageView) findViewById(R.id.select_photo);
        //imageview = (ImageView) findViewById(R.id.imageview);
    }






    //传递bitmap
    private void intentBitmap() {
        //把Bitmap转码成字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 50, baos);
        String imageBase64 = new String(Base64.encode(baos.toByteArray(), 0));
        //把字符串存到SharedPreferences里面
        SharedPreferences prePicture = getSharedPreferences("Picture", Context.MODE_PRIVATE);
        Editor editor = prePicture.edit();
        editor.putString("cameraImage", imageBase64);
        editor.commit();
    }

    private void startPhotoZoom(Uri uri, int size)
    {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);
        //startActivityForResult(intent, IMG_FROMCROP);
        }

    public void select_photo(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            openAlbum();
            Intent intentpicture = new Intent();
            intentpicture.setClass(DynamicActivity.this, DynamicActivity.class);
            startActivityForResult(intentpicture,1);
        }


    }

    public void published(View view){
        new AlertDialog.Builder(DynamicActivity.this)
                .setIcon(android.R.drawable.ic_dialog_map)
                .setTitle("SUCCESS")
                .setMessage("Successflly upload the photo.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intentpicture = new Intent();
                        intentpicture.setClass(DynamicActivity.this, Map.class);
                        startActivity(intentpicture);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intentpicture = new Intent();
                        intentpicture.setClass(DynamicActivity.this, DynamicActivity.class);
                        startActivity(intentpicture);
                    }
                }).create().show();

    }
    /**
     * 打开相册的方法view
     * */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,SELECT_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO :
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageview.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SELECT_PHOTO :
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImgeOnKitKat(data);
                    }else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }
    /**
     *4.4以下系统处理图片的方法
     * */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }
    /**
     * 4.4及以上系统处理图片的方法
     * */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImgeOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }else if ("content".equalsIgnoreCase(uri.getScheme())) {
                //如果是content类型的uri，则使用普通方式处理
                imagePath = getImagePath(uri,null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //如果是file类型的uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            //根据图片路径显示图片
            displayImage(imagePath);
        }
    }
    /**
     * 根据图片路径显示图片的方法
     * */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageview.setImageBitmap(bitmap);
        }else {
            Toast.makeText(DynamicActivity.this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 通过uri和selection来获取真实的图片路径
     * */
    private String getImagePath(Uri uri,String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1 :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(DynamicActivity.this,"failed to get image",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}



