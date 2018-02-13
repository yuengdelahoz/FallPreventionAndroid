package usf.delahoz.fallprevention.callbacks;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.nn_models.Detector;
import usf.delahoz.fallprevention.nn_models.DetectorFactory;

/* The following acquires an image reader from the listener. This in turn lets us call the method to acquire the latest image */
public class ImageAvailableCallback implements ImageReader.OnImageAvailableListener {
    public enum ImageProcessingMode { WEBAPI, LOCAL};
    private Utils mat = new Utils();
    private Detector detector;
    private String TAG = getClass().getName();
    private static final String INFERENCE_RUNTIME_FOLDER = "InferenceTimes";
    private String[] nn_models;

    public ImageAvailableCallback(Bundle options, Context context) {
        ImageProcessingMode mode = options.getString("operation_mode").toLowerCase().contains("local")? ImageProcessingMode.LOCAL: ImageProcessingMode.WEBAPI;

        ArrayList<String> models = options.getStringArrayList("models");
        nn_models = new String[models.size()];
        for (int i=0;i<models.size();i++){
            nn_models[i] = models.get(i);
        }

        Log.d(TAG,"Constructor: Processing Image using models" + Arrays.toString(nn_models));
        switch (mode){
            case LOCAL:
                this.detector = DetectorFactory.createFloorDetector(context.getAssets());
                break;
            case WEBAPI:
                this.detector = DetectorFactory.createRemoteDetector(context,nn_models);
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
            try {
                long start_time = System.currentTimeMillis();
                Mat im = Utils.createInputMat(img);
                float[] inferenceResult = detector.runInference(im, start_time);
                if (Utils.isExternalStorageWritable() && inferenceResult!=null) { // Write the execution times in a file in Downloads/Exec Times folder in the phone
                    Utils.mSaveData(detector.getInferenceRuntimeFilename(),
                            start_time + "," + detector.getInferenceRuntime(),
                            Utils.getAlbumStorageDir(INFERENCE_RUNTIME_FOLDER));
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