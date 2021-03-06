package usf.delahoz.fallprevention;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;


/**
 * Created by Yueng
 * Edited by Panos on 5/7/2017.
 */
public class Utils {

    private static String TAG = Utils.class.getName();
    private final static int LAPLACIAN_K_SIZE = 3;
    private final static int LAPLACIAN_DELTA = 0;
    private final static int LAPLACIAN_SCALE = 1;

    public static void SaveImage(String image) {
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        MatOfByte bytes = new MatOfByte(decodedString);
        Mat mat = imdecode(bytes, Imgcodecs.IMREAD_UNCHANGED);
        SaveImage(mat, System.currentTimeMillis());
    }

    public static void SaveImage(Mat img, long name) { //type of 'name' was String. Changed to long

        File file = new File(getAlbumStorageDir("cam2pics"), name + ".jpg");
        if (file.exists())
            file.delete();

        boolean bool = Imgcodecs.imwrite(file.toString(), img);
        if (bool == true) {
            Log.d(TAG, "Utils: SaveImage: Success writing image " + file.getAbsolutePath());
        } else
            Log.d(TAG, "Utils: SaveImage: Failed to write image");

    }
    /**
     * Method that paints the results of the floor detection model on top of the original input image
     * When a superpixel is classified as "floor", then all the pixels in the image that belong to that
     * superpixel are colored black if obscured is true, so that in the end the returned image is the original image with
     * all the areas identified as floor are colored black. If obscure is false, then we apply the red filter
     * @param superpixels - The 1250 vector with the output of the floor detection model
     * @param originalImage - The original resized image (500x500)
     * @param obscure - true to paint floor pixels black. False to apply the red filter
     * @return - A copy of the original image where all pixels classified as floor are colored black
     */
    public static Mat paintOriginalImage(float[] superpixels, Mat originalImage, boolean obscure) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat or = new Mat(240, 240, CV_8UC3);
        originalImage.convertTo(or, CV_8UC3);
        int superpixel = 0;
        for (int sv = 0; sv < height; sv += 10) { // 50 superpixels in the height direction
            for (int sh = 0; sh < width; sh += 20) { // 25 superpixels in the width direction
                Rect roi = new Rect(sh, sv, 20, 10);
                if (superpixels[superpixel] > 0.5 && !obscure) {
                    Mat oSubOrig = or.submat(sv, sv+10, sh, sh+20);
                    Mat mask = new Mat(10, 20, CV_8UC3, new Scalar(0, 0, 1));
                    Mat subOrig = oSubOrig.mul(mask);
                    subOrig.copyTo(or.submat(roi));
                } else if (superpixels[superpixel] <= 0.5 && obscure) {
                    or.submat(roi).setTo(new Scalar(0, 0, 0));
                }
                superpixel++;
            }
        }
        return or;
    }

    public static Mat paintFloorImage(float[] superpixels, Mat originalImage) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat or = new Mat(240, 240, CV_8UC3);
        originalImage.convertTo(or, CV_8UC3);
        int superpixel = 0;
        for (int sv = 0; sv < height; sv += 8) { // 30 superpixels in the height direction
            for (int sh = 0; sh < width; sh += 8) { // 30 superpixels in the width direction
                Rect roi = new Rect(sh, sv, 8, 8);
                if (superpixels[superpixel] <= 0.5) {
                    or.submat(roi).setTo(new Scalar(255, 255, 255));
                }
                superpixel++;
            }
        }
        return or;
    }

    public static Mat paintBlackWhiteResults(float[] superpixels, Mat originalImage) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat result = new Mat(240, 240, CV_8UC1);
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

    /**
     * Computes the LAplacian edge detection on the original input image (500x500)
     * @param originalImage - 500x500 input image to compute the LAplacian detection on
     * @return - The black and white laplacian edge detection image
     */
    public static Mat createLaplacianImage(Mat originalImage) {
        Mat gradientImg = new Mat();
        Mat or = new Mat();
        Imgproc.cvtColor(originalImage, or, Imgproc.COLOR_BGR2GRAY);
        or.convertTo(or, CV_8U);
        Imgproc.Laplacian(or, gradientImg, CvType.CV_8U, LAPLACIAN_K_SIZE, LAPLACIAN_SCALE, LAPLACIAN_DELTA);
        return gradientImg;
    }

    /**
     * Obtains the normalized float array of values from the Mat object
     * @param inputMat
     * @return
     */
    public static float[] convertMatToFloatArr(Mat inputMat) {
        Mat normalized = new Mat();
        inputMat.convertTo(normalized, CV_32FC3, 1.0/255.0);
        int size = (int)normalized.total() * normalized.channels();
        float[] imgValues = new float[size];
        // Extract the values of the image to a float array since the classifier expects
        // its input to be a float array
        normalized.get(0, 0, imgValues);
        return imgValues;
    }

    public static void mSaveData(String file, String line, File albumStorageDir){
        try {
            File mFile = new File(albumStorageDir, file);
            FileWriter writer = new FileWriter(mFile, true);
            BufferedWriter output = new BufferedWriter(writer);
            output.append(line);
            output.newLine();  // This is safer than using '\n'
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getAlbumStorageDir(String albumName) {
        // Path is Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),albumName);
        if (!file.mkdirs()) {
            // Shows this error also when directory already existed
//            Log.d("Error", "Directory not created");
        }
        return file;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Creates a base64 encoded image from the captured image
     * @param im - The map object representing the captured image
     * @return - The image encoded as base64 string
     */
    public static String createEncodedImage(Mat im) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", im, matOfByte);
        byte[] imageBytes = matOfByte.toArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * From the captured image it creates the 500x500 input Mat object
     * @param img - Captured image
     * @return - TF Model input mat
     */
    public static Mat createInputMat(Image img) {
        /*bytebuffer is a class that allows us to read/write bytes
                    * here it is used with "Save(... , ...)" to assign these
                    * collected bytes from the image to the created file from "CreateJpeg()"*/
        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        MatOfByte m = new MatOfByte(bytes);
        Mat imgMat = imdecode(m, Imgcodecs.IMREAD_COLOR);
//        Log.d(TAG,"Mat height, width: " + imgMat.rows() +", "+imgMat.cols() + " Image height, width: " + img.getHeight() + ", " + img.getWidth());
        return imgMat.submat(0,240,39,279);
    }
}
