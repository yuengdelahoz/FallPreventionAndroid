package waterdetection.usf.waterdetectionandroid;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by raulestrada on 8/27/17.
 */

public class ModelDetector {
    private static final String TF_INFERENCE_LIBRARY_NAME = "tensorflow_inference";

    // Path to frozen model
    private static final String MODEL_FILE = "file:///android_asset/floor_model.pb";

    // NAmes of nodes in the computational graph
    private static final String INPUT_NODE_NAME = "input_images:0";
    private static final String OUTPUT_NODE_NAME = "superpixels:0";
    private static final String[] OUTPUT_NODES = {"superpixels:0"};
    private static final int OUTPUT_SIZE = 1250;
    private static final String KEEP_PROB_NODE_NAME = "keep_prob:0";
    // Shape of input tensor
    private static final int[] INPUT_TENSOR_SHAPE = {1,500,500,3};

    // TensorFlowInference object used to make inferences on the graph of the loaded model
    private TensorFlowInferenceInterface inferenceInterface;

    // Load the tensorflow_inference native library
    static {
        System.loadLibrary(TF_INFERENCE_LIBRARY_NAME);
        Log.i("WATER DETECTION", "Inference library loaded");
    }

    public ModelDetector(AssetManager assetManager) {
        // Initialize the TEnsorFlowInference object and load the graph model
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Log.i("WATER DETECTION", "NN model file loaded");
    }

    public void doInference(long[] inputValues) {
        float[] results = new float[OUTPUT_SIZE];
        this.inferenceInterface.feed(INPUT_NODE_NAME, INPUT_TENSOR_SHAPE, inputValues);
        this.inferenceInterface.run(OUTPUT_NODES);
        this.inferenceInterface.fetch(OUTPUT_NODE_NAME, results);
    }
}
