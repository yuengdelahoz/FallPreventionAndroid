package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import java.util.concurrent.ExecutionException;

import usf.delahoz.fallprevention.Utils;

/**
 * Created by yuengdelahoz on 2/9/18.
 */

public class RemoteDetector implements Detector{
    private String URL ="http://research.jadorno.com:8100/waterdetection";
    private String KEY_IMAGE = "image";
    private Context context;
    private long startTime;
    private final String TAG = getClass().getName();


    public RemoteDetector(Context context) {
        this.context = context;
    }

    @Override
    public Mat runInference(Mat image) {
        /**
         * This method sends the image to the Python WebAPi and saves the output as a jpg file. It also updates the log file with
         * information useful for debugging purposes (Downloads/Logs/Log.txt)
         * @param im - The image captured
         */
        Log.d(TAG,"Running remote Inference. " + Thread.currentThread().getId());
        final String encodedImage = Utils.createEncodedImage(image);
        startTime = System.currentTimeMillis();
        JSONObject parm = new JSONObject();
        try {
            parm.put(KEY_IMAGE,encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL,parm,future,future);

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        //Adding request to the queue
        requestQueue.add(jsonRequest);
        try {
            JSONObject response = future.get(); // this will block
            JSONArray result = response.getJSONArray("result");
            Log.d(TAG,result.toString());

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
