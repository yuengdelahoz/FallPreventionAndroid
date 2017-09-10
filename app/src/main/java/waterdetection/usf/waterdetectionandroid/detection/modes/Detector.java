package waterdetection.usf.waterdetectionandroid.detection.modes;

import org.opencv.core.Mat;

import java.io.Serializable;

public interface Detector extends Serializable {
    Mat performDetection(Mat originalImage);
}
