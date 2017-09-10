package waterdetection.usf.waterdetectionandroid.detection.modes;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC3;

public class ImgUtils {
    private final static int LAPLACIAN_K_SIZE = 3;
    private final static int LAPLACIAN_DELTA = 0;
    private final static int LAPLACIAN_SCALE = 1;
    /**
     * Method that paints the results of the floor detection model on top of the original input image
     * When a superpixel is classified as "floor", then all the pixels in the image that belong to that
     * superpixel are colored black, so that in the end the returned image is the original image with
     * all the areas identified as floor are colored black.
     * @param superpixels - The 1250 vector with the output of the floor detection model
     * @param originalImage - The original resized image (500x500)
     * @return - A copy of the original image where all pixels classified as floor are colored black
     */
    public Mat paintOriginalImage(float[] superpixels, Mat originalImage, boolean obscure) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat or = new Mat(500, 500, CV_32FC3);
        originalImage.convertTo(or, CV_32FC3);
        int superpixel = 0;
        for (int sv = 0; sv < height; sv += 10) { // 50 superpixels in the height direction
            for (int sh = 0; sh < width; sh += 20) { // 25 superpixels in the width direction
                Rect roi = new Rect(sh, sv, 20, 10);
                if (superpixels[superpixel] > 0.5 && !obscure) {
                    Mat oSubOrig = or.submat(sv, sv+10, sh, sh+20);
                    Mat mask = new Mat(10, 20, CV_32FC3, new Scalar(0, 0, 1));
                    Mat subOrig = oSubOrig.mul(mask);
                    subOrig.copyTo(or.submat(roi));
                } else if (superpixels[superpixel] > 0.5) {
                    or.submat(roi).setTo(new Scalar(0, 0, 0));
                }
                superpixel++;
            }
        }
        return or;
    }

    public Mat paintBlackWhiteResults(float[] superpixels, Mat originalImage) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat result = new Mat(500, 500, CV_32F);
        int superpixel = 0;
        for (int sv = 0; sv < height; sv += 10) { // 50 superpixels in the height direction
            for (int sh = 0; sh < width; sh += 20) { // 25 superpixels in the width direction
                if (superpixels[superpixel] > 0.5) {
                    Rect roi = new Rect(sh, sv, 20, 10);
                    result.submat(roi).setTo(new Scalar(255));
                }
                superpixel++;
            }
        }
        return result;
    }

    public Mat createLaplacianImage(Mat originalImage) {
        Mat gradientImg = new Mat(500, 500, CvType.CV_32FC3);
        Mat or = new Mat(500, 500, CV_32FC3);
        Mat res = new Mat(500, 500, CV_32F);
        originalImage.convertTo(or, CV_32FC3);
        Imgproc.Laplacian(or, gradientImg, CvType.CV_32FC3, LAPLACIAN_K_SIZE, LAPLACIAN_SCALE, LAPLACIAN_DELTA);
        Imgproc.cvtColor(gradientImg, res, Imgproc.COLOR_BGR2GRAY);
        return res;
    }

    public float[] convertMatToFloatArr(Mat inputMat) {
        int size = (int)inputMat.total() * inputMat.channels();
        float[] imgValues = new float[size];
        // Extract the values of the image to a float array since the classifier expects
        // its input to be a float array
        inputMat.get(0, 0, imgValues);
        return imgValues;
    }
}
