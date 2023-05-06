package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorsListener implements SensorEventListener
{
    private SensorManager sensorManager;

    float[] accelerometerData = new float[3];
    float[] gyroscopeData = new float[3];
    float[] angularVelocityData = new float[3];
    long timeInterval;

    public SensorsListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void register() {
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeData = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            angularVelocityData = event.values.clone();
            timeInterval = event.timestamp - timeInterval;
        }
    }

    public float[] getAccelerometer() { return accelerometerData; }

    public float[] getGyroscope() { return gyroscopeData; }

    public float[] getAngularVelocity() { return angularVelocityData; }

    public float[] getTimeInterval() { return accelerometerData; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}