package com.luyaozhou.recognizethisforglass;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.glass.content.Intents;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Special thanks to Nortom Lam for camera example source

public class ViewFinder extends Activity {
    private static final int PHOTO_REQUEST_CODE=1;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean previewOn;
    Handler mHandler = new Handler();
    private final static int CAMERA_FPS = 5000;
    String mCurrentPhotoPath;
    ImageView mImageView;
    private boolean keyEnable = true;
    Map<String, String> map = new HashMap<String, String>();
    Map<String, String> iQAMsg = new HashMap<String, String>();

    //private CardScrollView mCardScroller;
    //private Slider mSlider;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_preview);

        // Set up the camera preview user interface
        getWindow().setFormat(PixelFormat.UNKNOWN);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolderCallback());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // keep screen on
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: { // camera button (hardware)
                if(keyEnable) {

                    camera.stopPreview(); // stop the preview
                    camera.release(); // release the camera
                    previewOn = false;

                    keyEnable = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // capture image
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                            }
                        }
                    });

                }
                    // Return false to allow the camera button to do its default action
                return false;

            }
            case KeyEvent.KEYCODE_DPAD_CENTER: // touchpad tap
            case KeyEvent.KEYCODE_ENTER: {

                if(keyEnable) {

                    camera.stopPreview();
                    camera.release();

                    previewOn = false; // Don't release the camera in surfaceDestroyed()

                    keyEnable = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // capture image
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                            }
                        }
                    });

                }
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
        //Map<String, String > result = new HashMap<String,String>();

        if (pictureFile.exists()) {
            // The picture is ready; process it.
            Bitmap imageBitmap = null;
            try {
                //               Bundle extras = data.getExtras();
                //               imageBitmap = (Bitmap) extras.get("data");
                FileInputStream fis = new FileInputStream(picturePath); //get the bitmap from file
                imageBitmap = (Bitmap) BitmapFactory.decodeStream(fis);

                if(imageBitmap != null) {
                    Bitmap lScaledBitmap = Bitmap.createScaledBitmap(imageBitmap,1600,1200,false);
                    if(lScaledBitmap != null) {
                        imageBitmap.recycle();
                        imageBitmap = null;

                        ByteArrayOutputStream lImageBytes = new ByteArrayOutputStream();
                        lScaledBitmap.compress(Bitmap.CompressFormat.JPEG,30,lImageBytes);
                        lScaledBitmap.recycle();
                        lScaledBitmap = null;

                        byte[] lImageByteArray = lImageBytes.toByteArray();

                        HashMap<String,String> lValuePairs = new HashMap<String,String>();
                        lValuePairs.put("base64Image", Base64.encodeToString(lImageByteArray,Base64.DEFAULT));
                        lValuePairs.put("compressionLevel","30");
                        lValuePairs.put("documentIdentifier", "DRIVER_LICENSE_CA");
                        lValuePairs.put("documentHints", "");
                        lValuePairs.put("dataReturnLevel", "15");
                        lValuePairs.put("returnImageType", "1");
                        lValuePairs.put("rotateImage", "0");
                        lValuePairs.put("data1", "");
                        lValuePairs.put("data2", "");
                        lValuePairs.put("data3", "");
                        lValuePairs.put("data4", "");
                        lValuePairs.put("data5", "");
                        lValuePairs.put("userName", "zbroyan@miteksystems.com");
                        lValuePairs.put("password", "google1");
                        lValuePairs.put("phoneKey", "1");
                        lValuePairs.put("orgName", "MobileImagingOrg");

                        String lSoapMsg= formatSOAPMessage("InsertPhoneTransaction",lValuePairs);

                        DefaultHttpClient mHttpClient = new DefaultHttpClient();
//                        mHttpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.R)
                        HttpPost mHttpPost = new HttpPost();

                        mHttpPost.setHeader("User-Agent","UCSD Team");
                        mHttpPost.setHeader("Content-typURIe","text/xml;charset=UTF-8");
                        //mHttpPost.setURI(URI.create("https://mi1.miteksystems.com/mobileimaging/ImagingPhoneService.asmx?op=InsertPhoneTransaction"));
                        mHttpPost.setURI(URI.create("https://mi1.miteksystems.com/mobileimaging/ImagingPhoneService.asmx"));

                        StringEntity se = new StringEntity(lSoapMsg, HTTP.UTF_8);
                        se.setContentType("text/xml; charset=UTF-8");
                        mHttpPost.setEntity(se);
                        HttpResponse mResponse = mHttpClient.execute(mHttpPost,new BasicHttpContext());

                        String responseString = new BasicResponseHandler().handleResponse(mResponse);
                        parseXML(responseString);

                        //Todo: this is test code. Need to be implemented
                        //result = parseXML(testStr);
                        Log.i("test", "test:"+" "+responseString);
                        Log.i("test", "test: "+" "+map.size());

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // this part will be relocated in order to let the the server process picture
            if (map.size() == 0){
                Intent display = new Intent(getApplicationContext(), DisplayInfoFailed.class);
                display.putExtra("result", (java.io.Serializable) iQAMsg);
                startActivity(display);
            }
            else{
                Intent display = new Intent(getApplicationContext(), DisplayInfo.class);
                display.putExtra("result", (java.io.Serializable) map);
                startActivity(display);

            }
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
                public void onEvent(int event, final String path) {
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
                                    new LongOperation().execute(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();

        }

    }


    private String formatSOAPMessage(String method, HashMap<String, String> params)
    {
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.setLength(0);
        mBuilder.append("<?xml version='1.0' encoding='utf-8'?>");
        mBuilder.append("<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>");
        mBuilder.append("<soap:Body><");
        mBuilder.append(method);
        mBuilder.append(" xmlns='http://www.miteksystems.com/'>");
        Iterator<String> it = params.keySet().iterator();
        String key;
        while(it.hasNext())
        {
            key = it.next();

            mBuilder.append("<");
            mBuilder.append(key);
            mBuilder.append(">");

            mBuilder.append(TextUtils.htmlEncode(params.get(key)));

            mBuilder.append("</");
            mBuilder.append(key);
            mBuilder.append(">");

        }

        mBuilder.append("</");
        mBuilder.append(method);
        mBuilder.append("></soap:Body></soap:Envelope>");
        return mBuilder.toString();
    }

    public void parseXML(String xml) {
        //Map<String, String> map = new HashMap<String, String>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));

            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("ExtractedField");

            // iterate the employees
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                NodeList name = element.getElementsByTagName("Name");
                Element nameE = (Element) name.item(0);

                NodeList bestValue = element.getElementsByTagName("ValueBest");
                Element bestValueE = (Element) bestValue.item(0);

                map.put(getCharacterDataFromElement(nameE), getCharacterDataFromElement(bestValueE) );
            }
            //if(map.size() == 0){
            NodeList imageQuality = doc.getElementsByTagName("IQAMessage");
            Element iqaMsg = (Element) imageQuality.item(0);
            iQAMsg.put("IQAMessage",getCharacterDataFromElement(iqaMsg));
            //}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
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
            keyEnable=true;

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (previewOn) {
                camera.stopPreview(); //stop the preview
                camera.release();  //release the camera for using it later (or if another app want to use)
                keyEnable = false;
            }
        }
    }

    class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            processPictureWhenReady(params[0]);
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
        }
        @Override
        protected void onPreExecute() {
            //mCardScroller = new CardScrollView(this);
            //mCardScroller.setAdapter(new CardAdapter(createCards()));
        }
        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}
