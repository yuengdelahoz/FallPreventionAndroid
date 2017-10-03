package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;
import waterdetection.usf.waterdetectionandroid.tfclassification.FileUtils;

class WaterDetector implements Detector {
    private Classifier classifier;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();
    private boolean isExternalStorageWritable;
    private File albumStorageDir;

    public WaterDetector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.classifier = ClassifierFactory.createWaterDetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        Long startTime = System.currentTimeMillis();
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<>();
        mats.add(originalImage);
        mats.add(edgeImage);
        Mat input = new Mat();
        Core.merge(mats, input);

        float[] inputValues = imgUtils.convertMatToFloatArr(input);
        Long startWater = System.currentTimeMillis();
        float[] superpixels = classifier.classifyImage(inputValues); //Perform the inference on the input image
        Long endWater = System.currentTimeMillis();
        Mat finalImage = imgUtils.paintOriginalImage(superpixels, originalImage, false);
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) {
            fileUtils.mSaveData("TimesWaterOriginal.txt", 0 + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return finalImage;
    }
}
