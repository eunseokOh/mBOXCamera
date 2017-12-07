/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mirimmedialab.co.kr.mboxcamera.cls;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

//import mirimmedialab.co.kr.mboxcamera.R;
//import com.google.android.cameraview.m_view.R;

//import com.google.android.cameraview.m_view.R;



public class ResultImgView extends Activity {
    private View decorView;
    private int uiOption;
    File imgFile = null;
    Bitmap cBitmap = null;
    FrameLayout fLayout = null;

    public int findResources(String type,String name ){
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, type, package_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(findResources("layout","activity_result_img_view"));
        fLayout = (FrameLayout) findViewById( findResources("id","fLayout"));
        setFullScreen();
        Intent intent = getIntent();
        final int facing = Integer.parseInt(intent.getExtras().get("facing").toString());
        imgFile = (File) intent.getExtras().get("imgFile");

        ImageView backImg = (ImageView) findViewById(findResources("id","backImg") );
        Picasso.with(getApplication()).load(intent.getExtras().getString("backImgURI")).into(backImg);
        fLayout.post(new Runnable() {
            @Override
            public void run() {

                viewTakeImage(imgFile, facing);
            }
        });


//        byte[] imgData = (byte[])intent.getExtras().get("imgData");
//
//        viewTakeImage(imgData, facing);
    }





    private void viewTakeImage(File imgFile, int facing){
        cBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        Matrix matrix = new Matrix();
        if(facing == 1){
            matrix.postRotate(-90);
        }else{
            matrix.postRotate(90);

        }
        Display mDisplay = this.getWindowManager().getDefaultDisplay();
        final int width  = mDisplay.getWidth();
        final int height = mDisplay.getHeight();
        Bitmap rotatedBitmap = Bitmap.createBitmap(cBitmap , 0, 0, cBitmap .getWidth(), cBitmap .getHeight(), matrix, true);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,width,height,true);


        imgFile.delete();

        ImageView myImage = (ImageView) findViewById(findResources("id","captureImage"));


        //cBitmap = combineImages(myBitmap,myBitmap);

        //myImage.getLayoutParams().height = realHeight;



        myImage.setImageBitmap(scaledBitmap);
        saveImageFile();







        //myImage.getLayoutParams().width = 100;

    }

    public void saveImageFile(){
        Bitmap captureView = Bitmap.createBitmap(fLayout.getMeasuredWidth(),
                fLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas screenShotCanvas = new Canvas(captureView);
        fLayout.draw(screenShotCanvas);
        fLayout.setDrawingCacheEnabled(false);
        String asPath = "MediaBOX";


        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),asPath);

        if(! file.isDirectory()) {
            Log.d("mkdir","success");
            file.mkdirs();
        }else{
            Log.d("mkdir","fail");
        }
        OutputStream os = null;
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().replaceAll("-","");
        String realPath = file.getPath()+"/"+uid+".jpg";
        File sendFile = null;
        try {

            os = new FileOutputStream(realPath);
            captureView.compress(Bitmap.CompressFormat.JPEG, 100, os);
            sendFile = new File(realPath);

            if(sendFile.exists()){
                this.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(sendFile)) );
            }




        } catch (IOException e) {

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Ignore
                }
            }



        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

    public void setFullScreen() {
        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().setFormat(PixelFormat.UNKNOWN);
        decorView.setSystemUiVisibility(uiOption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cBitmap.recycle();

    }
}
