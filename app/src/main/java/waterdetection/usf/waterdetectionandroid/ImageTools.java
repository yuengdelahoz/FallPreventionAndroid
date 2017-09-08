package waterdetection.usf.waterdetectionandroid;


import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by Yueng
 * Edited by Panos on 5/7/2017.
 */
public class ImageTools {

    private final String TAG_S = "SAVING";
    private final String TAG_R = "READING";
    public static int kval = 0;

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
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File ph = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dirName);

        String filename = name + ".jpg";
        File file = new File(ph, filename);
        if (file.exists())
            file.delete();
        Boolean bool = null;

        String filenm = file.toString();

        Size szResized = new Size(500,500);
        Mat mSource = img;
        Mat mResised = new Mat();
        Imgproc.resize(mSource, mResised, szResized,0,0, Imgproc.INTER_LINEAR);

        bool = Imgcodecs.imwrite(filenm, mResised);

        if (bool == true) {
            Log.i("OpenCVLoad", "ImageTools: SaveImage: Success writing image");
        } else
            Log.d("OpenCVLoad", "ImageTools: SaveImage: Failed to write image");

    }

}
