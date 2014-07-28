package br.usp.poli.thales.deteccaomovimento;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	static final float ALPHA = 0.20f;
	Sensor accelerometer;
	SensorManager sm;
	float[] oldvalues, valuesOrient;
	float[] speed;
	private long lastUpdate = 0;
	private long curTime = 0;;

    float[] rotMat = new float[9];
    float[] vals = new float[3];
    float[] angles;
	
	private Sensor mSensor;
	
	boolean mov = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        oldvalues = new float[3];
        speed = new float[3];
        
        mSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sm.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    @Override
    public void onAccuracyChanged (Sensor sensor, int accuracy){
    }
    
    protected float[] lowPass( float[] input, float[] output ) {
    	if ( output == null ) 
    		return input;
		for ( int i=0; i<input.length; i++ ) {
			output[i] = (1-ALPHA)*output[i] + ALPHA * input[i];
		}
		return output;
	}

    @Override
    public void onSensorChanged (SensorEvent event){
    	TextView movimento = (TextView)findViewById(R.id.textView1);
    	
    	if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
	    	TextView textoacc = (TextView) findViewById(R.id.acceleration);
	    	oldvalues = lowPass(event.values.clone(), oldvalues);
	    	
	    	textoacc.setText("X: " + 
	    		    oldvalues[0] + "\nY: " + oldvalues[1] + "\nZ: " + oldvalues[2] + "\n\n\nX: " + 
	    		    speed[0] + "\nY: " + speed[1] + "\nZ: " + speed[2]);
	    		    	
	    	curTime = System.currentTimeMillis();
//	    	if (lastUpdate < 10)
//	    		lastUpdate = curTime;
//	    	float limite = 0.3f;
//	    	if(Math.abs(oldvalues[0]) > limite || Math.abs(oldvalues[1]) > limite || Math.abs(oldvalues[2]) > limite){
//	    		movimento.setText("EM MOVIMENTO");
//	    	} else {
//	    		movimento.setText("PARADO");
//	    	}
//	    	
	    	for(int i = 0; i < speed.length; i++){
	    		speed[i] = ((float)(curTime-lastUpdate)/1000)*oldvalues[i]+speed[i];
	    	}
	    	lastUpdate = curTime;
    	}

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotMat,
                    event.values);
            
            SensorManager
                    .remapCoordinateSystem(rotMat,
                            SensorManager.AXIS_X, SensorManager.AXIS_Y,
                            rotMat);
            SensorManager.getOrientation(rotMat, vals);
            
            valuesOrient = new float[3];
            
            valuesOrient[0] = (float)Math.toDegrees(vals[0]); // in degrees [-180, +180]
            valuesOrient[1] = (float)Math.toDegrees(vals[1]);
            valuesOrient[2] = (float)Math.toDegrees(vals[2]);

            if(angles == null){
            	angles = new float[3];
            	angles = valuesOrient.clone();
            } else {
            	if(Math.abs(valuesOrient[0]-angles[0]) >= 45 ||
            	   Math.abs(valuesOrient[1]-angles[1]) >= 45 ||
            	   Math.abs(valuesOrient[2]-angles[2]) >= 45
            	) {
            		angles = valuesOrient.clone();
            		mov = !mov;
            		if(!mov)
            			movimento.setText("parado");
            		else
            			movimento.setText("em movimento");
            	}
            }
            
        	TextView tv2 = (TextView)findViewById(R.id.textView2);
        	tv2.setText("Angulos:\nX: " + 
        			valuesOrient[0] + "\nY: " + valuesOrient[1] + "\nZ: " + valuesOrient[2]);
        	
        }

    	
    }
}
