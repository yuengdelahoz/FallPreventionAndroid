package usf.delahoz.fallprevention.callbacks;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.nn_models.Detector;
import usf.delahoz.fallprevention.nn_models.DetectorFactory;

/* The following acquires an image reader from the listener. This in turn lets us call the method to acquire the latest image */
public class ImageAvailableCallback implements ImageReader.OnImageAvailableListener {
    public enum ImageProcessingMode { WEBAPI, LOCAL};
    private Utils mat = new Utils();
    private Detector detector;
    private String TAG = getClass().getName();
    private Object lock = new Object();

    public ImageAvailableCallback(ImageProcessingMode mode , Context context) {
        Log.d(TAG,"Constructor: Processing Image using mode " + mode);
        switch (mode){
            case LOCAL:
                this.detector = DetectorFactory.createFloorDetector(context.getAssets());
                break;
            case WEBAPI:
                this.detector = DetectorFactory.createRemoteDetector(context);
                break;
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        /* the following variables are used to convert the data we get to bytes,
        * and re-construct them to finally create and save an image file*/
        Image img = null;
        //pretty self explanatory. like, c'mon now. read the line. lazy...
        img = reader.acquireLatestImage();
            /*the full code below would also have "if-else" or "else" statements
            * to check for other types of retrieved images/files */
        if (img == null) return;
        if (img.getFormat() == ImageFormat.JPEG) {
            //check if we have external storage to write to. if we do, save acquired image
            if (Utils.isExternalStorageWritable())
            {
                try {
                    Mat im = Utils.createInputMat(img);
                    Mat result = detector.runInference(im);
                    if (result != null){
                        Utils.SaveImage(im,System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    img.close();
                }
            }
        }
    }


}