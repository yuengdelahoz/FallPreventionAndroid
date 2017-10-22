package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;
import waterdetection.usf.waterdetectionandroid.tfclassification.FileUtils;

class WaterFloorOp1Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();
    private boolean isExternalStorageWritable;
    private File albumStorageDir;

    WaterFloorOp1Detector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.waterClassifier = ClassifierFactory.createWaterFloorOp1DetectionClassifier(assetManager);
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        // Perform the floor detection and create the black and white image from its output
        Long startTime = System.currentTimeMillis();
        float[] floorInputValues = imgUtils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        // Perform the water detection passing the 5 dimension input (original RGB image + edge detection + b/w floor image)
        Mat inputImage = mergeLayersToCreateInput(floorSuperpixels, originalImage);
        float[] waterInputValues = imgUtils.convertMatToFloatArr(inputImage);
        Long startWater = System.currentTimeMillis();
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Long endWater = System.currentTimeMillis();
        Mat waterResImage = imgUtils.paintOriginalImage(waterSuperpixels, originalImage, false); //Paint a red filter on those areas classified as 'water' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) { // Write the execution times in a file in Downloads/Exec Times/TimesWaterOp1.txt file in the phone
            fileUtils.mSaveData("TimesWaterOp1.txt", (endFloor-startFloor) + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return waterResImage;
    }

    /**
     * Merges the original RGB image, the laplacian image and the black and white floor detection output to create the input of the water detection model
     * @param floorSuperpixels - Raw output of the floor model
     * @param originalImage - Original RGB image
     * @return - The mat representing the input of the water detection model
     */
    private Mat mergeLayersToCreateInput(float[] floorSuperpixels, Mat originalImage) {
        Mat floorImage = imgUtils.paintBlackWhiteResults(floorSuperpixels, originalImage);
        // Compute the Laplacian edge detection on the input image and create the 5 dimension input
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(originalImage);
        mats.add(edgeImage);
        mats.add(floorImage);
        Mat inputImage = new Mat();
        Core.merge(mats, inputImage);
        return inputImage;
    }
}
