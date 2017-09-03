package waterdetection.usf.waterdetectionandroid.tfclassification;

import java.io.Serializable;
import java.util.List;

/**
 * Created by raulestrada on 8/27/17.
 */

public interface Classifier extends Serializable {
    List<Recognition> classifyImage(float[] inputValues, String dir);
    void enableStatLogging(boolean debug);
    String getStatString();
    void close();
}
