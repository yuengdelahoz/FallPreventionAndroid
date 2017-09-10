package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;
import waterdetection.usf.waterdetectionandroid.tfclassification.ClassifierFactory;

class FloorDetector implements Detector {
    private Classifier floorClassifier;
    private ImgUtils imgUtils = new ImgUtils();

    public FloorDetector(AssetManager assetManager) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
    }

    @Override
    public Mat performDetection(float[] inputValues, Mat originalImage) {
        float[] superpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
        Mat finalImage = imgUtils.paintOriginalImage(superpixels, originalImage);
        return finalImage;
    }
}
