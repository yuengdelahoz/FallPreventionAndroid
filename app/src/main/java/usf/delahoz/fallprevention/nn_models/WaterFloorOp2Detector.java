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

class WaterFloorOp2Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private long inferenceTime = -1l;
    private static final String INFERENCE_RUNTIME_FILENAME = "Inference_Time_Water_Floor_OP2_Detection.csv";

    WaterFloorOp2Detector(AssetManager assetManager) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.waterClassifier = ClassifierFactory.createWaterFloorOp2DetectionClassifier(assetManager);
    }

    @Override
    public float[] runInference(Mat originalImage, long startTime) {
        // Perform the floor detection and create the color image from its output
        float[] floorInputValues = Utils.convertMatToFloatArr(originalImage);
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        // Perform the water detection passing the 4 dimension input (color image from the floor detection model + edge detection)
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
     * Merges the laplacian image and the RGB floor detection output to create the input of the water detection model
     * @param floorSuperpixels - Raw output of the floor model
     * @param originalImage - Original RGB image
     * @return - The mat representing the input of the water detection model
     */
    private Mat mergeLayersToCreateInput(float[] floorSuperpixels, Mat originalImage) {
        Mat floorImage = Utils.paintOriginalImage(floorSuperpixels, originalImage, true);
        // Compute the Laplacian edge detection on the input image and create the 4 dimension input
        Mat edgeImage = Utils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(floorImage);
        mats.add(edgeImage);
        Mat waterInputImage = new Mat();
        Core.merge(mats, waterInputImage);
        return waterInputImage;
    }
}
