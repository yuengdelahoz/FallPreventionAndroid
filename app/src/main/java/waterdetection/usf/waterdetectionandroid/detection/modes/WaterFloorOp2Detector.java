package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;
import waterdetection.usf.waterdetectionandroid.tfclassification.FileUtils;

class WaterFloorOp2Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private File albumStorageDir;
    private boolean isExternalStorageWritable;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();

    public WaterFloorOp2Detector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.waterClassifier = ClassifierFactory.createWaterFloorOp2DetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        Long startTime = System.currentTimeMillis();
        float[] floorInputValues = imgUtils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        Mat floorImage = imgUtils.paintOriginalImage(floorSuperpixels, originalImage, true);
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(floorImage);
        mats.add(edgeImage);
        Mat waterInputImage = new Mat();
        Core.merge(mats, waterInputImage);
        float[] waterInputValues = imgUtils.convertMatToFloatArr(waterInputImage);
        Long startWater = System.currentTimeMillis();
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Long endWater = System.currentTimeMillis();
        Mat waterImage = imgUtils.paintOriginalImage(waterSuperpixels, originalImage, false);
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) {
            fileUtils.mSaveData("TimesWaterOp2.txt", (endFloor-startFloor) + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return waterImage;
    }
}
