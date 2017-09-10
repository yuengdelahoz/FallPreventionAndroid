package waterdetection.usf.waterdetectionandroid;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 113;
    private static final int REQUEST_READ_STORAGE = 114;

    boolean canWrite = false;
    boolean useCamera = false;
    boolean canRead = false;

    private Messenger mService = null;
    private Intent deployAgent;
    boolean mBound = false;

    private Button btn;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int [] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWrite = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "write request");
                }
                /** ****************************************************************/
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useCamera = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "camera request");
                }
                /** ****************************************************************/
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "read request");
                }
                /** ****************************************************************/

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasReadPermission){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
        /** ****************************************************************/
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
/** ****************************************************************/
        boolean camPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        if (!camPermission){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        deployAgent = new Intent(this, HiddenAgent.class);
        final Intent processImage = new Intent(this,Camera2Service.class);
        btn = (Button) findViewById(R.id.startBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mBound) {
                    if (btn.getText().toString().equals("Launch")) {  //Start
                        startService(processImage);
                        btn.setText("Stop");
                        Message message = Message.obtain(null, HiddenAgent.Start);
                        message.replyTo = new Messenger(new ResponseHandler());
                        try {
                            mService.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else if (btn.getText().toString().equals("Stop")) { //Stop
                        stopService(processImage);
                        btn.setText("Launch");
                        Message message = Message.obtain(null, HiddenAgent.Stop);
                        message.replyTo = new Messenger(new ResponseHandler());
                        try {
                            mService.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else bindService(deployAgent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case HiddenAgent.briefing:
                    Bundle incomingData = msg.getData();
                    // UpdateWidgets(incomingData);
                    break;
                case HiddenAgent.dismissed:
                    unbindService(mConnection);
                    mService = null;
                    mBound = false;
                    break;
                default: super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            mService = new Messenger(service);
            mBound = true;
            Toast.makeText(getApplicationContext(),"All set!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){

            Toast.makeText(getApplicationContext(),"Service Crashed", Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void onStop(){
        mBound = false;
        mService = null;
        unbindService(mConnection);
        super.onStop();
    }

}
