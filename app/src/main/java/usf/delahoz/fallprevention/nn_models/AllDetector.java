package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;

import org.opencv.core.Mat;

import java.util.Arrays;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.tfclassification.Classifier;
import usf.delahoz.fallprevention.tfclassification.ClassifierFactory;

/**
 * Created by yuengdelahoz on 2/21/18.
 */

public class AllDetector implements Detector{
    private Classifier objectClassifier;
    private Classifier floorClassifier;
    private Classifier distanceclassifier;
    private long inferenceTime = -1l;
    private static String INFERENCE_RUNTIME_FILENAME = "";

    AllDetector(AssetManager assetManager, Context context) {
        this.distanceclassifier = ClassifierFactory.createDistanceEstimatorClassifier(assetManager);
        this.objectClassifier = ClassifierFactory.createObjectSixFloorDetectionClassifier(assetManager);
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String SCREEN_MODE = pm.isInteractive() ? "SCREEN_ON": "SCREEN_OFF";

        INFERENCE_RUNTIME_FILENAME  = "LOCAL_"+SCREEN_MODE+"_inference_times_trial_"+System.currentTimeMillis()+".csv";
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        float[] inputValues = Utils.convertMatToFloatArr(originalImage);

        float[] floorSuperpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
        Mat floorImage = Utils.paintFloorImage(floorSuperpixels, originalImage);
        float[] floorValues = Utils.convertMatToFloatArr(floorImage);

        float[] object_superpixels = objectClassifier.classifyImage(floorValues); //Perform the inference on the input image
        float[] superpixels = distanceclassifier.classifyImage(inputValues); //Perform the inference on the input image

        this.inferenceTime = (System.currentTimeMillis() - startTime);
        return superpixels;
    }

    @Override
    public long getInferenceRuntime() {
        return this.inferenceTime;
    }

    @Override
    public String getInferenceRuntimeFilename() {
        return INFERENCE_RUNTIME_FILENAME;
    }
}
