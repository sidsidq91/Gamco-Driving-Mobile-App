package personal.com.gamcodrivingapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static personal.com.gamcodrivingapp.MainActivity.Online;
import static personal.com.gamcodrivingapp.MainActivity.recieveData;
import static personal.com.gamcodrivingapp.MainActivity.sendDataOut;
import static personal.com.gamcodrivingapp.MainActivity.setIncomeAsZero;

public class PlayGround extends AppCompatActivity implements SensorEventListener{

    private ImageView wheelHolder;
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private TextView speedTextView;
    private TextView scoreBoard;

    private LinearLayout dangerBox;
    private TextView dangerBoxTextView;

    private ImageView gasHolder;
    private ImageView breakHolder;

    private Vibrator v;

    private Handler handler;
    private Runnable updateRunnable;

    private int secCounter=0;

    private int Score;
    private final int TIME_SCORE = 2;
    private final int OVER_SPEED_SCORE = 2;
    private final int CRITICAL_OVER_SPEED_SCORE = 5;
    private final int PEDISTRIAN_LINE_BREAK = 1;
    private final int PASS_TRAFFIC_LIGHT = 10;
    private final int BELT_SCORE = 10;
    private final int CRITICAL_SAFE_SPEED = 2;
    private final int DAMAGE_SCORE = 5;
    private final int LINE_DROPPING_SCORE = 3;
    private boolean traffic_shit_happned=false;


    private Car car;

    private float[] mGravity;
    private float prevAngel=0;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_ground);
        speedTextView = findViewById(R.id.speedTextView);
        scoreBoard = findViewById(R.id.scoreboard);

        v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        dangerBox = findViewById(R.id.dangerBox);
        dangerBoxTextView = findViewById(R.id.dangerBoxTextView);
        dangerBox.setVisibility(View.INVISIBLE);

        scoreBoard.setText("0");
        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        car = new Car();
        car.setAccelerating(true);
        wheelHolder = findViewById(R.id.wheelHolder);
        gasHolder = findViewById(R.id.gas_holder);
        gasHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        gasHolder.setImageDrawable(getResources().getDrawable(R.drawable.gas_pressed));
                        car.setAccelerataion(Car.ACCELERATION);
                        break;
                    case MotionEvent.ACTION_UP:
                        car.setAccelerataion(Car.FREE_BREAKING);
                        gasHolder.setImageDrawable(getResources().getDrawable(R.drawable.gas_not_pressed));
                        break;
                }
                return true;
            }
        });
        breakHolder = findViewById(R.id.break_holder);
        breakHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        car.setAccelerataion(Car.BREAKING);
                        breakHolder.setImageDrawable(getResources().getDrawable(R.drawable.break_pressed));
                        break;
                    case MotionEvent.ACTION_UP:
                        car.setAccelerataion(Car.FREE_BREAKING);
                        breakHolder.setImageDrawable(getResources().getDrawable(R.drawable.break_not_pressed));
                        break;
                }
                return true;
            }
        });
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                update();

            }
        };
        handler.postDelayed(updateRunnable, 50);
    }

    private void update() {
        car.update();
        prossesIncomingData(recieveData());
        speedTextView.setText((int)car.getSpeed()+"  KM/H");
        if (!traffic_shit_happned){
            scoreBoard.setTextColor(getResources().getColor(R.color.green));
        }else {
            scoreBoard.setTextColor(getResources().getColor(R.color.red));
        }
        scoreBoard.setText(" "+Score);
        secCounter++;
        if (secCounter == 20){
            secCounter =0;
        }
        if (car.getSpeed()>70){
            vibrate(30);
            if (secCounter==0) {
                Score -= OVER_SPEED_SCORE;
            }
            showAlert("سرعت بالا",70);
        }
        if (car.getSpeed()>110){
            vibrate(70);
            if (secCounter==0) {
                Score -= CRITICAL_OVER_SPEED_SCORE;
            }
            Score-=CRITICAL_OVER_SPEED_SCORE;
            showAlert("سرعت پرخطر",110);
        }
        if (car.getSpeed()<70&&car.getSpeed()>3){
            if (secCounter==0){
                Score+=TIME_SCORE;
                if (!traffic_shit_happned){
                    Score+=CRITICAL_SAFE_SPEED;
                }
            }
            traffic_shit_happned=false;
        }
        if (car.getSpeed()<30&&car.getSpeed()>3){
            if (secCounter==0){
                Score+=TIME_SCORE;
            }
            traffic_shit_happned=false;
        }

        if (Score<=-2){
            handler.removeCallbacks(updateRunnable);
            car.setSpeed(0);
            car.setAccelerataion(0);
            car.setAccelerating(false);
            try{
                String data = car.getSpeed()+","+car.getAngel();
                if (Online) {
                    sendDataOut(data);
                }
            }catch (Exception e){

            }
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(PlayGround.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(PlayGround.this);
            }
            builder.setTitle("بازی تمام شد!")
                    .setMessage("شما باختید! در دفعات بعد بیشتر به قوانین توجه کنید!")
                    .setPositiveButton("خیلی خب!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PlayGround.this.finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_menu_help)
                    .show();
        }else {
            handler.postDelayed(updateRunnable,100);
        }
        try{
            String data = car.getSpeed()+","+car.getAngel();
            if (Online) {
                sendDataOut(data);
            }
        }catch (Exception e){

        }
        //todo add sending data
    }

    private void prossesIncomingData(String s) {
        System.out.println("incoming data from plyaer = "+s);
        switch (s){
            case "1":
                showAlert("توقف در پارک ممنوع",0);
                Score-=PEDISTRIAN_LINE_BREAK;
                break;
            case "2":
                showAlert("عبور از چراغ قرمز",0);
                Score-=PASS_TRAFFIC_LIGHT;
                break;
            case "3":
                showAlert("برخورد با موانع",0);
                Score-=DAMAGE_SCORE;
                break;
            case "4":
                showAlert("خروج از خط",0);
                Score-=LINE_DROPPING_SCORE;
                break;
            case "5":
                break;
            case "6":
                break;
        }
        setIncomeAsZero();
    }


    @Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            rotate((int) (10*mGravity[1]));
            car.setAngel((int) (10*mGravity[1]));
            // Make this higher or lower according to how much
            // motion you want to detect
            if(mAccel > 3){

            }
        }
    }

    private void rotate(int angle){
        wheelHolder.clearAnimation();
        RotateAnimation rotate = new RotateAnimation(prevAngel, angle,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        rotate.setDuration(100);
        prevAngel = angle;
        rotate.setFillAfter(true);
        wheelHolder.setAnimation(rotate);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void vibrate(int durition){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durition, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(durition);
        }
    }

    private void showAlert(String msg, final int limitSpeed){
        dangerBox.setVisibility(View.VISIBLE);
        dangerBoxTextView.setText(msg);
        traffic_shit_happned = true;
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (limitSpeed !=0 ) {
                    if (car.getSpeed() < limitSpeed) {
                        dangerBox.setVisibility(View.INVISIBLE);
                    }
                }else {
                    dangerBox.setVisibility(View.INVISIBLE);
                }
            }
        },1500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Score=-3;
    }
}
