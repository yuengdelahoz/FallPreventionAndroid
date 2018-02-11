package usf.delahoz.fallprevention.nn_models;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.tfclassification.Classifier;
import usf.delahoz.fallprevention.tfclassification.ClassifierFactory;

class ObjectOnFloorDetector implements Detector {
    private Classifier objectClassifier;
    private Classifier floorClassifier;
    private long inferenceTime = -1l;
    private static final String INFERENCE_RUNTIME_FILENAME = "Inference_Time_Object_On_Floor_Detection.csv";
    public enum ObjectOnFloorMode {SIX_NO_FLOOR, SIX_FLOOR, NINE_HUNDRED_NO_FLOOR, NINE_HUNDRED_FLOOR};
    private boolean hasFloorDetection;

    ObjectOnFloorDetector(AssetManager assetManager, ObjectOnFloorDetector.ObjectOnFloorMode mode) {
        switch (mode) {
            case SIX_NO_FLOOR:
                this.objectClassifier = ClassifierFactory.createObjectSixNoFloorDetectionClassifier(assetManager);
                break;
            case SIX_FLOOR:
                this.objectClassifier = ClassifierFactory.createObjectSixFloorDetectionClassifier(assetManager);
                this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
                this.hasFloorDetection = true;
                break;
            case NINE_HUNDRED_NO_FLOOR:
                this.objectClassifier = ClassifierFactory.createObjectNineHundredNoFloorDetectionClassifier(assetManager);
                break;
            case NINE_HUNDRED_FLOOR:
                this.objectClassifier = ClassifierFactory.createObjectNineHundredFloorDetectionClassifier(assetManager);
                this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
                this.hasFloorDetection = true;
                break;
            default: throw new IllegalArgumentException("Object on floor detection mode " + mode + " not supported");
        }
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        Mat inputImage = originalImage;
        float[] inputValues = Utils.convertMatToFloatArr(inputImage);
        if (this.hasFloorDetection) {
            float[] floorSuperpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
            inputImage = Utils.paintFloorImage(floorSuperpixels, originalImage);
            inputValues = Utils.convertMatToFloatArr(inputImage);
        }
        float[] superpixels = objectClassifier.classifyImage(inputValues); //Perform the inference on the input image
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
