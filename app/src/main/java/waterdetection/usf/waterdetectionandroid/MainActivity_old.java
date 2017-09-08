package waterdetection.usf.waterdetectionandroid;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Handler;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity_old extends AppCompatActivity {
    private boolean canWrite = false, useCamera = false, canRead = false;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 113;
    private static final int REQUEST_READ_STORAGE = 114;

    private Messenger mService = null;
    private Intent deployAgent;
    boolean mBound = false;

    private Button startBtn;
    private Classifier classifier;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWrite = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "write request");
                }
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useCamera = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "camera request");
                }
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "read request");
                }
        }
    }

    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, opencv_callback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            opencv_callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void askForPermissions() {
        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasReadPermission){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
        boolean camPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        if (!camPermission){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.startBtn = (Button)findViewById(R.id.startBtn);
        this.classifier = ClassifierFactory.createFloorDetectionClassifier(getAssets());

        askForPermissions();
        loadOpenCV();

        deployAgent = new Intent(this, HiddenAgent.class);
        //bindService(deployAgent, mConnection, Context.BIND_AUTO_CREATE);
        final Intent processImage = new Intent(this, Camera2Service.class);
        //startService(processImage)
        this.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBound) {
                    if (startBtn.getText().toString().equals("Launch")) { //Start
                        startService(processImage);
                        startBtn.setText("Stop");
                        /*Message message = Message.obtain(null, HiddenAgent.Start);
                        message.replyTo = new Messenger(new ResponseHandler());
                        try {
                            mService.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }*/
                    } else if (startBtn.getText().toString().equals("Stop")) { //Stop
                        stopService(processImage);
                        startBtn.setText("Launch");
                        /*Message message = Message.obtain(null, HiddenAgent.Stop);
                        message.replyTo = new Messenger(new ResponseHandler());
                        try {
                            mService.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }*/
                    }
                }
                else {
                    bindService(deployAgent, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });
    }

    class ResponseHandler extends android.os.Handler {
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


    private BaseLoaderCallback opencv_callback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCVLoad", "OpenCV loaded successfully");
                    Log.e("WATER DETECTION","Current thread is " + Thread.currentThread().getName());
                    File f = new File(getApplicationContext().getCacheDir() + "/image-25787.jpg");
                    if (!f.exists()) {
                        try {
                            InputStream is = getApplicationContext().getAssets().open("image-25787.jpg");
                            int size = is.available();
                            byte[] buffer = new byte[size];
                            is.read(buffer);
                            is.close();
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(buffer);
                            fos.close();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    Mat orig = imread(f.getAbsolutePath(), IMREAD_COLOR);
                    Mat img = new Mat(500,500,3);
                    orig.assignTo(img, CvType.CV_32F);
                    int size = (int)img.total() * img.channels();
                    float[] imgValues = new float[size];
                    img.get(0, 0, imgValues);
                    classifier.classifyImage(imgValues);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
