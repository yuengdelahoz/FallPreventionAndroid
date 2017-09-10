package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

class WaterDetector implements Detector {
    private Classifier classifier;

    public WaterDetector(AssetManager assetManager) {
        this.classifier = ClassifierFactory.createWaterDetectionClassifier(assetManager);
    }

    @Override
    public Mat performDetection(float[] inputValues, Mat originalImage) {
        return null;
    }
}
