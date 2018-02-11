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

class WaterFloorOp1Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private long inferenceTime = -1l;
    private static final String INFERENCE_RUNTIME_FILENAME = "Inference_Time_Water_Floor_OP1_Detection.csv";

    WaterFloorOp1Detector(AssetManager assetManager) {
        this.waterClassifier = ClassifierFactory.createWaterFloorOp1DetectionClassifier(assetManager);
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        // Perform the floor detection and create the black and white image from its output
        float[] floorInputValues = Utils.convertMatToFloatArr(originalImage);
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        // Perform the water detection passing the 5 dimension input (original RGB image + edge detection + b/w floor image)
        Mat inputImage = mergeLayersToCreateInput(floorSuperpixels, originalImage);
        float[] waterInputValues = Utils.convertMatToFloatArr(inputImage);
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        this.inferenceTime = (System.currentTimeMillis() - startTime);
        return waterSuperpixels;
    }

    @Override
    public long getInferenceRuntime() {
        return this.inferenceTime;
    }

    @Override
    public String getInferenceRuntimeFilename() {
        return INFERENCE_RUNTIME_FILENAME;
    }

    /**
     * Merges the original RGB image, the laplacian image and the black and white floor detection output to create the input of the water detection model
     * @param floorSuperpixels - Raw output of the floor model
     * @param originalImage - Original RGB image
     * @return - The mat representing the input of the water detection model
     */
    private Mat mergeLayersToCreateInput(float[] floorSuperpixels, Mat originalImage) {
        Mat floorImage = Utils.paintBlackWhiteResults(floorSuperpixels, originalImage);
        // Compute the Laplacian edge detection on the input image and create the 5 dimension input
        Mat edgeImage = Utils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(originalImage);
        mats.add(edgeImage);
        mats.add(floorImage);
        Mat inputImage = new Mat();
        Core.merge(mats, inputImage);
        return inputImage;
    }
}
