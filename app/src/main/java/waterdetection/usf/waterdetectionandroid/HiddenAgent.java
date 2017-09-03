package waterdetection.usf.waterdetectionandroid;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;

public class HiddenAgent extends Service implements SensorEventListener {
    private SensorManager mySensorManager;
    private Messenger mHandler = new Messenger(new InputHandler());

    static final int Start = 1;
    static final int Stop = 2;
    static final int briefing = 3;
    static final int dismissed = 4;
    public static final long NOTIFY_INTERVAL = 100;  // 100 ms
    public static final long WRITE_INTERVAL = 200;   // 100 ms
    public static final double THRESHOLD = 10.25;    // 10.25 m/s^2


    private Timer timer1 = new Timer();
    private Timer timer2 = new Timer();
    private Message serviceMessage;
    private Message incomingMessage = new Message();
    private Bundle data = new Bundle();
    private SendTask sTask;
    private WriteTask wTask;
    private int stepCount = 0;

    private class SendTask extends TimerTask {
        @Override
        public void run(){
            try {
                incomingMessage.replyTo.send(serviceMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Task to write to a file with dirName and fileName as specified below
    private class WriteTask extends TimerTask {
        @Override
        public void run(){
            String dirName = "Test Data";
            String fileName = "panos.csv";
            mSaveData(dirName,fileName);
        }
    }

    // Check to see if external storage is accessible
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public File getAlbumStorageDir(String albumName) {
        // Path is Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),albumName);
        if (!file.mkdirs()) {
            // Shows this error also when directory already existed
            Log.e("Error", "Directory not created");
        }
        return file;
    }

    // This method only saves accel values (x,y,z), gyro values (x,y,z) and time(hh:mm:ss ms).
    private void mSaveData(String dir, String file){
        Date date = new Date();
        if(isExternalStorageWritable()) {
            try {
                File directory = getAlbumStorageDir(dir);
                File mFile = new File(directory, file);
                FileWriter writer = new FileWriter(mFile, true);
                BufferedWriter output = new BufferedWriter(writer);
                float [] accel = data.getFloatArray("Accelerometer");
                float [] gyro = data.getFloatArray("Gyroscope");
                if(accel != null && gyro != null) {
                    output.append(accel[0] + "," + accel[1] + "," + accel[2] + "," +
                            gyro[0] + "," + gyro[1] + "," + gyro[2] + "," +
                            date.getHours()+ ":" + date.getMinutes() + ":" + date.getSeconds() + "," + System.currentTimeMillis()%1000);
                }
                output.newLine();  // This is safer than using '\n'
                output.close();
                Log.e("Error", "Wrote");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    class InputHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            incomingMessage.replyTo = msg.replyTo;
            switch(msg.what) {
                case Start:
                    serviceMessage = android.os.Message.obtain(null, briefing);
                    serviceMessage.setData(data);
                    sTask = new SendTask();
                    wTask = new WriteTask();
                    timer1.scheduleAtFixedRate(sTask, 100, NOTIFY_INTERVAL);
                    timer2.scheduleAtFixedRate(wTask,1000,WRITE_INTERVAL);
                    break;
                case Stop:
                    Message message = android.os.Message.obtain(null, dismissed);
                    message.setData(data);
                    try {
                        msg.replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    sTask.cancel();
                    wTask.cancel();
                    break;
                default: super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mHandler.getBinder();
    }

    @Override
    public void onCreate(){
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor myGravity = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor myLAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor myMagnetometer = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor myLight = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor myProximity = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor myBarometer = mySensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor myStepCounter = mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mySensorManager.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myLAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myLight, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myBarometer, SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                data.putFloatArray("Accelerometer", event.values);
                if(abs(event.values[0]) > THRESHOLD){
                    data.putDouble("Step counter", ++stepCount);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                data.putFloatArray("Gyroscope", event.values);
                break;
            case Sensor.TYPE_GRAVITY:
                data.putFloatArray("Gravity", event.values);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                data.putFloatArray("Linear acceleration", event.values);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                data.putFloatArray("Magnetic field", event.values);
                break;
            case Sensor.TYPE_LIGHT:
                data.putFloatArray("Light", event.values);
                break;
            case Sensor.TYPE_PROXIMITY:
                data.putFloatArray("Proximity", event.values);
                break;
            case Sensor.TYPE_PRESSURE:
                data.putFloatArray("Pressure", event.values);
                break;
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        mySensorManager.unregisterListener(this);
        Toast.makeText(this,"HiddenAgent MIA", Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
