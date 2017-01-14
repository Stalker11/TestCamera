package com.olegel.professor.testcamera;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavePicture {
    private static final String TAG = SavePicture.class.getSimpleName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static Context cont;
    private VideoCallBack callBack;

    public SavePicture(VideoCallBack callBack) {
        this.callBack = callBack;
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }
    public static void setContext(Context context){
        cont = context;
    }
    /** Create a File for saving an image or video */
    public File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            Log.d(TAG, "getOutputMediaFile: "+mediaFile.getAbsolutePath());
            String[] path = new String[]{mediaFile.getAbsolutePath()};
            MediaScannerConnection.scanFile(cont, path, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.d(TAG, "getOutputMediaFile: "+s+" "+uri);
                }
            });
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
            Log.d(TAG, "getOutputMediaFile: "+mediaFile.getAbsolutePath()+" "+100);
            String[] path = new String[]{mediaFile.getAbsolutePath()};
            Log.d(TAG, "getOutputMediaFile: "+mediaFile.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(mediaFile));
            cont.sendBroadcast(intent);
            callBack.path(mediaFile.getAbsolutePath());
            /*MediaScannerConnection.scanFile(cont, path, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    Log.d(TAG, "getOutputMediaFile: connected");
                }

                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.d(TAG, "getOutputMediaFile: "+s+" "+uri);
                }
            });*/
        } else {
            return null;
        }

        return mediaFile;
    }
}
