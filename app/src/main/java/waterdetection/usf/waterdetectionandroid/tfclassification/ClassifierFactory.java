package waterdetection.usf.waterdetectionandroid.tfclassification;

import android.content.res.AssetManager;

/**
 * Created by raulestrada on 8/27/17.
 */

public class ClassifierFactory {
    private final static String FLOOR_MODEL_FILE = "file:///android_asset/floor_model.pb";
    private final static String WATER_MODEL_FILE = "file:///android_asset/water_model_original.pb";
    private static final String INPUT_NODE_NAME = "input_images:0";
    private static final String OUTPUT_NODE_NAME = "superpixels:0";
    private static final String[] OUTPUT_NODES = {"superpixels:0"};
    private static final int OUTPUT_SIZE = 1250;
    private static final String KEEP_PROB_NODE_NAME = "keep_prob:0";
    private static final float[] KEEP_PROB_VALUE = {1.0f};
    private static final long[] KEEP_PROB_TENSOR_SHAPE = {1};
    private static final long[] FLOOR_INPUT_TENSOR_SHAPE = {1,500,500,3};
    private static final long[] WATER_INPUT_TENSOR_SHAPE = {1,500,500,4};

    public static Classifier createFloorDetectionClassifier(AssetManager assetManager) {
        return new ObjectDetectionClassifier(FLOOR_MODEL_FILE, INPUT_NODE_NAME, OUTPUT_NODE_NAME,
                OUTPUT_NODES, OUTPUT_SIZE, KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_TENSOR_SHAPE,
                FLOOR_INPUT_TENSOR_SHAPE, assetManager);
    }

    public static Classifier createWaterDetectionClassifier(AssetManager assetManager) {
        return new ObjectDetectionClassifier(WATER_MODEL_FILE, INPUT_NODE_NAME, OUTPUT_NODE_NAME,
                OUTPUT_NODES, OUTPUT_SIZE, KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_TENSOR_SHAPE,
                WATER_INPUT_TENSOR_SHAPE, assetManager);
    }
}