package waterdetection.usf.waterdetectionandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity {
    private Classifier classifier;

    private boolean canWrite = false, useCamera = false, canRead = false;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 113;
    private static final int REQUEST_READ_STORAGE = 114;

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
        classifier = ClassifierFactory.createFloorDetectionClassifier(getAssets());

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, opencv_callback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            opencv_callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

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

    private BaseLoaderCallback opencv_callback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCVLoad", "OpenCV loaded successfully");
                    Log.e("WATER DETECTION","Current thread is " + Thread.currentThread().getName());
                    File f = new File(getApplicationContext().getCacheDir() + "/image-1024.jpg");
                    if (!f.exists()) {
                        try {
                            InputStream is = getApplicationContext().getAssets().open("image-1024.jpg");
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
                    classifier.classifyImage(imgValues, Environment.getExternalStorageDirectory() + "/Download/");
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
