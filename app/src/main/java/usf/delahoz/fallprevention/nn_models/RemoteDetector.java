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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import usf.delahoz.fallprevention.Utils;

/**
 * Created by yuengdelahoz on 2/9/18.
 */

public class RemoteDetector implements Detector{
    private String URL ="http://research.jadorno.com:8100/fallprevention";
    private String KEY_IMAGE = "image";
    private Context context;
    private long startTime;
    private final String TAG = getClass().getName();
    private long start_time,end_time;
    private String[] models = {"floor_detection","object_detectio"};
    private JSONArray nn_models;



    public RemoteDetector(Context context) {
        this.context = context;
        nn_models = new JSONArray(Arrays.asList(models));
    }

    @Override
    public float[] runInference(Mat image) {
        /**
         * This method sends the image to the Python WebAPi and saves the output as a jpg file. It also updates the log file with
         * information useful for debugging purposes (Downloads/Logs/Log.txt)
         * @param im - The image captured
         */
        start_time = System.currentTimeMillis();
        final String encodedImage = Utils.createEncodedImage(image);
        startTime = System.currentTimeMillis();
        JSONObject params = new JSONObject();
        try {
            params.put(KEY_IMAGE,encodedImage);
            params.put("models",nn_models);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL,params,future,future);

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        //Adding request to the queue
        requestQueue.add(jsonRequest);
        try {
            JSONObject response = future.get(); // this will block
            JSONArray result = response.getJSONArray("result");
            float [] superpixels = fillData(result.getJSONArray(0));
            Log.d(TAG, "superpixels " + Arrays.toString(superpixels));

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handline
        } catch (JSONException e) {
            e.printStackTrace();
        }
        end_time = System.currentTimeMillis();
        return null;
    }

    @Override
    public long getInferenceRuntime() {
        long inference_time = end_time - start_time;
        Utils.mSaveData(nn_models.toString()+"_inference_times.csv",inference_time+"",Utils.getAlbumStorageDir("exec_times"));
        return inference_time;
    }

    private float[] fillData(JSONArray jsonArray){

        float[] fData = new float[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                fData[i] = Float.parseFloat(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return fData;
    }

}
