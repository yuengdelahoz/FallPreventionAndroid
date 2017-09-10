package waterdetection.usf.waterdetectionandroid.callbacks;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import waterdetection.usf.waterdetectionandroid.ImageTools;
import waterdetection.usf.waterdetectionandroid.tfclassification.Classifier;

import static org.opencv.core.CvType.CV_32FC3;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;

/* the following acquires an image reader from the listener. this in turn lets us call the
        * method to acquire the latest image*/
public class ImageAvailableCallback implements ImageReader.OnImageAvailableListener {
    private ImageTools mat = new ImageTools();
    private Classifier classifier;

    public ImageAvailableCallback(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
            /* the following variables are used to convert the data we get to bytes,
            * and re-construct them to finally create and save an image file*/
        Image img;
        ByteBuffer buffer;
        byte[] bytes;
        //pretty self explanatory. like, c'mon now. read the line. lazy...
        img = reader.acquireLatestImage();
            /*the full code below would also have "if-else" or "else" statements
            * to check for other types of retrieved images/files */
        if (img.getFormat() == ImageFormat.JPEG) {
            //check if we have external storage to write to. if we do, save acquired image
            if (isExternalStorageWritable())
            {
                    /*method to create a new file/item/object and set
                    it up for a JPEG assignment*/
                //imgFile = CreateJPEG();
                try {
                    /*bytebuffer is a class that allows us to read/write bytes
                    * here it is used with "Save(... , ...)" to assign these
                    * collected bytes from the image to the created file from "CreateJpeg()"*/
                    buffer = img.getPlanes()[0].getBuffer();
                    int h = img.getHeight();
                    int w = img.getWidth();
                    Mat imgMat = new Mat(w,h, CvType.CV_32FC3);

                    bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    // Obtain a float array with the values of the image captured. The classifier
                    // uses this float array to perform the inference
                    MatOfByte m = new MatOfByte(bytes);
                    imgMat = imdecode(m, IMREAD_COLOR);
                    // We need to resize the image because the floor detection model expects an input
                    // image with dimensions 500x500
                    Size szResized = new Size(500,500);
                    Mat mSource = imgMat;
                    Mat mResised = new Mat();
                    Imgproc.resize(mSource, mResised, szResized,0,0, Imgproc.INTER_LINEAR);
                    Mat im = new Mat(500,500,3);
                    // We copy the mat to a new one where the data type is CV_32FC3 because it is
                    // the type expected by the classifier and otherwise it would raise an error.
                    mResised.assignTo(im, CvType.CV_32FC3);
                    int size = (int)im.total() * im.channels();
                    float[] imgValues = new float[size];
                    // Extract the values of the image to a float array since the classifier expects
                    // its input to be a float array
                    im.get(0, 0, imgValues);
                    float[] superpixels = classifier.classifyImage(imgValues); //Perform the inference on the input image
                    Mat fin = paintOriginalImage(superpixels, mResised); //Paint the results on the original image
                    mat.SaveImage(fin,System.currentTimeMillis()); //Save the output image
                    img.close();
                } catch (Exception e) {
                    Log.i("Exception e", "ImageFormat.JPEG,,,,,,," + e.getMessage());
                    e.getStackTrace();
                }
            }
        }
    }

    /**
     * Method that paints the results of the floor detection model on top of the original input image
     * When a superpixel is classified as "floor", then all the pixels in the image that belong to that
     * superpixel are colored black, so that in the end the returned image is the original image with
     * all the areas identified as floor are colored black.
     * @param superpixels - The 1250 vector with the output of the floor detection model
     * @param originalImage - The original resized image (500x500)
     * @return - A copy of the original image where all pixels classified as floor are colored black
     */
    private Mat paintOriginalImage(float[] superpixels, Mat originalImage) {
        int height = originalImage.height();
        int width = originalImage.width();
        Mat or = new Mat(500, 500, CV_32FC3);
        originalImage.convertTo(or, CV_32FC3);
        int superpixel = 0;
        for (int sv = 0; sv < height; sv += 10) { // 50 superpixels in the height direction
            for (int sh = 0; sh < width; sh += 20) { // 25 superpixels in the width direction
                if (superpixels[superpixel] > 0.5) {
                    Rect roi = new Rect(sh, sv, 20, 10);
                    Mat oSubOrig = or.submat(sv, sv+10, sh, sh+20);
                    Mat mask = new Mat(10, 20, CV_32FC3, new Scalar(0, 0, 1));
                    Mat subOrig = oSubOrig.mul(mask);
                    subOrig.copyTo(or.submat(roi));
                }
                superpixel++;
            }
        }
        return or;
    }

    //checks if there is external storage to write to
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}