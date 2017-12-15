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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


/**
 * This demo app saves the taken picture to a constant file.
 * $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
 */
public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener {

    private static final String TAG = "MainActivity";
    private View decorView;
    private int uiOption;
    RelativeLayout rootLayout = null;
    private  final int REQUEST_CAMERA_PERMISSION = 1;
    String backImgURI = null;
    private  final String FRAGMENT_DIALOG = "dialog";
    Toast toast = null;
    CountDownTimer toastTimer = null;
    private final  int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private   int[] FLASH_ICONS = null;

    private   int[] FLASH_TITLES = null;

    private int mCurrentFlash;

    private CameraView mCameraView;
    private ImageView mImageView;

    private Handler mBackgroundHandler;

//    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.take_picture:
//                    if (mCameraView != null) {
//
//                        mCameraView.takePicture();
//                    }
//                    break;
//            }
//        }
//    };



    public void setFullScreen() {
        
        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            //uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
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




    public int findResources(String type,String name ){
        String package_name = getApplication().getPackageName();
        return getApplication().getResources().getIdentifier(name, type, package_name);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(getResources("preview_overlay_new_portrait", "layout", getPackageName()));
        Intent intent = getIntent();
    
        backImgURI = intent.getStringExtra("imgURL");

        setContentView(findResources("layout","mirim_camera"));





        FLASH_ICONS = new int[]{
                findResources("drawable","@drawable/ic_flash_auto"),
                findResources("drawable","@drawable/ic_flash_off"),
                findResources("drawable","@drawable/ic_flash_on")
        };

        FLASH_TITLES = new int[]{
                findResources("string","flash_auto"),
                findResources("string","flash_off"),
                findResources("string","flash_on")
        };


        //setContentView(R.layout.mirim_camera);
        mCameraView = (CameraView) findViewById(findResources("id","camera"));
        //mCameraView = (CameraView) findViewById(R.id.camera);
        rootLayout = (RelativeLayout) findViewById(findResources("id","root"));
        //rootLayout = (RelativeLayout) findViewById(R.id.root);

        setFullScreen();
        mCameraView.setFacing(CameraView.FACING_FRONT);
        mCameraView.post(new Runnable() {
            @Override
            public void run() {
                toast =  Toast.makeText(getApplicationContext(), "화면을 터치하세요", Toast.LENGTH_SHORT);
                ViewGroup group = (ViewGroup) toast.getView();
                TextView messageTextView = (TextView) group.getChildAt(0);
                messageTextView.setTextSize(17);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                toastTimer = new CountDownTimer(5000, 1000)
                {

                    public void onTick(long millisUntilFinished) {toast.show();}
                    public void onFinish() {toast.show(); toastTimer.cancel(); toastTimer = null;}

                }.start();

                Iterator<AspectRatio> aspectIterator = mCameraView.getSupportedAspectRatios().iterator();
                while(aspectIterator.hasNext()){
                    AspectRatio tmpAspect = aspectIterator.next();
                    if(tmpAspect.toString().equals("5:3")){
                        mCameraView.setAspectRatio(tmpAspect);
                        break;
                    }else if(tmpAspect.toString().equals("16:9")){
                        mCameraView.setAspectRatio(tmpAspect);
                        break;
                    };

                }
                Log.d("aspect",mCameraView.getSupportedAspectRatios().size()+"");
            }
        });
        ImageView backImg = (ImageView) findViewById(findResources("id","imageView"));
        
        Picasso.with(getApplicationContext()).load(backImgURI).into(backImg);

        //mCameraView.setAspectRatio();
        rootLayout.setOnClickListener(new RelativeLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraView != null) {
                    if (mCameraView.isCameraOpened()) {
                        mCameraView.takePicture();
                    } else {
                        //mCameraView.start();
                    }

//                    if(mCameraView.isCameraOpened()){
//
//
//                            //mCameraView.addCallback();
//
//                       // mCameraView.stop();
//
//
////                        Toast.makeText(MainActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT)
/// .show();
//                    }else{
//                        mCameraView.start();
//                    }

                }
            }
        });
        //mImageView = (ImageView)findViewById(R.id.imageView);
        //mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));

        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
            //Log.d("aspect",mCameraView.getSupportedAspectRatios().size()+"");

        }
        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.take_picture);
        //if (fab != null) {
        //fab.setOnClickListener(mOnClickListener);
        //}
        Toolbar toolbar = (Toolbar) findViewById(findResources("id","toolbar"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {permission},123);

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    //f
                    .newInstance(findResources("string","camera_permission_confirmation"),
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            findResources("string","camera_permission_not_granted"))
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
          if(toastTimer != null){
            toastTimer.cancel();
        }
        if(toast != null){
            toast.cancel();

        }

        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, findResources("string","camera_permission_not_granted"),
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setFullScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(findResources("menu","main"), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == findResources("id","aspect_ratio")){
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (mCameraView != null
                    && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                final AspectRatio currentRatio = mCameraView.getAspectRatio();
                AspectRatioFragment.newInstance(ratios, currentRatio)
                        .show(fragmentManager, FRAGMENT_DIALOG);
            }
            return true;
        }else if(item.getItemId() == findResources("id","switch_flash")){
            if (mCameraView != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                item.setTitle(FLASH_TITLES[mCurrentFlash]);
                item.setIcon(FLASH_ICONS[mCurrentFlash]);
                mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
            }
            return true;
        }else if(item.getItemId() == findResources("id","switch_camera")){
            if (mCameraView != null) {
                int facing = mCameraView.getFacing();
                mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                        CameraView.FACING_BACK : CameraView.FACING_FRONT);
            }
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }


    }

    public void showTakeImg(byte[] imgData){


    }

    public void showTakeImg(File imgFile){

        Intent intent = new Intent(this, ResultImgView.class);
        intent.putExtra("imgFile", imgFile);

        intent.putExtra("backImgURI", backImgURI);
        intent.putExtra("facing", mCameraView.getFacing());

        startActivity(intent);
    }

    final Handler handler = new Handler(){
        public void handleMessage(Message msg){

            byte[] fileData = msg.getData().getByteArray("fileData");

            String sdPath = "MediaBOX";
            //showTakeImg(fileData);


// make file
            File file = new File(getFilesDir(),sdPath);

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
            try {
                os = new FileOutputStream(realPath);
                os.write(fileData);
                os.close();
                File imgFile = new File(realPath);
                showTakeImg(imgFile);
            } catch (IOException e) {
                Log.w(TAG, "Cannot write to " + realPath, e);
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
    };

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

//    public void openCapture(String fileURI){
//        Intent cameraIntent = new Intent(
//                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI);
//        this.startActivityForResult(cameraIntent, 101);
//    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }


//        private void screenshot(Bitmap bm) {
//            FileOutputStream out = null;
//
//            String sdPath = "MediaBOX";
//            try {
//
//                File path = new File(Environment.getExternalStorageDirectory(),sdPath);
//
//                if(! path.isDirectory()) {
//                    Log.d("mkdir","success");
//                    path.mkdirs();
//                }else{
//                    Log.d("mkdir","fail");
//                }
//
//                sdPath=path.getPath()+"/hello.jpg";
//                Log.d("sdPath ", sdPath);
//                out = new FileOutputStream(sdPath);
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    Intent mediaScanIntent = new Intent(
//                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    Uri contentUri = Uri.fromFile(path);
//                    mediaScanIntent.setData(contentUri);
//                    this.sendBroadcast(mediaScanIntent);
//                } else {
//                    sendBroadcast(new Intent(
//                            Intent.ACTION_MEDIA_MOUNTED,
//                            Uri.parse("file://"
//                                    + Environment.getExternalStorageDirectory())));
//                }
//            } catch (FileNotFoundException e) {
//                Log.d("FileNotFoundException:", e.getMessage());
//            } finally {
//                if(out  !=null){
//                    try{
//                        out.close();
//                    }catch (Exception e){
//                        Log.d("Exception :", e.getMessage());
//                    }
//                }else{};
//            }
//        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] fileData) {



            //Log.d(TAG, "onPictureTaken " + fileData.length);
   

//            final Bitmap bm = Bitmap.createBitmap(640, 360, Bitmap.Config.RGB_565);
//            bm.copyPixelsFromBuffer(ByteBuffer.wrap(data));
//            getBackgroundHandler().post(new Runnable() {
//                @Override
//                public void run() {
//                    FileOutputStream out = null;
//
//                    String sdPath = "MediaBOX";
//                    try {
//
//                        File path = new File(Environment.getExternalStorageDirectory(), sdPath);
//
//                        if (!path.isDirectory()) {
//                            Log.d("mkdir", "success");
//                            path.mkdirs();
//                        } else {
//                            Log.d("mkdir", "fail");
//                        }
//
//                        sdPath = path.getPath() + "/hello.jpg";
//                        Log.d("sdPath ", sdPath);
//                        out = new FileOutputStream(sdPath);
//                        bm.compress(Bitmap.CompressFormat.JPEG,100, out);
//                        //bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////                    Intent mediaScanIntent = new Intent(
////                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////                    Uri contentUri = Uri.fromFile(path);
////                    mediaScanIntent.setData(contentUri);
////                    this.sendBroadcast(mediaScanIntent);
////                } else {
////                    sendBroadcast(new Intent(
////                            Intent.ACTION_MEDIA_MOUNTED,
////                            Uri.parse("file://"
////                                    + Environment.getExternalStorageDirectory())));
////                }
//                    } catch (FileNotFoundException e) {
//                        Log.d("FileNotFoundException:", e.getMessage());
//                    } finally {
//                        if (out != null) {
//                            try {
//                                out.close();
//                            } catch (Exception e) {
//                                Log.d("Exception :", e.getMessage());
//                            }
//                        }
//                    }
//                }
//            });
//        };       ;

            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {

                    Message msg = handler.obtainMessage();
                    Bundle data = new Bundle();
                    data.putByteArray("fileData", fileData);
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            });
        }



        ;
    };

    ;

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))

                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

}
