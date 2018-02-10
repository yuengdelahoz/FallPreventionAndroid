package usf.delahoz.fallprevention.nn_models;

import org.opencv.core.Mat;

import java.io.Serializable;

public interface Detector extends Serializable {
    Mat runInference(Mat originalImage);
}
