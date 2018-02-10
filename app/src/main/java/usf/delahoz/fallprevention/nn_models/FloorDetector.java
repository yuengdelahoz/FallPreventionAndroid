package usf.delahoz.fallprevention.nn_models;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import usf.delahoz.fallprevention.Utils;
import usf.delahoz.fallprevention.tfclassification.Classifier;
import usf.delahoz.fallprevention.tfclassification.ClassifierFactory;

class FloorDetector implements Detector {
    private Classifier floorClassifier;

    FloorDetector(AssetManager assetManager) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
    }

    @Override
    public float[] runInference(Mat originalImage) {
        Long startTime = System.currentTimeMillis();
        float[] inputValues = Utils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] superpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        Mat finalImage = Utils.paintOriginalImage(superpixels, originalImage, false); //Paint a red filter on those areas classified as 'floor' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (Utils.isExternalStorageWritable()) { // Write the execution times in a file in Downloads/Exec Times/TimesFloorOriginal.txt file in the phone
            Utils.mSaveData("TimesFloorOriginal.txt", (endFloor - startFloor) + ";" + 0 + ";" + (endTime-startTime),Utils.getAlbumStorageDir("Logs"));
        }
        return superpixels;
    }
}
