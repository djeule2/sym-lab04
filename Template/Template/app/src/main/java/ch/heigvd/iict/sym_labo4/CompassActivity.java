package ch.heigvd.iict.sym_labo4;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer;

/**
 * Project: Labo4
 * Created by fabien.dutoit on 09.08.2019
 * Updated by Matthieu Girard & Olivier Djeuzeleck
 * (C) 2019 - HEIG-VD, IICT
 */

public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    //opengl
    private OpenGLRenderer  opglr           = null;
    private GLSurfaceView   m3DView         = null;
    private SensorManager sensorManager= null;
    private Sensor accelerometer = null;
    private Sensor magnetic = null;
    private final float[] acceleromterValues = new float[3];
    private final float[] magneticValues = new float[3];
    private float[] resultValues = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        opglr = new OpenGLRenderer(this);


        // we need fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // we initiate the view
        setContentView(R.layout.activity_compass);

        //we create the renderer
        this.opglr = new OpenGLRenderer(getApplicationContext());

        // link to GUI
        this.m3DView = findViewById(R.id.compass_opengl);

        //init opengl surface view
        this.m3DView.setRenderer(this.opglr);

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Get readings from accelerometer and magnetometer.
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(sensorEvent.values, 0, acceleromterValues,0, acceleromterValues.length);
        }
        else if( sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(sensorEvent.values, 0, magneticValues, 0, magneticValues.length);
        }


        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(resultValues, null, acceleromterValues, magneticValues );
        resultValues = opglr.swapRotMatrix(resultValues);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume(){
        super.onResume();
        // Get updates from the accelerometer and magnetometer at a constant rate.
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(accelerometer!=null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        if(magnetic!=null)
            sensorManager.registerListener(this, magnetic,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause(){
        super.onPause();
        //Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

}
