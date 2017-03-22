package lab4_202_16.uwaterloo.ca.lab4_202_16_2;

/**
 * Created by Nadeem Amin on 2017-03-03.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;

public class GameLoopTask extends TimerTask{
    private Activity        myActivity;                     //Private variables for storing the activity, the context and the layout
    private Context         myContext;
    private RelativeLayout  relativeLayout;

    public static LinkedList<GameBlock> gameBlocks = new LinkedList<GameBlock>();
    public enum Movement{UP, DOWN, LEFT, RIGHT, NO_MOVEMENT}        //Enum created for communicating the direction of motion
    public Movement movement = Movement.NO_MOVEMENT;               //Variable created for tracking the direction of motion

    public static boolean[][] gameBoardGrid = new boolean[4][4];    //Boolean grid which keeps track of what places are available for spawning in
    private int x0 = 0;     private int y0 = 0;                     //Variables used in conjuction with the grid to spawn blocks
    Random random = new Random();                                   //Randomizes the process of making a block
    private boolean creationPossible = false;                       //States that the block is possible to create and doesnt trigger the end game flag

    public static boolean resetTriggered = false;                   //If the reset button was hit, this would be true and would state that a new block must be created

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public GameLoopTask(MainActivity mainActivity, Context applicationContext, RelativeLayout rL) { //Creator function for this method
        this.myActivity = mainActivity;             //Stores the activity,  context and layout of the main method in its private variables
        this.myContext = applicationContext;
        this.relativeLayout = rL;
        createBlock();                      //Creates the first GameBlock randomly
    }       //Creator for class

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void createBlock(){
        int[][] availablePoints = new int[16][2];   int count = 0; int index = 0;       //Variables for keeping track of all the possible places to spawn

        for( int xCounter = 0; xCounter < 4 ; xCounter++) {             //Iterates through the boolean grid to check which spots are open
            for (int yCounter = 0; yCounter < 4; yCounter++) {
                if (!gameBoardGrid[xCounter][yCounter] && xCounter < 4 && yCounter < 4 ) {
                    availablePoints[count][0] = xCounter;           //Stores the available points in an array
                    availablePoints[count][1] = yCounter;
                    count++;                                        //Increases the size of the array for tracking
                    creationPossible = true;                        //Since a place to create the block is available, creation is possible and the
                    break;                                                      //error text should'nt be shown
                }
            }
        }

        if (!creationPossible) {                                        //If creation isn't possible then the error text is displayed
            TextView errorText = new TextView(myContext);
            errorText.setText("GAME OVER");
            errorText.setTextSize(50f);
            errorText.setX(180f);
            errorText.setTextColor(Color.BLACK);
            errorText.setBackgroundColor(Color.WHITE);          //Brought to front and is large to be noticeable
            errorText.setPadding(10,10,10,10);
            relativeLayout.addView(errorText);
            if(resetTriggered)
                relativeLayout.removeView(errorText);
        }
        else {
            index = random.nextInt(count);                  //Randomly choose a point
            this.x0 = availablePoints[index][0];            //Sets up the variables that then update the grid and add the block
            this.y0 = availablePoints[index][1];
            this.onCreateGridUpdate(x0, y0);        //Updates the boolean grid with this new point, and set the pixel displacement
            gameBlocks.add(new GameBlock(this.myContext, relativeLayout, x0, y0));     //Calls the creator function from the GameBlock method to create the new block
        }

        creationPossible = false;   //Sets creationPossible to false for the next iteration
    }

    private void onCreateGridUpdate(int x, int y){
        this.gameBoardGrid[x][y] = true;        //Updates the grid with the new point
        this.x0 = this.fixPixel(x);             //Runs a method which takes the boolean grid coordinates and returns its respective value
        this.y0 = this.fixPixel(y);             //in pixels
    }
    private int fixPixel(int n){
        switch(n){      //Maps boolean grid coordinates to points, determined through trial and error
            case 0:
                n = 20;
                break;
            case 1:
                n = 378;
                break;
            case 2:
                n = 735;
                break;
            case 3:
                n = 1090;
                break;
        }
        return n;
    }

    public void run() {     //The method required by the abstract nature of the TimerTask method, will be the method that runs periodically
        myActivity.runOnUiThread(   //Function that will allow the enclosed statements to be run on the main activity
                new Runnable() {    //The runnable statements
                    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run() {

                        MainActivity.scoreLog.setText(String.format("SCORE: %d", MainActivity.score));      //Updates the score

                        if(resetTriggered){             //If the reset button was hit then it makes a new block
                            createBlock();
                            resetTriggered = false;         //Sets it back to false so that it doesnt keep triggering on each iteration
                        }

                        if(GameBlock.gameWinDetected){
                            TextView winText = new TextView(myContext);       //TextView which shows the error text
                            winText.setText("GAME WON");
                            winText.setTextSize(20f);
                            winText.setX(180f);
                            winText.setY(1450f);
                            winText.setTextColor(Color.BLACK);
                            winText.setBackgroundColor(Color.WHITE);          //Brought to front and is large to be noticeable
                            winText.setPadding(5,5,5,5);
                            relativeLayout.addView(winText);

                        }



                        if(movement != Movement.NO_MOVEMENT) {  //If there is some change in movement other than no movement
                            if (gameBlocks.get(gameBlocks.size() - 1).movementCompleted || !gameBlocks.get(gameBlocks.size() - 1).entrance) {
                                //If the previous movement has been completed or if no movement has been made yet
                                //Checks theses statements for the second last block

                                for( int i = 0 ; i < gameBlocks.size() ; i++) {
                                    gameBlocks.get(i).setBlockDirection(movement); //Sets each block's movement to the new incoming movement
                                    gameBlocks.get(i).oneTime = true;               //Variable used to ensure that setDirection in GB only runs once
                                }


                            }
                        }
                        else{
                            for (int i = 0; i < gameBlocks.size() ; i++) {      //Goes through each block and calls its move function
                                    gameBlocks.get(i).move();
                            }

                            if(GameBlock.makeBlock){            //If the last block has moved, then this will be true and a new block will be spawned
                                createBlock();
                            }

                        }

                        //Note that even if the movement was to be called repeatedly as no new movement has been received, the statements in the GameBlock.move()
                        //function will keep the GameBlock on the screen
                    }
                }
        );

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void setMovement(Movement mov){
        movement = mov;                             //Updates the private field for tracking the movement
    }

}