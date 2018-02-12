package usf.delahoz.fallprevention.tfclassification;

import android.content.res.AssetManager;

/**
 * Use this class to load the model from the pb file and return a classifier
 */

public class ClassifierFactory {
    // Name of the frozen model files in the Android Assets folder
    private final static String FLOOR_MODEL_FILE = "file:///android_asset/floor_detection.pb";
    private final static String OBJECT_MODEL_SIX_NOFLOOR_WEIGHTED_EARLYSTOP_FILE = "file:///android_asset/object_detection_six_nofloor_weighted_earlystop.pb";
    private final static String OBJECT_MODEL_SIX_FLOOR_WEIGHTED_EARLYSTOP_FILE = "file:///android_asset/object_detection_six_floor_weighted_earlystop.pb";
    private final static String OBJECT_MODEL_NINEHUNDRED_NOFLOOR_WEIGHTED_FILE = "file:///android_asset/object_detection_ninehundred_nofloor_weighted.pb";
    private final static String OBJECT_MODEL_NINEHUNDRED_FLOOR_WEIGHTED_FILE = "file:///android_asset/object_detection_ninehundred_floor_weighted.pb";
    private final static String DISTANCE_ESTIMATOR_FILE = "file:///android_asset/distance_estimation.pb";
    private final static String WATER_MODEL_FILE = "file:///android_asset/v2_water_model_original.pb";
    private final static String WATER_FLOOR_OP1_MODEL_FILE = "file:///android_asset/v2_water_model_op1.pb";
    private final static String WATER_FLOOR_OP2_MODEL_FILE = "file:///android_asset/v2_water_model_op2.pb";
    // Names of the different tensor nodes in the computational graph
    private static final String INPUT_NODE_NAME = "input_images:0";
    private static final String OUTPUT_NODE_NAME = "superpixels:0";
    private static final String[] OUTPUT_NODES = {"superpixels:0"};
    // Number of superpixels the NN model will output
    private static final int OUTPUT_SIZE = 1250;
    private static final int FLOOR_OUTPUT_SIZE = 900;
    private static final int OBJECT_SIX_OUTPUT_SIZE = 6;
    private static final int OBJECT_NINEHUNDRED_SIZE = 900;
    private static final String KEEP_PROB_NODE_NAME = "keep_prob:0";
    private static final float[] KEEP_PROB_VALUE = {1.0f};
    // Shape of the tensor nodes in the computational graph
    private static final long[] KEEP_PROB_TENSOR_SHAPE = {1};
    private static final long[] FLOOR_INPUT_TENSOR_SHAPE = {1,240,240,3};
    private static final long[] OBJECT_INPUT_TENSOR_SHAPE = {1, 240, 240, 3};
    private static final long[] WATER_INPUT_TENSOR_SHAPE = {1,500,500,4};
    private static final long[] WATER_FLOOR_OP1_TENSOR_SHAPE = {1, 500, 500, 5};

    /**
     * Creates and returns a floor detection NN model loading such model from the frozen .pb file in the Assets folder
     * @param assetManager - The AssetManager is needed in order to load the frozen model file from the assets folder
     * @return - The flood detection model
     */
    public static Classifier createFloorDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                FLOOR_MODEL_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                FLOOR_OUTPUT_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                FLOOR_INPUT_TENSOR_SHAPE,
                assetManager);
    }

    public static Classifier createObjectSixNoFloorDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                OBJECT_MODEL_SIX_NOFLOOR_WEIGHTED_EARLYSTOP_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                OBJECT_SIX_OUTPUT_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                OBJECT_INPUT_TENSOR_SHAPE,
                assetManager
        );
    }

    public static Classifier createObjectSixFloorDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                OBJECT_MODEL_SIX_FLOOR_WEIGHTED_EARLYSTOP_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                OBJECT_SIX_OUTPUT_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                OBJECT_INPUT_TENSOR_SHAPE,
                assetManager
        );
    }

    public static Classifier createObjectNineHundredNoFloorDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                OBJECT_MODEL_NINEHUNDRED_NOFLOOR_WEIGHTED_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                OBJECT_NINEHUNDRED_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                OBJECT_INPUT_TENSOR_SHAPE,
                assetManager
        );
    }

    public static Classifier createObjectNineHundredFloorDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                OBJECT_MODEL_NINEHUNDRED_FLOOR_WEIGHTED_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                OBJECT_NINEHUNDRED_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                OBJECT_INPUT_TENSOR_SHAPE,
                assetManager
        );
    }

    public static Classifier createDistanceEstimatorClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(
                DISTANCE_ESTIMATOR_FILE,
                INPUT_NODE_NAME,
                OUTPUT_NODE_NAME,
                OUTPUT_NODES,
                OBJECT_SIX_OUTPUT_SIZE,
                KEEP_PROB_NODE_NAME,
                KEEP_PROB_VALUE,
                KEEP_PROB_TENSOR_SHAPE,
                OBJECT_INPUT_TENSOR_SHAPE,
                assetManager
        );
    }

    /**
     * Creates and returns a water detection NN model loading such model from the frozen .pb file in the Assets folder
     * @param assetManager - The AssetManager is needed in order to load the frozen model file from the assets folder
     * @return - The water detection model
     */
    public static Classifier createWaterDetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(WATER_MODEL_FILE, INPUT_NODE_NAME, OUTPUT_NODE_NAME,
                OUTPUT_NODES, OUTPUT_SIZE, KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_TENSOR_SHAPE,
                WATER_INPUT_TENSOR_SHAPE, assetManager);
    }

    public static Classifier createWaterFloorOp1DetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(WATER_FLOOR_OP1_MODEL_FILE, INPUT_NODE_NAME, OUTPUT_NODE_NAME,
                OUTPUT_NODES, OUTPUT_SIZE, KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_TENSOR_SHAPE,
                WATER_FLOOR_OP1_TENSOR_SHAPE, assetManager);
    }

    public static Classifier createWaterFloorOp2DetectionClassifier(AssetManager assetManager) {
        return new FallPreventionClassifier(WATER_FLOOR_OP2_MODEL_FILE, INPUT_NODE_NAME, OUTPUT_NODE_NAME,
                OUTPUT_NODES, OUTPUT_SIZE, KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_TENSOR_SHAPE,
                WATER_INPUT_TENSOR_SHAPE, assetManager);
    }
}