package fallprevention.usf.neuralnetwork.callbacks;

import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

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
