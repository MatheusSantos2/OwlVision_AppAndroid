package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class FilterSensorsLite implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private PositionEstimator positionEstimator;
    private long lastTimestamp;
    private float deltaTime;


    public FilterSensorsLite(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        positionEstimator = new PositionEstimator();
        lastTimestamp = 0;
        deltaTime = 0;
    }

    public void register() {
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTimestamp = event.timestamp;
            if (lastTimestamp != 0) {
                deltaTime = (currentTimestamp - lastTimestamp) / 1e9f;
            }
            lastTimestamp = currentTimestamp;
            float[] currentPosition = positionEstimator.getCurrentPosition(0, 0, 0, deltaTime);

            if (sensorUpdateCallback != null) {
                sensorUpdateCallback.onSensorUpdate(currentPosition);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private SensorUpdateCallback sensorUpdateCallback;

    public interface SensorUpdateCallback {
        void onSensorUpdate(float[] positions);
    }

    public void setSensorUpdateCallback(SensorUpdateCallback callback) {
        sensorUpdateCallback = callback;
    }
}

