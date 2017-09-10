package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

class WaterFloorOp1Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private ImgUtils imgUtils = new ImgUtils();

    public WaterFloorOp1Detector(AssetManager assetManager) {
        this.waterClassifier = ClassifierFactory.createWaterFloorOp1DetectionClassifier(assetManager);
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        float[] floorInputValues = imgUtils.convertMatToFloatArr(originalImage);
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Mat floorImage = imgUtils.paintBlackWhiteResults(floorSuperpixels, originalImage);
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(originalImage);
        mats.add(edgeImage);
        mats.add(floorImage);
        Mat inputImage = new Mat();
        Core.merge(mats, inputImage);
        float[] waterInputValues = imgUtils.convertMatToFloatArr(inputImage);
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Mat waterResImage = imgUtils.paintOriginalImage(waterSuperpixels, originalImage);
        return waterResImage;
    }
}
