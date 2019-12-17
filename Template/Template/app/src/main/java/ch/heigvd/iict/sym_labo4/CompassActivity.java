package ch.heigvd.iict.sym_labo4;

import android.app.Activity;
import android.content.Context;
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

public class CompassActivity extends AppCompatActivity {

    //opengl
    private OpenGLRenderer  opglr           = null;
    private GLSurfaceView   m3DView         = null;
    private SensorManager sensorManager= null;
    private Sensor accelerometer = null;
    private Sensor magnetic = null;
    float[] acceleromterValues = new float[3];
    float[] magneticValues = new float[3];
    float[] resultValues = new float[16];
    OpenGLRenderer openGLRenderer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        openGLRenderer = new OpenGLRenderer(this);


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

    /* TODO
        your activity need to register to accelerometer and magnetometer sensors' updates
        then you may want to call
        this.opglr.swapRotMatrix()
        with the 4x4 rotation matrix, everytime a new matrix is computed
        more information on rotation matrix can be found on-line:
        https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],%20float[],%20float[],%20float[])
    */



    final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //mettre à jour la valeur de l'accéléromètre et du champ magnetique
            if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                acceleromterValues = sensorEvent.values;
            }
            if( sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                magneticValues = sensorEvent.values;
            }

            SensorManager.getRotationMatrix(resultValues, null, acceleromterValues, magneticValues );
            openGLRenderer.swapRotMatrix(resultValues);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(mSensorEventListener, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mSensorEventListener, magnetic,
                SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(mSensorEventListener, accelerometer);
        //sensorManager.unregisterListener(mSensorEventListener, magnetic);
    }

}
