package waterdetection.usf.waterdetectionandroid.tfclassification;

import java.io.Serializable;
import java.util.List;

/**
 * Created by raulestrada on 8/27/17.
 * This interface contains all the methods that a normal TensorFlow classifier supports
 */
public interface Classifier extends Serializable {
    /**
     * Calls the inference on the image whose values are passed as input values, and returns the
     * superpixels produced by the NN model.
     * @param inputValues - The pixel values of the input image
     * @return - The superpixels produced by the inference of the NN model
     */
    float[] classifyImage(float[] inputValues);

    /**
     * Enables/Disables the logging of statisitics. By default it's set to false
     * @param debug - True if stat logging should be enabled. False otherwise
     */
    void enableStatLogging(boolean debug);

    /**
     * Gets and returns the string with the stats returned by the NN model
     * @return - The string with the stats returned by the model
     */
    String getStatString();

    /**
     * Closes the session, feeds and fetches used by the model inference.
     */
    void close();
}
