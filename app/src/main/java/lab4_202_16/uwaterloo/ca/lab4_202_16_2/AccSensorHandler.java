package lab4_202_16.uwaterloo.ca.lab4_202_16_2;

/**
 * Created by Nadeem Amin on 2017-03-03.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class AccSensorHandler implements SensorEventListener {
    //Variables for reading, filtering, and saving accelerometer readings
    private static float[][] aReadings = new float[100][3];            //Array for the 100 readings of the accelerometer readings
    private final static float FILTER_CONSTANT = 12f;                  //The filter constant specific to the Samsung Galaxy S6

    //Variables for FSM
    //FSM: Setting up FSM states and signatures
    private enum fsmState{WAIT, RISE_Ax, FALL_Ax, FALL_Bx, RISE_Bx, RISE_Cz, FALL_Cz, FALL_Dz, RISE_Dz, DETERMINED}  //States of determining the movement
    private static fsmState state = fsmState.WAIT;      //Default / Starting state is WAIT

    private enum TYPE{L, R, U, D, X}        //Types of movements: LEFT, RIGHT, UP, DOWN, UNDETERMINED
    private static TYPE type = TYPE.X;      //Default / Starting type is undetermined

    //FSM: Threshold constants for determining state of FSM
    private static final float[] THRES_A = {0.35f, 3.35f, -1.35f};   // Threshold values for RIGHT
    private static final float[] THRES_B = {-0.45f, -2.55f, 4.90f};  // Threshold values for LEFT
    private static final float[] THRES_C = {0.45f, 2.950f, -0.45f};  // Threshold values for UP
    private static final float[] THRES_D = {-0.35f, -2.85f, 2.46f};  // Threshold values for DOWN

    private static float accXMax = -1;  // Maximum and minimum values for the X and Z axis, used in determining the movement
    private static float accXMin =  1;
    private static float accZMax = -1;
    private static float accZMin =  1;

    //FSM: sample counter is implemented here
    private static final int SAMPLE_AMOUNT = 30;        //After 30 samples the FSM resets to the default states
    private static int sampleCounter = SAMPLE_AMOUNT;   //Counter variable that counts down the 30 samples

    private static GameLoopTask LoopTask;


    public AccSensorHandler(GameLoopTask gameLoopTask){
        this.LoopTask = gameLoopTask;
    }   //Creator function for this class, solely requires the reference to the gameLoopTask instance, which is made in the main method

    private static void filteredSensorReadings(float[] currentReading) {

        for(int i = 1; i < 100; i++){   //Cycles out the oldest reading by replacing it with the reading after it, leaving the 99th index open
            aReadings[i - 1][0] = aReadings[i][0];
            aReadings[i - 1][1] = aReadings[i][1];
            aReadings[i - 1][2] = aReadings[i][2];
        }

        aReadings[99][0] += (currentReading[0] - aReadings[99][0]) / FILTER_CONSTANT;       //The reading in the X,Y,Z axis is filtered and entered in the 99th index,
        aReadings[99][1] += (currentReading[1] - aReadings[99][1]) / FILTER_CONSTANT;       //making it the newest reading
        aReadings[99][2] += (currentReading[2] - aReadings[99][2]) / FILTER_CONSTANT;

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private static void fsm(){

        float accX = ( aReadings[99][0] - aReadings[98][0] ) ;      //The change in acceleration in the X direction
        float accZ = ( aReadings[99][2] - aReadings[98][2] ) ;      //The change in acceleration in the Z direction

        if(aReadings[99][0] < accXMin)      accXMin = aReadings[99][0];     //Updates the minimum and maximum values of the X and Z axis values
        if(aReadings[99][0] > accXMax)      accXMax = aReadings[99][0];
        if(aReadings[99][2] < accZMin)      accZMin = aReadings[99][2];
        if(aReadings[99][2] > accZMax)      accZMax = aReadings[99][2];

        sampleCounter--;      //Decrement the counter by 1 to keep track of when 30 samples have been taken

        if(sampleCounter <=0) {
            state = fsmState.WAIT;   //If thirty samples have been taken, reset the FSM
        }

        switch (state){         //Start implementing the FSM

            case WAIT:                              //Case when nothing determinable occurs, or when the FSM has just started or been reset
                sampleCounter = SAMPLE_AMOUNT;      //Resets the FSM counter
                type = TYPE.X;                      //Resets the FSM signature to UNDETERMINED

                accXMax = -1;  accXMin =  1;  accZMax = -1; accZMin =  1;       //Resets all maximum and minimum acceleration values, for both axis

                if(accX > THRES_A[0])
                    state = fsmState.RISE_Ax;       //If it meets the first requirement for going RIGHT, then switch to the first state for RIGHT

                else if(accX < THRES_B[0])
                    state = fsmState.FALL_Bx;       //If it meets the first requirement for going LEFT, then switch to the first state for LEFT

                else if(accZ > THRES_C[0])
                    state = fsmState.RISE_Cz;       //If it meets the first requirement for going UP, then switch to the first state for UP

                else if(accZ < THRES_D[0])
                    state = fsmState.FALL_Dz;       //If it meets the first requirement for going DOWN, then switch to the first state for DOWN

                //If nothing can be concluded then stay in the WAIT state and start from here again the next time the FSM is called

                break;


            case RISE_Ax:                   //Case that determines RIGHT, Occurs when the first condition for being RIGHT has been achieved ( THRES_A[0] )

                if(accX <= 0) {     //If the movement is accelerating positively
                    if (accXMax >= THRES_A[1])       //The second condition for checking if the movement is RIGHT ( accXMax >= THRES_A[1] )
                        state = fsmState.FALL_Ax;   //If the second condition for being a RIGHT movement is met, switch to the second case for checking if its RIGHT
                    else {
                        state = fsmState.DETERMINED;        //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                        //the specifications. Remember, the signature is still type.X, the undetermined state.
                    }
                }
                break;

            case FALL_Ax:       //Case for checking the final condition for the movement being RIGHT

                if(accX  >= 0) {        //If the movement is accelerating negatively
                    if (accXMin <= THRES_A[2]) {    //The final condition for the signature being RIGHT  ( accXMin <= THRES_A[2] )
                        type = TYPE.R;  //If it is determined that this is a RIGHT movement, switch to the DETERMINED state and output RIGHT
                        //   Log.d("FSM : ", "CONCLUSION  " + type.toString());
                    }
                    state = fsmState.DETERMINED;       //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                    //the specifications. Remember, the signature is still type.X, the undetermined state.
                }
                break;


            case FALL_Bx:                   //Case that determines LEFT, occurs when the first condition for left has been met ( THRES_B[0] )
                if(accX >= 0) {     //If the movement is accelerating negatively
                    if(accXMin <= THRES_B[1]) {         //The second condition for the movement being left, ( accXMin <= THRES_B[1] )
                        state = fsmState.RISE_Bx;       //If the second condition for being a LEFT movement is met, switch to the second case for checking if its LEFT
                    }
                    else {
                        state = fsmState.DETERMINED;    //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                        //the specifications. Remember, the signature is still type.X, the undetermined state.
                    }
                }
                break;

            case RISE_Bx:   //Case for checking the final condition for the movement being LEFT

                if(accX  >= 0) {  //If the movement is accelerating positively
                    if (accXMax <= THRES_B[2]) {         //Final condition for the movement being LEFT ( accXMax <= THRES_B[2] )
                        type = TYPE.L;          //If it is determined that this is a LEFT movement, switch to the DETERMINED state and output LEFT
                    }
                }
                state = fsmState.DETERMINED;           //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                //the specifications. Remember, the signature is still type.X, the undetermined state.
                break;


            case RISE_Cz:          //Case that determines UP, occurs when the first condition for being UP has been met ( THRES_C[0] )
                if(accZ <= 0 ) {        //If the movement is accelerating positively
                    if (accZMax >= THRES_C[1]) {     //The second condition for the movement being UP ( accZMax >= THRES_C[1] )
                        state = fsmState.FALL_Cz;       //If the second condition for being a UP movement is met, switch to the second case for checking if its UP
                    }

                    else {
                        state = fsmState.DETERMINED;    //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                        //the specifications. Remember, the signature is still type.X, the undetermined state.
                    }
                }
                break;

            case FALL_Cz:   //Case for checking the final condition for the movement being UP
                if(accZ  <= 0){     //If the movement is accelerating negatively
                    if(accZMin >= THRES_C[2]) {    //Final condition for the movement being UP ( accZMin >= THRES_C[2] )
                        type = TYPE.U;      //If it is determined that this is a UP movement, switch to the DETERMINED state and output UP
                    }
                }
                state = fsmState.DETERMINED;            //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                //the specifications. Remember, the signature is still type.X, the undetermined state.

                break;


            case FALL_Dz:      //Case that determines DOWN, occurs when the first condition for being DOWNN has been met ( THRES_D[0] )

                if(accZ >= 0) {     //If the movement is accelerating negatively
                    if (accZMin <= THRES_D[1]) {       //The second condition for the movement being DOWN (accZMin <= THRES_D[1] )
                        state = fsmState.RISE_Dz;   //If the second condition for being a DOWN movement is met, switch to the second case for checking if its DOWN
                    }
                    else {
                        state = fsmState.DETERMINED;    //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                        //the specifications. Remember, the signature is still type.X, the undetermined state.
                    }
                }
                break;


            case RISE_Dz:   //Case for checking the final condition for the movement being DOWN
                if(accZ  <= 0) {    //If the movement is accelerating positively
                    if (accZMax >= THRES_D[2]) {    //Final condition for the movement being DOWN ( accZMax >= THRES_D[2] )
                        type = TYPE.D;  //If it is determined that this is a DOWN movement, switch to the DETERMINED state and output DOWN
                    }
                    state = fsmState.DETERMINED;          //Otherwise switch to the DETERMINED state and say that there is no conclusion due to a movement that didn't meet
                    //the specifications. Remember, the signature is still type.X, the undetermined state.
                }
                break;

        }


        if(type == TYPE.L) {          //Sets the movement in the GameLoopTask to LEFT, to make the GameBlock go LEFT
            LoopTask.setMovement(GameLoopTask.Movement.LEFT);
        }

        else if(type == TYPE.R) {     //Sets the movement in the GameLoopTask to RIGHT, to make the GameBlock go RIGHT
            LoopTask.setMovement(GameLoopTask.Movement.RIGHT);
        }

        else if(type == TYPE.U) {    //Sets the movement in the GameLoopTask to UP, to make the GameBlock go UP
            LoopTask.setMovement(GameLoopTask.Movement.UP);
        }

        else if(type == TYPE.D) {   //Sets the movement in the GameLoopTask to DOWN, to make the GameBlock go DOWN
            LoopTask.setMovement(GameLoopTask.Movement.DOWN);
        }
        else{
            LoopTask.setMovement(GameLoopTask.Movement.NO_MOVEMENT);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onSensorChanged(SensorEvent event) {        //Is called whenever there is a sensor event, in this case when the phone is moved
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            AccSensorHandler.filteredSensorReadings(event.values);     //Filters the incoming readings
            AccSensorHandler.fsm();
            //Calls the FSM to determine the direction of the movement, and then sets the movement direction in the GameLoopTask method
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}       //Unused but required from the abstract class SensorEventListener

}