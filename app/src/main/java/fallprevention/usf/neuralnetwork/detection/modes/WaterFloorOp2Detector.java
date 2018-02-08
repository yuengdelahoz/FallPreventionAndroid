package fallprevention.usf.neuralnetwork.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fallprevention.usf.neuralnetwork.tfclassification.Classifier;
import fallprevention.usf.neuralnetwork.tfclassification.ClassifierFactory;
import fallprevention.usf.neuralnetwork.tfclassification.FileUtils;

class WaterFloorOp2Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private File albumStorageDir;
    private boolean isExternalStorageWritable;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();

    WaterFloorOp2Detector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.waterClassifier = ClassifierFactory.createWaterFloorOp2DetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        // Perform the floor detection and create the color image from its output
        Long startTime = System.currentTimeMillis();
        float[] floorInputValues = imgUtils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        // Perform the water detection passing the 4 dimension input (color image from the floor detection model + edge detection)
        Mat inputImage = mergeLayersToCreateInput(floorSuperpixels, originalImage);
        float[] waterInputValues = imgUtils.convertMatToFloatArr(inputImage);
        Long startWater = System.currentTimeMillis();
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Long endWater = System.currentTimeMillis();
        Mat waterImage = imgUtils.paintOriginalImage(waterSuperpixels, originalImage, false); //Paint a red filter on those areas classified as 'water' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) { // Write the execution times in a file in Downloads/Exec Times/TimesWaterOp1.txt file in the phone
            fileUtils.mSaveData("TimesWaterOp2.txt", (endFloor-startFloor) + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return waterImage;
    }

    /**
     * Merges the laplacian image and the RGB floor detection output to create the input of the water detection model
     * @param floorSuperpixels - Raw output of the floor model
     * @param originalImage - Original RGB image
     * @return - The mat representing the input of the water detection model
     */
    private Mat mergeLayersToCreateInput(float[] floorSuperpixels, Mat originalImage) {
        Mat floorImage = imgUtils.paintOriginalImage(floorSuperpixels, originalImage, true);
        // Compute the Laplacian edge detection on the input image and create the 4 dimension input
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(floorImage);
        mats.add(edgeImage);
        Mat waterInputImage = new Mat();
        Core.merge(mats, waterInputImage);
        return waterInputImage;
    }
}
