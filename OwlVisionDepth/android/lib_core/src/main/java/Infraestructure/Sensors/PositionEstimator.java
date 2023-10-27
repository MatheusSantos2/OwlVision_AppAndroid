package Infraestructure.Sensors;


public class PositionEstimator {
    private static final float ALPHA = 0.2f;

    public float[] getCurrentPosition(float initialX, float initialY, float initialZ, float deltaTime) {

        float deltaX = initialX  * deltaTime * deltaTime;
        float deltaY = initialY  * deltaTime * deltaTime;
        float deltaZ = initialZ  * deltaTime * deltaTime;

        return new float[]{deltaX, deltaY, deltaZ};
    }
}
