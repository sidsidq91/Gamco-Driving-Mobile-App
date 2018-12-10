package personal.com.gamcodrivingapp;

public class Car {
    public static final float FREE_BREAKING = (float) -0.15;
    public static final float BREAKING = (float) -2;
    public static final float ACCELERATION = (float) 1.5;
    private float speed=0;
    private int angel=0;
    private int maxSpeed = 120;
    private float accelerataion= (float) 0;
    private boolean accelerating=false;
    private boolean BeltClosed=false;
    public void update(){
        if (accelerating){
            speed += accelerataion;
            if (speed>=maxSpeed)
                speed = maxSpeed;
            if (speed<=0)
                speed=0;
        }
    }

    public float getAccelerataion() {
        return accelerataion;
    }

    public void setAccelerataion(float accelerataion) {
        this.accelerataion = accelerataion;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isAccelerating() {
        return accelerating;
    }

    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }

    public boolean isBeltClosed() {
        return BeltClosed;
    }

    public void setBeltClosed(boolean beltClosed) {
        BeltClosed = beltClosed;
    }

    public int getAngel() {
        return angel;
    }

    public void setAngel(int angel) {
        this.angel = angel;
    }
}
