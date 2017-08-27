package waterdetection.usf.waterdetectionandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

public class MainActivity extends AppCompatActivity {
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        classifier = ClassifierFactory.createFloorDetectionClassifier(getAssets());

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, opencv_callback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            opencv_callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    private BaseLoaderCallback opencv_callback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCVLoad", "OpenCV loaded successfully");
                    Log.e("WATER DETECTION","Current thread is " + Thread.currentThread().getName());
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
