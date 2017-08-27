package waterdetection.usf.waterdetectionandroid.tfclassification;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.List;

/**
 * Created by raulestrada on 8/27/17.
 */

public class ObjectDetectionClassifier implements Classifier {
    private static final String TF_INFERENCE_LIBRARY_NAME = "tensorflow_inference";

    // Path to frozen model
    private final String MODEL_FILE;
    // Names of nodes in the computational graph
    private final String INPUT_NODE_NAME;
    private final String OUTPUT_NODE_NAME;
    private final String[] OUTPUT_NODES;
    private final int OUTPUT_SIZE;
    private final String KEEP_PROB_NODE_NAME;
    // Shape of input tensor
    private final int[] INPUT_TENSOR_SHAPE;

    // TensorFlowInference object used to make inferences on the graph of the loaded model
    private TensorFlowInferenceInterface inferenceInterface;
    private boolean logStats = false;

    // Load the tensorflow_inference native library
    static {
        System.loadLibrary(TF_INFERENCE_LIBRARY_NAME);
        Log.i("WATER DETECTION", "Inference library loaded");
    }

    public ObjectDetectionClassifier(String modelFile, String inputNodeName, String outputNodeName, String[] outputNodes,
                                     int outputSize, String keepProbNodeName, int[] inputTensorShape, AssetManager assetManager) {
        if (modelFile == null || modelFile.trim().isEmpty() || inputNodeName == null || inputNodeName.trim().isEmpty()
                || outputNodeName == null || outputNodeName.trim().isEmpty() || outputNodes == null || outputNodes.length == 0
                || outputSize < 0 || keepProbNodeName == null || keepProbNodeName.trim().isEmpty() || inputTensorShape == null
                || inputTensorShape.length == 0 || assetManager == null) {
            throw new IllegalArgumentException("Object detection classifier constructor has received null or invalid values");
        }
        this.MODEL_FILE = modelFile;
        this.INPUT_NODE_NAME = inputNodeName;
        this.OUTPUT_NODE_NAME = outputNodeName;
        this.OUTPUT_NODES = outputNodes;
        this.OUTPUT_SIZE = outputSize;
        this.KEEP_PROB_NODE_NAME = keepProbNodeName;
        this.INPUT_TENSOR_SHAPE = inputTensorShape;
        this.loadModelFile(assetManager);
    }

    private void loadModelFile(AssetManager assetManager) {
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Log.i("TF-ANDR WATER DETECTION", "NN model file loaded");
    }

    @Override
    public List<Recognition> classifyImage(long[] inputValues) {
        float[] results = new float[OUTPUT_SIZE];
        this.inferenceInterface.feed(INPUT_NODE_NAME, INPUT_TENSOR_SHAPE, inputValues);
        this.inferenceInterface.run(OUTPUT_NODES, logStats);
        this.inferenceInterface.fetch(OUTPUT_NODE_NAME, results);
        return null;
    }

    @Override
    public void enableStatLogging(boolean debug) {
        this.logStats = debug;
    }

    @Override
    public String getStatString() {
        return this.inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        this.inferenceInterface.close();
    }
}
