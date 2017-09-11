package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

class WaterDetector implements Detector {
    private Classifier classifier;
    private ImgUtils imgUtils = new ImgUtils();

    public WaterDetector(AssetManager assetManager) {
        this.classifier = ClassifierFactory.createWaterDetectionClassifier(assetManager);
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage);
        List<Mat> mats = new ArrayList<>();
        mats.add(originalImage);
        mats.add(edgeImage);
        Mat input = new Mat();
        Core.merge(mats, input);

        float[] inputValues = imgUtils.convertMatToFloatArr(input);
        float[] superpixels = classifier.classifyImage(inputValues); //Perform the inference on the input image
        Mat finalImage = imgUtils.paintOriginalImage(superpixels, originalImage, false);
        return finalImage;
    }
}
