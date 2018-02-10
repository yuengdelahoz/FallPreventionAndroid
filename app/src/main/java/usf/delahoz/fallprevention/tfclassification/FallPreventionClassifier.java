package usf.delahoz.fallprevention.tfclassification;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 *
 * This class represents a classifier that takes an input image and produces an array of superpixels.
 * The computational graph of the model has input, output and keep_prob nodes whose data needs to be
 * provided in order to perform the inference.
 */

class FallPreventionClassifier implements Classifier {
    private static final String TF_INFERENCE_LIBRARY_NAME = "tensorflow_inference";
    // Path to frozen model
    private final String MODEL_FILE;
    // Names of nodes in the computational graph
    private final String INPUT_NODE_NAME;
    private final String OUTPUT_NODE_NAME;
    private final String[] OUTPUT_NODES;
    private final int OUTPUT_SIZE;
    private final String KEEP_PROB_NODE_NAME;
    private final float[] KEEP_PROB_VALUE;
    private final long[] KEEP_PROB_SHAPE;
    // Shape of input tensor
    private final long[] INPUT_TENSOR_SHAPE;
    // TensorFlowInference object used to make inferences on the graph of the loaded model
    private TensorFlowInferenceInterface inferenceInterface;
    private boolean logStats = false;

    // Load the tensorflow_inference native library
    static {
        System.loadLibrary(TF_INFERENCE_LIBRARY_NAME);
        Log.i("WATER DETECTION", "Inference library loaded");
    }

    public FallPreventionClassifier(String modelFile,
                                    String inputNodeName,
                                    String outputNodeName,
                                    String[] outputNodes,
                                    int outputSize,
                                    String keepProbNodeName,
                                    float[] keepProbValues,
                                    long[] keepProbShape,
                                    long[] inputTensorShape,
                                    AssetManager assetManager)
    {
        if (modelFile == null || modelFile.trim().isEmpty() || inputNodeName == null || inputNodeName.trim().isEmpty()
                || outputNodeName == null || outputNodeName.trim().isEmpty() || outputNodes == null || outputNodes.length == 0
                || outputSize < 0 || keepProbNodeName == null || keepProbNodeName.trim().isEmpty() || inputTensorShape == null
                || inputTensorShape.length == 0 || assetManager == null || keepProbValues == null || keepProbValues.length == 0) {
            throw new IllegalArgumentException("Object detection classifier constructor has received null or invalid values");
        }
        this.MODEL_FILE = modelFile;
        this.INPUT_NODE_NAME = inputNodeName;
        this.OUTPUT_NODE_NAME = outputNodeName;
        this.OUTPUT_NODES = outputNodes;
        this.OUTPUT_SIZE = outputSize;
        this.KEEP_PROB_NODE_NAME = keepProbNodeName;
        this.INPUT_TENSOR_SHAPE = inputTensorShape;
        this.KEEP_PROB_SHAPE = keepProbShape;
        this.KEEP_PROB_VALUE = keepProbValues;
        this.loadModelFile(assetManager); //Loads the frozen model from the .pb file in the assets folder
    }

    private void loadModelFile(AssetManager assetManager) {
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Log.i("TF-ANDR WATER DETECTION", "NN model file loaded");
    }

    /**
     * Calls the inference on the image whose values are passed as input values, and returns the
     * superpixels produced by the NN model.
     * @param inputValues - The pixel values of the input image
     * @return - The superpixels produced by the inference of the NN model
     */
    @Override
    public float[] classifyImage(float[] inputValues) {
        float[] results = new float[OUTPUT_SIZE]; //This array will contain the results produced by the neural network
        // We need to feed the input and keep_prob data just like we would in Python
        this.inferenceInterface.feed(INPUT_NODE_NAME, inputValues, INPUT_TENSOR_SHAPE);
        this.inferenceInterface.feed(KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_SHAPE);
        this.inferenceInterface.run(OUTPUT_NODES, logStats);
        this.inferenceInterface.fetch(OUTPUT_NODE_NAME, results); //Fetch the output values and store it in the results array
        return results;
    }

    /**
     * Enables/Disables the logging of statisitics. By default it's set to false
     * @param debug - True if stat logging should be enabled. False otherwise
     */
    @Override
    public void enableStatLogging(boolean debug) {
        this.logStats = debug;
    }

    /**
     * Gets and returns the string with the stats returned by the NN model
     * @return - The string with the stats returned by the model
     */
    @Override
    public String getStatString() {
        return this.inferenceInterface.getStatString();
    }

    /**
     * Closes the session, feeds and fetches used by the model inference.
     */
    @Override
    public void close() {
        this.inferenceInterface.close();
    }
}
