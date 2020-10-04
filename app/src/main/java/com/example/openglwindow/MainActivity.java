package com.example.openglwindow;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public EditText editBox;
    Sensor mMagnetoMeter;
    private Choreographer.FrameCallback frameCallback = null;
    private boolean frameCallbackPending = false;

    public void armVSyncHandler() {
        if(!frameCallbackPending) {
            frameCallbackPending = true;
            if(frameCallback == null)
            {
                frameCallback = new Choreographer.FrameCallback() {
                    @Override
                    public void doFrame(long frameTimeNanos) {
                        frameCallbackPending = false;

                        //updateFrame();

                        armVSyncHandler();
                    }
                };
            }
            Choreographer.getInstance().postFrameCallback(frameCallback);
        }
    }

    Object[] objects = new Object[11];
    Camera camera;

    public void setObjects(Object[] newObjects){
        objects = newObjects;
    }

    public void setCamera(Camera newCamera){
        camera = newCamera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

//        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
//            Log.d("tag", "accelerometer!!");
//        } else {
//            Log.d("tag", "no accelerometer");
//        }
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
//            Log.d("tag", "magnetic field!!");
//        } else {
//            Log.d("tag", "no magnetic field");
//        }

        //mMagnetoMeter = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        editBox = new EditText(this);
        editBox.setText("Hello Matron");

        //SensorActivity a=new SensorActivity();

        addContentView(editBox, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        updateOrientationAngles();
    }

    float[] moveMatrix={2.3f, 0.2f,0.75f};
    float movementSpeed=0.001f;

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        // "mOrientationAngles" now has up-to-date information.

        float azimuth=Math.round((Math.toDegrees(orientationAngles[0])));
        float pitch=Math.round((Math.toDegrees(orientationAngles[1])));
        float roll=Math.round((Math.toDegrees(orientationAngles[2])));

        editBox.setText("Azimuth: "+azimuth+
                ",\n Pitch: "+pitch+
                ",\n Roll: "+roll);

        float[] newMatrix = {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                moveMatrix[0]+(-movementSpeed*pitch) , 0.2f, moveMatrix[2]+(-movementSpeed*roll), 1.0f
        };

        moveMatrix[0]=moveMatrix[0]+(-movementSpeed*pitch);
        moveMatrix[2]=moveMatrix[2]+(-movementSpeed*roll);

        objects[0].changeTransform(newMatrix);

        checkCollision();

        //camera
        //camera.setCamera(new float[]{1.5f, 0.5f, -0.3f}, new float[]{0f, 0f, 0f});
        camera.setCamera(new float[]{1.5f+moveMatrix[0], 0.5f, -0.3f+moveMatrix[2]},
                new float[]{moveMatrix[0], 0f, moveMatrix[2]});

    }

    public void checkCollision(){
        float[] ballHitbox=objects[0].getHitBox();
        for (int i=6; i<11; i++){
            float[] boxHitbox=objects[i].getHitBox();
            for (int j=0; j<8; j++){
//                if (boxHitbox[0]>ballHitbox[j] && boxHitbox[1]<ballHitbox[j+1] &&
//                        boxHitbox[2]<ballHitbox[j] && boxHitbox[1]<ballHitbox[j+1] &&
//                boxHitbox[4]>ballHitbox[j] && boxHitbox[5]>ballHitbox[j+1] &&
//                boxHitbox[6]<ballHitbox[j] && boxHitbox[7]>ballHitbox[j+1]){
                    j++;
                    //return true;
                    Log.d("tag", "Collision at "+j+". object");
//                }
            }
        }

    }


}
