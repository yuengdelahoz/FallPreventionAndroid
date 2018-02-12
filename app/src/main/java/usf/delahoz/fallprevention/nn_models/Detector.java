package usf.delahoz.fallprevention.nn_models;

import org.opencv.core.Mat;

import java.io.Serializable;

public interface Detector extends Serializable {
    float[] runInference(Mat originalImage, long startTime);

    long getInferenceRuntime();
    String getInferenceRuntimeFilename();
}
