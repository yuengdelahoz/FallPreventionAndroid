package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

class WaterFloorOp2Detector implements Detector {
    private Classifier waterClassifier;
    private Classifier floorClassifier;
    private ImgUtils imgUtils = new ImgUtils();

    public WaterFloorOp2Detector(AssetManager assetManager) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.waterClassifier = ClassifierFactory.createWaterFloorOp2DetectionClassifier(assetManager);
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        float[] floorInputValues = imgUtils.convertMatToFloatArr(originalImage);
        float[] floorSuperpixels = floorClassifier.classifyImage(floorInputValues); //Perform the inference on the input image
        Mat floorImage = imgUtils.paintOriginalImage(floorSuperpixels, originalImage, true);
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<Mat>();
        mats.add(floorImage);
        mats.add(edgeImage);
        Mat waterInputImage = new Mat();
        Core.merge(mats, waterInputImage);
        float[] waterInputValues = imgUtils.convertMatToFloatArr(waterInputImage);
        float[] waterSuperpixels = waterClassifier.classifyImage(waterInputValues);
        Mat waterImage = imgUtils.paintOriginalImage(waterSuperpixels, originalImage, false);
        return waterImage;
    }
}
