package lab4_202_16.uwaterloo.ca.lab4_202_16_2;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {


    public static TextView scoreLog;  public static int score = 0;      //Variables that keep track of the score

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();             //Hides the Action bar
        actionBar.hide();

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_main);            //Sets the layout to relative after getting the id to the layout
        relativeLayout.getLayoutParams().width  = 1430;                                               //Sets the layout dimensions to 1430 by 1430 pixels
        relativeLayout.getLayoutParams().height = 1750;
        relativeLayout.setBackgroundResource(R.drawable.gameboard);                                   //Sets the background image to the gameboard


        final GameLoopTask gameLoopTask = new GameLoopTask(this, getApplicationContext(), relativeLayout);  //Creates an instance of the GameLoopTask to be called repeatedly
        final AccSensorHandler accSensor = new AccSensorHandler(gameLoopTask);                        //Creates an instance of the accSensorHandler which will handle all sensor events

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);                //Sensor manager requests the usage of the sensors
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);       //Allocates the sensor variable "accelerometer" as the accelerometer sensor, excluding gravity.
        sensorManager.registerListener(accSensor, accelerometer, SensorManager.SENSOR_DELAY_GAME);    //Registers the accelerometer variable, and sets the delay to game (lowest delay)

        Button resetButton = new Button(getApplicationContext());           //Button that would call the reset function
        resetButton.setText("RESET GAME");
        resetButton.setBackgroundColor(Color.WHITE);
        resetButton.setTextColor(Color.BLACK);
        resetButton.setTextSize(20f);
        resetButton.setX(150f);
        resetButton.setY(1430f);
        resetButton.bringToFront();
        relativeLayout.addView(resetButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score = 0;
                while(GameLoopTask.gameBlocks.size()!= 0) {
                    for (int i = 0; i < GameLoopTask.gameBlocks.size(); i++) {
                        relativeLayout.removeView(GameLoopTask.gameBlocks.get(i).numberText);       //Removes the TextView displaying the text of the blokc
                        relativeLayout.removeView(GameLoopTask.gameBlocks.get(i));                  //Removes the block itself
                        GameLoopTask.gameBlocks.remove(i);                                          //Removes it from the LinkedList and deletes it
                    }
                }

                for(int xAxis = 0; xAxis < 4 ; xAxis++){
                    for(int yAxis = 0; yAxis < 4 ; yAxis++){
                        GameLoopTask.gameBoardGrid[xAxis][yAxis] = false;       //Resets the gameBoardGrid to false
                    }
                }

                GameLoopTask.resetTriggered = true;                             //Triggers the reset in the GLT to say that we have to make a new block now
            }
        });

        scoreLog = new TextView(getApplicationContext());   //TextView that displays the score
        scoreLog.setX(850f);
        scoreLog.setY(1480f);
        scoreLog.setTextColor(Color.BLACK);
        scoreLog.setText(String.format("SCORE: %d", score));        //The score is continually updated in the GLT
        scoreLog.setTextSize(20f);
        relativeLayout.addView(scoreLog);


        Timer gameLoopTimer = new Timer();                         //Creates the timer that will be used to repeatedly call the gameLoopTask
        gameLoopTimer.schedule(gameLoopTask, 10, 10);              //Schedules the timer to call the gameLoopTask after a 10ms delay, every 10 ms;
        // This will perform movements every time a movement has been registered
    }
}