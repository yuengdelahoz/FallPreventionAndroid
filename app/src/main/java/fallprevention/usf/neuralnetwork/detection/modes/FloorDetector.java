package fallprevention.usf.neuralnetwork.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Mat;

import java.io.File;

import fallprevention.usf.neuralnetwork.tfclassification.Classifier;
import fallprevention.usf.neuralnetwork.tfclassification.ClassifierFactory;
import fallprevention.usf.neuralnetwork.tfclassification.FileUtils;

class FloorDetector implements Detector {
    private Classifier floorClassifier;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();
    private File albumStorageDir;
    private boolean isExternalStorageWritable;

    FloorDetector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.floorClassifier = ClassifierFactory.createFloorDetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        Long startTime = System.currentTimeMillis();
        float[] inputValues = imgUtils.convertMatToFloatArr(originalImage);
        Long startFloor = System.currentTimeMillis();
        float[] superpixels = floorClassifier.classifyImage(inputValues); //Perform the inference on the input image
        Long endFloor = System.currentTimeMillis();
        Mat finalImage = imgUtils.paintOriginalImage(superpixels, originalImage, false); //Paint a red filter on those areas classified as 'floor' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) { // Write the execution times in a file in Downloads/Exec Times/TimesFloorOriginal.txt file in the phone
            fileUtils.mSaveData("TimesFloorOriginal.txt", (endFloor - startFloor) + ";" + 0 + ";" + (endTime-startTime), albumStorageDir);
        }
        return finalImage;
    }
}
