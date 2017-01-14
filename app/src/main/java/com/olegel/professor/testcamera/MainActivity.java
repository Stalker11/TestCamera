package com.olegel.professor.testcamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
private static final String TAG = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording = false;
    private boolean photoVideo = false;
    private FrameLayout preview;
    private Button photoButton;
    private Button videoButton;
    private MediaRecorder mMediaRecorder;
    private String videoLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoButton = (Button) findViewById(R.id.button_capture);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        videoButton = (Button) findViewById(R.id.button_video);
        photoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                        photoVideo = true;
                    }
                }
        );
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    // stop recording and release camera
                    mMediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder
                    Log.d(TAG, "onClick: "+isRecording);
                    // inform the user that recording has stopped
                   videoButton.setText("Начать запись");
                    String[] path = new String[]{videoLink};
                    Log.d(TAG, "onClick: "+videoLink);
                     MediaScannerConnection.scanFile(getBaseContext(), path, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    Log.d(TAG, "getOutputMediaFile: connected");
                }

                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.d(TAG, "getOutputMediaFile: "+s+" "+uri);
                }
            });
                    isRecording = false;
                } else {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();
                        Log.d(TAG, "onClick: "+isRecording);
                        // inform the user that recording has started
                        videoButton.setText("Стоп");
                        isRecording = true;
                        photoVideo = false;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                }
            }

        });
    }

    @Override
    protected void onStart() {
        checkCameraHardware(this);
        Log.d(TAG, "onCreate: "+mCamera);
        getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        Log.d(TAG, "onStart: "+mPreview);
        preview.addView(mPreview);
        super.onStart();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.d(TAG, "checkCameraHardware: "+true);
            return true;
        } else {
            Log.d(TAG, "checkCameraHardware: "+false);
            return false;
        }
    }
    public void getCameraInstance(){
       if (mCamera != null){
           mCamera.release();
           mCamera = null;
       }
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters params = mCamera.getParameters();
// set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
// set Camera parameters
            mCamera.setParameters(params);
            Log.d(TAG, "getCameraInstance: "+mCamera);
        }
        catch (Exception e){
            Log.d(TAG, "getCameraInstance: "+e.getMessage());
        }

    }

    @Override
    protected void onDestroy() {
        mCamera.release();
        super.onDestroy();
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if(photoVideo){

            }
            File pictureFile = new SavePicture(null).getOutputMediaFile(SavePicture.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");

                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d(TAG, "onPictureTaken: "+pictureFile.getAbsolutePath());
              /*  String[] path = new String[]{pictureFile.getAbsolutePath()};
                MediaScannerConnection.scanFile(getBaseContext(), path, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Log.d(TAG, "onPictureTaken: "+s+"  "+uri);
                    }
                });*/
                mCamera.startPreview();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
    }
    private boolean prepareVideoRecorder(){

       // mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(new SavePicture(new VideoCallBack() {
            @Override
            public void path(String link) {
               videoLink = link;
            }
        }).getOutputMediaFile(SavePicture.MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

}
