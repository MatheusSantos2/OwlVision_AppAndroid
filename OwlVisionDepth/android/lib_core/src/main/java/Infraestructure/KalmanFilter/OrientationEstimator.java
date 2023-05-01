package Infraestructure.KalmanFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import org.ejml.simple.SimpleMatrix;

public class OrientationEstimator implements SensorEventListener {
    // state vector [x, y, z, vx, vy, vz, qx, qy, qz, qw, wx, wy, wz]
    public SimpleMatrix state = new SimpleMatrix(13, 1);

    // state transition matrix
    public SimpleMatrix F = SimpleMatrix.identity(13);

    // measurement matrix
    public SimpleMatrix H = new SimpleMatrix(9, 13);

    // process noise covariance matrix
    public SimpleMatrix Q = new SimpleMatrix(13, 13);

    // measurement noise covariance matrix
    public SimpleMatrix R = new SimpleMatrix(9, 9);

    // Kalman gain
    public SimpleMatrix K = new SimpleMatrix(13, 9);

    //Predict
    public SimpleMatrix P = new SimpleMatrix(13, 13);

    public float[] accelerometerData = new float[3];
    public float[] gyroscopeData = new float[3];
    public float[] angularVelocityData = new float[3];

    private long timeInterval;

    public OrientationEstimator() {

        H.set(0,3,1);
        H.set(1,4,1);
        H.set(2,5,1);
        H.set(3,6,1);
        H.set(4,7,1);
        H.set(5,8,1);
        H.set(6,9,1);
        H.set(7,10,1);
        H.set(8,11,1);
        // initialize matrices
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

        predict();
        update();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void predict() {
        // update state transition matrix
        F.set(3, 6, timeInterval);
        F.set(4, 7, timeInterval);
        F.set(5, 8, timeInterval);
        F.set(9, 12, timeInterval);
        F.set(10, 13, timeInterval);
        F.set(11, 14, timeInterval);
        F.set(11, 14, timeInterval);
        F.set(12, 15, timeInterval);

        // propagate the state
        state = F.mult(state);

        // propagate the covariance
        Q.set(3, 3, timeInterval);
        Q.set(4, 4, timeInterval);
        Q.set(5, 5, timeInterval);
        Q.set(9, 9, timeInterval);
        Q.set(10, 10, timeInterval);
        Q.set(11, 11, timeInterval);
        Q.set(12, 12, timeInterval);
        P = F.mult(P).mult(F.transpose()).plus(Q);
    }

    private void update() {
        // calculate Kalman gain
        K = P.mult(H.transpose()).mult(H.mult(P).mult(H.transpose()).plus(R).invert());

        // measurement
        SimpleMatrix z = new SimpleMatrix(9, 1);
        z.set(0, 0, accelerometerData[0]);
        z.set(1, 0, accelerometerData[1]);
        z.set(2, 0, accelerometerData[2]);
        z.set(3, 0, gyroscopeData[0]);
        z.set(4, 0, gyroscopeData[1]);
        z.set(5, 0, gyroscopeData[2]);
        z.set(6, 0, angularVelocityData[0]);
        z.set(7, 0, angularVelocityData[1]);
        z.set(8, 0, angularVelocityData[2]);

        // update state estimate
        state = state.plus(K.mult(z.minus(H.mult(state))));

        // update covariance
        P = P.minus(K.mult(H).mult(P));
    }
}
