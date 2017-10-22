package waterdetection.usf.waterdetectionandroid.callbacks;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import waterdetection.usf.waterdetectionandroid.detection.modes.DetectorFactory;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class OpenCvCallback extends BaseLoaderCallback {
    private static final String TAG = "OpenCV Callback";

    public OpenCvCallback(Context appContext) {
        super(appContext);
    }

    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS: {
                Log.i("OpenCVLoad", "OpenCV loaded successfully");
                Log.e(TAG,"Current thread is " + Thread.currentThread().getName());
            }
            break;
            default: {
                super.onManagerConnected(status);
            }
            break;
        }
    }
}
