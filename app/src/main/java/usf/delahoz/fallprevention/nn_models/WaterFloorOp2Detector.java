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
    private File albumStorageDir;
    private boolean isExternalStorageWritable;

    WaterFloorOp2Detector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.waterClassifier = ClassifierFactory.createWaterFloorOp2DetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public float[] runInference(Mat originalImage) {
        // Perform the floor detection and create the color image from its output
        Long startTime = System.currentTimeMillis();
        float[] floorInputValues = Utils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        // Perform the water detection passing the 4 dimension input (color image from the floor detection model + edge detection)
        Mat inputImage = mergeLayersToCreateInput(floorSuperpixels, originalImage);
        float[] waterInputValues = Utils.convertMatToFloatArr(inputImage);
        Long startWater = System.currentTimeMillis();
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Long endWater = System.currentTimeMillis();
        Mat waterImage = Utils.paintOriginalImage(waterSuperpixels, originalImage, false); //Paint a red filter on those areas classified as 'water' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) { // Write the execution times in a file in Downloads/Exec Times/TimesWaterOp1.txt file in the phone
            Utils.mSaveData("TimesWaterOp2.txt", (endFloor-startFloor) + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return waterSuperpixels;
    }

    @Override
    public long getInferenceRuntime() {
        return 0;
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
