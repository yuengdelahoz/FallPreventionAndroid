package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Mat;

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
        Log.i("EDGE IMAGE", "Size: " + edgeImage.width() + ", " + edgeImage.height() + ", " + edgeImage.channels() + ". Type: " + edgeImage.type());
        return edgeImage;
    }
}
