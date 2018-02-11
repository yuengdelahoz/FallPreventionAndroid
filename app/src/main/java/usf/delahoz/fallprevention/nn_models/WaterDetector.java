package usf.delahoz.fallprevention.nn_models;

import android.content.res.AssetManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.tfclassification.Classifier;
import usf.delahoz.fallprevention.tfclassification.ClassifierFactory;

class WaterDetector implements Detector {
    private Classifier classifier;
    private long inferenceTime = -1l;
    private static final String INFERENCE_RUNTIME_FILENAME = "Inference_Time_Water_Detection.csv";

    WaterDetector(AssetManager assetManager) {
        this.classifier = ClassifierFactory.createWaterDetectionClassifier(assetManager);
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        Mat edgeImage = Utils.createLaplacianImage(originalImage); //Compute the Laplacian edge detection image and add it as the fourth dimension of the input of the water detection model
        List<Mat> mats = new ArrayList<>();
        mats.add(originalImage);
        mats.add(edgeImage);
        Mat input = new Mat();
        Core.merge(mats, input);

        float[] inputValues = Utils.convertMatToFloatArr(input);
        float[] superpixels = classifier.classifyImage(inputValues); //Perform the inference on the input image
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
