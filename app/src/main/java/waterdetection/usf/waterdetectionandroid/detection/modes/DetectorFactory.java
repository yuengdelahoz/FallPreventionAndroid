package waterdetection.usf.waterdetectionandroid.detection.modes;

import android.content.res.AssetManager;

public class DetectorFactory {

    public static Detector createFloorDetector(AssetManager assetManager) {
        return new FloorDetector(assetManager);
    }

    public static Detector createWaterDetector(AssetManager assetManager) {
        return new WaterDetector(assetManager);
    }

}
