package com.luyaozhou.recognizethisforglass;


        import java.io.BufferedOutputStream;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;
        import java.net.URI;

        import android.app.Activity;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.PixelFormat;
        import android.hardware.Camera;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.FileObserver;
        import android.os.Handler;
        import android.provider.MediaStore;
        import android.view.KeyEvent;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;

        import com.google.android.glass.content.Intents;

//Special thanks to Nortom Lam for camera example source

public class CameraActivity extends Activity {
    private static final int PHOTO_REQUEST_CODE=1;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean previewOn;
    Handler mHandler = new Handler();
    private final static int CAMERA_FPS = 5000;
    Uri myUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_preview);

        // Set up the camera preview user interface
        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolderCallback());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: { // camera button (hardware)

                camera.stopPreview(); // stop the preview
                camera.release(); // release the camera
                previewOn = false;

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // capture image
                startActivityForResult(intent, PHOTO_REQUEST_CODE);

                // Return false to allow the camera button to do its default action
                return false;
            }
            case KeyEvent.KEYCODE_DPAD_CENTER: // touchpad tap
            case KeyEvent.KEYCODE_ENTER: {



                camera.stopPreview();
                camera.release();

                previewOn = false; // Don't release the camera in surfaceDestroyed()
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // capture image

                        startActivityForResult(intent, PHOTO_REQUEST_CODE);
                    }
                });

                return false;
            }
            default: {
                return super.onKeyDown(keyCode, event);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == PHOTO_REQUEST_CODE ) {


            String photoFileName = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.

        } else {

            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).
            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };

            observer.startWatching();
            Intent infoIntent = new Intent(this, DisplayInfo.class);
            startActivity(infoIntent);
        }
    }

        // camera preview stuff
        class SurfaceHolderCallback implements SurfaceHolder.Callback {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (null != camera) {

                    try {
                        Camera.Parameters params = camera.getParameters(); // must change the camera parameters to fix a bug in XE1
                        params.setPreviewFpsRange(CAMERA_FPS, CAMERA_FPS);
                        camera.setParameters(params);

                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                        previewOn = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                camera = Camera.open();
                try {
                    camera.setPreviewDisplay(holder);
                    Camera.Parameters p = camera.getParameters();
                    p.set("jpeg-quality", 70);
                    p.setPictureFormat(PixelFormat.JPEG);
                    p.setPictureSize(640, 480);
                    camera.setParameters(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (previewOn) {
                    camera.stopPreview(); //stop the preview
                    camera.release();  //release the camera for using it later (or if another app want to use)
                }
            }
        }

    }
