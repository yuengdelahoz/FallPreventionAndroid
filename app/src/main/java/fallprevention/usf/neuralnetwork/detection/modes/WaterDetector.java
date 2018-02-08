package fallprevention.usf.neuralnetwork.detection.modes;

import android.content.res.AssetManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fallprevention.usf.neuralnetwork.tfclassification.Classifier;
import fallprevention.usf.neuralnetwork.tfclassification.ClassifierFactory;
import fallprevention.usf.neuralnetwork.tfclassification.FileUtils;

class WaterDetector implements Detector {
    private Classifier classifier;
    private ImgUtils imgUtils = new ImgUtils();
    private FileUtils fileUtils = new FileUtils();
    private boolean isExternalStorageWritable;
    private File albumStorageDir;

    WaterDetector(AssetManager assetManager, File albubStorageDir, boolean isExternalStorageWritable) {
        this.classifier = ClassifierFactory.createWaterDetectionClassifier(assetManager);
        this.albumStorageDir = albubStorageDir;
        this.isExternalStorageWritable = isExternalStorageWritable;
    }

    @Override
    public Mat performDetection(Mat originalImage) {
        Long startTime = System.currentTimeMillis();
        Mat edgeImage = imgUtils.createLaplacianImage(originalImage); //Compute the Laplacian edge detection image and add it as the fourth dimension of the input of the water detection model
        List<Mat> mats = new ArrayList<>();
        mats.add(originalImage);
        mats.add(edgeImage);
        Mat input = new Mat();
        Core.merge(mats, input);

        float[] inputValues = imgUtils.convertMatToFloatArr(input);
        Long startWater = System.currentTimeMillis();
        float[] superpixels = classifier.classifyImage(inputValues); //Perform the inference on the input image
        Long endWater = System.currentTimeMillis();
        Mat finalImage = imgUtils.paintOriginalImage(superpixels, originalImage, false); // Paint a red filter on those areas classified as 'water' by the model in the RGB input image
        Long endTime = System.currentTimeMillis();
        if (isExternalStorageWritable) { // Write the execution times in a file in Downloads/Exec Times/TimesWaterOriginal.txt file in the phone
            fileUtils.mSaveData("TimesWaterOriginal.txt", 0 + ";" + (endWater-startWater) + ";" + (endTime-startTime), albumStorageDir);
        }
        return finalImage;
    }
}
