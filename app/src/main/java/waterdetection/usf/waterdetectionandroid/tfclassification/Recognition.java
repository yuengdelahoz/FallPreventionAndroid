package waterdetection.usf.waterdetectionandroid.tfclassification;

import java.io.Serializable;

/**
 * Created by raulestrada on 8/27/17.
 */

public class Recognition implements Serializable {
    private final String id;
    private final String name;
    private final float confidence;
    private final int superpixelIndex;

    public Recognition(String id, String name, float confidence, int superpixelIndex) {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty() ||
                confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Id, name or confidence are missing or contain invalid values");
        }
        this.id = id;
        this.name = name;
        this.confidence = confidence;
        this.superpixelIndex = superpixelIndex;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getSuperpixelIndex() {
        return superpixelIndex;
    }

    @Override
    public String toString() {
        return "Recognition [" + this.superpixelIndex + "] : " + this.id + " : " + this.name + " : " +
                this.confidence;
    }
}
