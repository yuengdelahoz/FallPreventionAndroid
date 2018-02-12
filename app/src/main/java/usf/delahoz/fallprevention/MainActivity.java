package usf.delahoz.fallprevention;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 113;
    private static final int REQUEST_READ_STORAGE = 114;
    private String TAG = getClass().getName();

    boolean canWrite = false;
    boolean useCamera = false;
    boolean canRead = false;

    private Button btn;
    private Spinner operation_mode;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int [] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWrite = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "write request");
                }
                /** ****************************************************************/
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useCamera = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "camera request");
                }
                /** ****************************************************************/
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "read request");
                }
                /** ****************************************************************/

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasReadPermission){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
        /** ****************************************************************/
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
/** ****************************************************************/
        boolean camPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        if (!camPermission){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("At least one model must be selected");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });



        final Switch isFloor = (Switch) findViewById(R.id.floor_detection);
        final Switch isObject = (Switch) findViewById(R.id.object_detection);
        final Switch isDistance = (Switch) findViewById(R.id.distance_estimation);

//        Choose to run models locally or through the webapi
        operation_mode = (Spinner) findViewById(R.id.operation_mode);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operation_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operation_mode.setAdapter(adapter);

        final Intent processImage = new Intent(this,ImageCollectorService.class);
        final Bundle options = new Bundle();
        btn = (Button) findViewById(R.id.startBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                options.putString("operation_mode",operation_mode.getSelectedItem().toString());

                ArrayList<String> models = new ArrayList<String>();
                if (isFloor.isChecked()) models.add("floor_detection");
                if (isObject.isChecked()) models.add("object_detection");
                if (isDistance.isChecked()) models.add("distance_detection");


                options.putStringArrayList("models",models);
                processImage.putExtras(options);

                if (btn.getText().toString().equals("Launch") && models.size() >0) {  //Start
                    btn.setText("Stop");
                    operation_mode.setEnabled(false);
                    isFloor.setEnabled(false);
                    isObject.setEnabled(false);
                    isDistance.setEnabled(false);
                    startService(processImage);

                } else if (btn.getText().toString().equals("Stop")) { //Stop
                    btn.setText("Launch");
                    operation_mode.setEnabled(true);
                    isFloor.setEnabled(true);
                    isObject.setEnabled(true);
                    isDistance.setEnabled(true);
                    stopService(processImage);
                }
                else if (models.size() <1){
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
