package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.opencv.video.KalmanFilter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class DistanceCalculator implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private long lastUpdate;
    private float[] acceleration;
    private float[] magneticField;
    private float[] rotationMatrix;
    private float[] orientation;
    private float[] velocity;
    private float[] distance;
    private KalmanFilter kalmanFilter;
    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public DistanceCalculator(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.lastUpdate = 0L;
        this.acceleration = new float[3];
        this.magneticField = new float[3];
        this.rotationMatrix = new float[9];
        this.orientation = new float[3];
        this.velocity = new float[3];
        this.distance = new float[3];
        this.kalmanFilter = new KalmanFilter();
    }
    private SensorUpdateCallback sensorUpdateCallback;

    public interface SensorUpdateCallback {
        void onSensorUpdate(float[] positions);
    }

    public void setSensorUpdateCallback(SensorUpdateCallback callback) {
        sensorUpdateCallback = callback;
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void applySensorFusion() {
        float[] rotationMatrix = new float[9];
        float[] inclinationMatrix = new float[9];

        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, acceleration, magneticField);

        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotationMatrix);

        SensorManager.getOrientation(rotationMatrix, orientation);
    }

    private void applyKalmanFilter() {
        float alpha = 0.8f; // Smoothing factor (0 < alpha < 1)
        float deltaTime = (System.currentTimeMillis() - lastUpdate) / 1000.0f;

        velocity[0] = alpha * velocity[0] + (1 - alpha) * acceleration[0] * deltaTime;
        velocity[1] = alpha * velocity[1] + (1 - alpha) * acceleration[1] * deltaTime;
        velocity[2] = alpha * velocity[2] + (1 - alpha) * acceleration[2] * deltaTime;

        distance[0] += velocity[0] * deltaTime;
        distance[1] += velocity[1] * deltaTime;
        distance[2] += velocity[2] * deltaTime;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleration = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticField = event.values.clone();
        }

        if (acceleration != null && magneticField != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magneticField);
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Apply sensor fusion techniques
            applySensorFusion();

            // Apply Kalman filter for further refinement
            applyKalmanFilter();

            lastUpdate = System.currentTimeMillis();

            formatCurrentPosition();

            if (sensorUpdateCallback != null) {
                sensorUpdateCallback.onSensorUpdate(distance);
            }
        }
    }

    private void formatCurrentPosition()
    {
        try {
            distance[0] =  decimalFormat.parse(decimalFormat.format(distance[0])).floatValue();
            distance[1] =  decimalFormat.parse(decimalFormat.format(distance[1])).floatValue();
            distance[2] =  decimalFormat.parse(decimalFormat.format(distance[2])).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



