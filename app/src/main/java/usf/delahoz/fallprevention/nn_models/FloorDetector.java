package usf.delahoz.fallprevention.nn_models;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.tfclassification.Classifier;
import usf.delahoz.fallprevention.tfclassification.ClassifierFactory;

class FloorDetector implements Detector {
    private Classifier floorClassifier;
    private long inferenceTime = -1l;
    private static final String INFERENCE_RUNTIME_FILENAME = "Inference_Time_Floor_Detection.csv";

    FloorDetector(AssetManager assetManager) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        float[] inputValues = Utils.convertMatToFloatArr(originalImage);
        float[] superpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
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
