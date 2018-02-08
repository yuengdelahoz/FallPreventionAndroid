package usf.delahoz.fallprevention.detection.modes;

import org.opencv.core.Mat;

import java.io.Serializable;

public interface Detector extends Serializable {
    Mat performDetection(Mat originalImage);
}
