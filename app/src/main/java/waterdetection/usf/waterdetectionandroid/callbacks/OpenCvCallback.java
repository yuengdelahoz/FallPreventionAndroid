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

    /*private void testImg(String fileName) {
        File f = new File(mAppContext.getCacheDir() + "/" + fileName + ".jpg");
        if (!f.exists()) {
            try {
                InputStream is = mAppContext.getAssets().open(fileName + ".jpg");
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
        Log.i("RAUL DEBUG", "AQUI");
        Mat orig = imread(f.getAbsolutePath(), IMREAD_COLOR);
        Mat img = new Mat();
        orig.assignTo(img, CvType.CV_8UC3);
        Mat finalImg = DetectorFactory.createWaterFloorOp2Detector(mAppContext.getAssets()).performDetection(img);
        imwrite(Environment.getExternalStorageDirectory() + "/Download/" + fileName + "-op2-android.jpg", finalImg);
        Mat finalImg2 = DetectorFactory.createWaterFloorOp1Detector(mAppContext.getAssets()).performDetection(img);
        imwrite(Environment.getExternalStorageDirectory() + "/Download/" + fileName + "-op1-android.jpg", finalImg2);
        Mat finalImg3 = DetectorFactory.createWaterDetector(mAppContext.getAssets()).performDetection(img);
        imwrite(Environment.getExternalStorageDirectory() + "/Download/" + fileName + "-android.jpg", finalImg3);
    }*/
}
