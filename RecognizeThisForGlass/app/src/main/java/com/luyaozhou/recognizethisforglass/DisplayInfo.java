package com.luyaozhou.recognizethisforglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


public class DisplayInfo extends Activity {

    private boolean keyEnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Map<String,String> result = (HashMap) intent.getSerializableExtra("result");
        Log.i("Display Info", ": "+ result.size());
        setContentView(R.layout.activity_display_info);
        TextView idNumber = (TextView)findViewById(R.id.idNumber);
        TextView fn = (TextView)findViewById(R.id.fn);
        TextView ln = (TextView)findViewById(R.id.ln);
        TextView dob = (TextView)findViewById(R.id.dob);
        TextView fullName = (TextView)findViewById(R.id.fullName);
        TextView sex = (TextView)findViewById(R.id.sex);
        TextView address = (TextView)findViewById(R.id.address);
        TextView aContent1 = (TextView)findViewById(R.id.aContent1);
        TextView aContent2 = (TextView)findViewById(R.id.aContent2);


        ln.setText("Last Name:    " + result.get("Address Recipient Last Name(0)") );
        fn.setText("First Name:    " + result.get("Address Recipient First Name(0)") );
        idNumber.setText("ID Number:    " + result.get("ID(0)") );
        dob.setText("Date of Birth:    " + result.get("DoB(0)") );
        String name = result.get("Address Recipient Formatted(0)");
        String[] name1 = name.split("|");
        //fullName.setText("Full Name:    " + name1[0] + " " + name1[1] + " " + name1[2] );
        fullName.setText("Full Name:    " + name );

        sex.setText("Sex:    " + result.get("Sex(0)") );
        //address.setText("Address:    " + result.get("Address(0)") );
        String addr = result.get("Address(0)");
        String[] aContent = addr.split("\n");
        aContent1.setText(aContent[1]);
        aContent2.setText(aContent[2]);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // keep screen on

    }

    /*

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: { // camera button (hardware)

                if(keyEnable){
                    finish();
                }
                // Return false to allow the camera button to do its default action
                return false;
            }
            case KeyEvent.KEYCODE_DPAD_CENTER: // touchpad tap
            case KeyEvent.KEYCODE_ENTER: {

                if(keyEnable){
                    finish();
                }
                return false;
            }
            default: {
                return super.onKeyDown(keyCode, event);
            }
        }
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
