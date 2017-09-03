package waterdetection.usf.waterdetectionandroid.tfclassification;

import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

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

    public ObjectDetectionClassifier(String modelFile, String inputNodeName, String outputNodeName, String[] outputNodes,
                                     int outputSize, String keepProbNodeName, float[] keepProbValues, long[] keepProbShape,
                                     long[] inputTensorShape, AssetManager assetManager) {
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
        this.loadModelFile(assetManager);
    }

    private void loadModelFile(AssetManager assetManager) {
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
        Log.i("TF-ANDR WATER DETECTION", "NN model file loaded");
    }

    @Override
    public float[] classifyImage(float[] inputValues, String dir) {
        float[] results = new float[OUTPUT_SIZE];
        this.inferenceInterface.feed(INPUT_NODE_NAME, inputValues, INPUT_TENSOR_SHAPE);
        this.inferenceInterface.feed(KEEP_PROB_NODE_NAME, KEEP_PROB_VALUE, KEEP_PROB_SHAPE);
        this.inferenceInterface.run(OUTPUT_NODES, logStats);
        this.inferenceInterface.fetch(OUTPUT_NODE_NAME, results);
        Mat a = new Mat(500,500,1);
        int cnt = 0;
        for (int sv = 0; sv < 500; sv += 10) {
            for (int sh = 0; sh < 500; sh += 20) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 20; j++) {
                        if (results[cnt] > 0.5) {
                            a.put(sv+i, sh+j, 255);
                        } else {
                            a.put(sv + i, sh + j, 0);
                        }
                    }
                }
                cnt++;
            }

        }
        boolean success = Imgcodecs.imwrite(dir + "/floor.jpg", a);
        return results;
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
