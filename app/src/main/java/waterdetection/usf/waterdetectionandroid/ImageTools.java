package waterdetection.usf.waterdetectionandroid;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Yueng
 * Edited by Panos on 5/7/2017.
 */
public class ImageTools {

    private final String TAG_S = "SAVING";
    private final String TAG_R = "READING";

    public Mat ReadImage(File path, String name) {

        File file = new File(path, name);
        Mat src = null;
        String filename = file.toString();
        // 2.4.11
        src = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        // 3.0.0
        // src = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        if (!src.empty()) {
            Log.i(TAG_R, "SUCCESS Reading the image " + name);
            Imgproc.resize(src, src, new Size(320, 240));
        } else {
            Log.d(TAG_R, "Fail Reading the image " + name);
            return null;
        }
        return src;

    }

    public void SaveImage(Mat img, long name) { //type of 'name' was String. Changed to long
        Log.i("OpenCVLoad", "ImageTools: SaveImage");
        String dirName = "Cam 2 Pictures";
        File ph = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dirName);
        if (!ph.exists()) {
            ph.mkdirs();
        }

        File file = new File(ph, name + ".jpg");
        if (file.exists())
            file.delete();

        boolean bool = Imgcodecs.imwrite(file.toString(), img);
        if (bool == true) {
            Log.i("OpenCVLoad", "ImageTools: SaveImage: Success writing image");
        } else
            Log.d("OpenCVLoad", "ImageTools: SaveImage: Failed to write image");

    }

    public void saveImage(String image) {
        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        MatOfByte bytes = new MatOfByte(decodedString);
        Mat mat = Imgcodecs.imdecode(bytes, Imgcodecs.IMREAD_UNCHANGED);
        SaveImage(mat, System.currentTimeMillis());
    }

}
