package lab4_202_16.uwaterloo.ca.lab4_202_16_2;

/**
 * Created by Nadeem Amin on 2017-03-03.
 */
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Random;

public class GameBlock extends GameBlockTemplate{
    private final float xMin = 90;          private final float xMax = 1150;        //Variables that dictate the maximum and minimum x,y values that the GameBlock can be set to
    private final float yMin = 90;          private final float yMax = 1150;
    private final float SLOT_ISOLATION = 355;
    public boolean oneTime = true;                      //Variable used to control how many times setDirection is called

    public GameLoopTask.Movement movement = GameLoopTask.Movement.NO_MOVEMENT;     //Variable that is used for tracking the direction of movement, will be used for lab 4

    private Random random = new Random();               //Used in randomly creating a number for the new block that is spawned
    public TextView numberText;                         //Used for displaying the new number that is spawned
    public int blockNumber;                             //Used for tracking the number of the block

    private final float IMAGE_SCALE = 1.5f;                                         //The scaling that must be done to the image that is being used
    private float myCoordX;        private float targetCoordX;                      //Variables that are used for tracking the current and desired x coordinate value
    private float myCoordY;        private float targetCoordY;                      //Variables that are used for tracking the current and desired y coordinate value

    public int booleanX;           public int booleanY;         //Variables for keeping track of the blocks coordinates in the GLT gameblockgrid

    private float velocity = 0;   private final float ACC = 2;          //Variable for tracking the velocity (pixels/tick) and the acceleration (pixels/tick/tick)

    public boolean merging = false;                                     //Used for determining how many blocks to move in setDir based on how many blocks are currently merging
    public  boolean movementCompleted = false;                      //Boolean variable that tracks if the occurring movement has completed or not
    public  boolean entrance = false;                               //Boolean variable that checks if the move function has been used or not (used to resolve a bug)
    private boolean destroyMe = false;              //Boolean that signals that this block is to be destroyed after the next movement

    public static boolean makeBlock = false;        //Boolean that is given to GLT which dictates when the next gameeBlock should be created

    public static boolean gameWinDetected = false;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public GameBlock(Context context, RelativeLayout relativeLayout, float coordX, float coordY) {
        super(context, relativeLayout);
        myContext = context;
        this.setX(coordX);  this.myCoordX = coordX;       //Sets the image to the specified x coordinate, and updates the current x position
        this.setY(coordY);  this.myCoordY = coordY;       //Sets the image to the specified y coordinate, and updates the current y position
        this.setScaleX(IMAGE_SCALE);                      //Scales the image horizontally by the preset scaling factor
        this.setScaleY(IMAGE_SCALE);                      //Scales the image vertically by the preset scaling factor
        this.setImageResource(R.mipmap.ic_launcher);      //Sets the image resource as an android (this is just for fun and visual appeal)

        this.setBooleanXY( (int) coordX, (int) coordY);

        blockNumber = (random.nextInt(2)) + 2 ;    if(blockNumber == 3) blockNumber = 4;
        numberText = new TextView(myContext);
        numberText.setText(String.format("    " + blockNumber));
        numberText.setTextSize(21f);
        numberText.setTextColor(Color.BLACK);
        numberText.setX(coordX + 12);  numberText.setY(coordY + 120);

        relativeLayout.addView(this);                               //Adds the block to the layout
        relativeLayout.addView(numberText);
        numberText.bringToFront();
    }

    private void setBooleanXY(int X, int Y){
        if(X == 20)
            this.booleanX = 0;
        else if( X == 378 )
            this.booleanX = 1;
        else if( X == 735 )
            this.booleanX = 2;
        else
            this.booleanX = 3;

        if(Y == 20)
            this.booleanY =0;
        else if ( Y == 378 )
            this.booleanY = 1;
        else if ( Y == 735 )
            this.booleanY = 2;
        else
            this.booleanY = 3;


    }       //Takes an incoming blocks pixel coordinates and converts them into Boolean coordinate for the GLT grid

    public void setBlockDirection (GameLoopTask.Movement mov){
        this.movement = mov;
    }
    //Function that sets the new direction of the GameBlock to the private field that tracks the movement of the GameBlock

    public void destroy(){
        relativeLayout.removeView(this);                //Removes the block from the layout
        relativeLayout.removeView(this.numberText);         //Removes the block's number TextView off the layout
        GameLoopTask.gameBlocks.remove(this);                       //Deletes the block from the LinkedList
        //This allows for the Java Garbage Collection Manager to then take this and delete it from memory
    }       //Function that is used to destroy the block

    private void clearAll(){
        for(int i = 0; i < GameLoopTask.gameBlocks.size() ; i++) {          //Used to reset the merging boolean for all the gameblocks so that
            GameLoopTask.gameBlocks.get(i).merging = false;                 //the setDirection method works accurately
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void move() {
        if(oneTime) {
            setDestination();    //Sets the target coordinates; Only occurs once, which is controleld by the boolen oneTime
            oneTime = false;
        }

        makeBlock = false;              //Sets makeBlock to be false, and will only be set to true once the last moving block has finished its movement

        switch (movement){
            case UP:
                movementCompleted = false;    //If it is the first time entering this movement, set to false to avoid bug where the motion does not go to completion

                if(!entrance) {
                    setX(myCoordX += 70);       //This is to resolve a bug where the creator sets the image 70 pixels above where it should be
                    numberText.setX(myCoordX + 12);
                    entrance = true;            //States that the first movement has been recorded and started
                }
                entrance = true;

                velocity += ACC;            //Increases the velocity by the acceleration constant to mimic linear motion

                if( (myCoordY - velocity) > targetCoordY) {             //If the increase by velocity does not exceed the target coordinate move the image to the new coordinate
                    setY(myCoordY -= velocity);                         //Sets the new coordinate with the added velocity, and updates the current y coordinate
                    numberText.setY(myCoordY + 120);
                }
                else{                           //This occurs if the increase by velocity exceeds the target coordinate, meaning that it would go offscreen. Instead it wraps up the movement
                    velocity = 0;               //Resets the velocity for the next coming movement
                    setY(targetCoordY);         //Sets the y values to the desired coordinate at the end
                    myCoordY = targetCoordY;    //Updates the current y coordinate position
                    numberText.setY(myCoordY + 120);

                    this.movementCompleted = true;

                    if(GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size()-1).booleanX == this.booleanX && GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size() - 1 ).booleanY == this.booleanY)
                        makeBlock = true;           //If this is the last gameblock then set makeBlock to be true so that the next block can be created

                    if(destroyMe)       //If this block is to be destroyed, then destroy it after you've finished the movement to make the animation smooth
                        destroy();

                }

                break;

            case DOWN:
                movementCompleted = false;    //If it is the first time entering this movement, set to false to avoid bug where the motion does not go to completion

               if(!entrance) {
                    setX(myCoordX += 70);       //This is to resolve a bug where the creator sets the image 70 pixels above where it should be
                    numberText.setX(myCoordX + 12);
                    entrance = true;            //States that the first movement has been recorded and started
               }
                entrance = true;

                velocity += ACC;                //Increases the velocity by the acceleration constant to mimic linear motion

                if( (myCoordY + velocity) < targetCoordY) {             //If the increase by velocity does not exceed the target coordinate move the image to the new coordinate
                    setY(myCoordY += velocity);                         //Sets the new coordinate with the added velocity, and updates the current y coordinate
                    numberText.setY(myCoordY + 120);
                }
                else{                           //This occurs if the increase by velocity exceeds the target coordinate, meaning that it would go offscreen. Instead it wraps up the movement
                    velocity = 0;               //Resets the velocity for the next coming movement
                    setY(targetCoordY);         //Sets the y values to the desired coordinate at the end
                    myCoordY = targetCoordY;    //Updates the current y coordinate position
                    numberText.setY(myCoordY + 120);

                    this.movementCompleted = true;
                    if(GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size()-1).booleanX == this.booleanX && GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size() - 1 ).booleanY == this.booleanY)
                        makeBlock = true;           //If this is the last gameblock then set makeBlock to be true so that the next block can be created

                    if(destroyMe)               //If this block is to be destroyed, then destroy it after you've finished the movement to make the animation smooth
                        destroy();
                }

                break;

            case LEFT:
                movementCompleted = false;                              //If it is the first time entering this movement, set to false to avoid bug where the motion does not go to completion

               if(!entrance) {
                    setY(myCoordY += 70);   //This is to resolve a bug where the creator sets the image 70 pixels to the right of where it should be
                    numberText.setY(myCoordY + 120);
               }
               entrance = true;        //States that the first movement has been recorded and started

                velocity += ACC;            //Increases the velocity by the acceleration constant to mimic linear motion

                if( (myCoordX - velocity) > targetCoordX){      //If the increase by velocity does not exceed the target coordinate move the image to the new coordinate
                    setX(myCoordX-=velocity);                   //Sets the new coordinate with the added velocity, and updates the current x coordinate
                    numberText.setX(myCoordX + 12);
                }
                else {                          //This occurs if the increase by velocity exceeds the target coordinate, meaning that it would go offscreen. Instead it wraps up the movement
                    velocity = 0 ;              //Resets the velocity for the next coming movement
                    setX(targetCoordX);         //Sets the x values to the desired coordinate at the end
                    myCoordX = targetCoordX;    //Updates the current x coordinate position
                    numberText.setX(myCoordX + 12);

                    this.movementCompleted = true;

                    if(GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size()-1).booleanX == this.booleanX && GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size() - 1 ).booleanY == this.booleanY)
                        makeBlock = true;           //If this is the last gameblock then set makeBlock to be true so that the next block can be created

                    if(destroyMe)           //If this block is to be destroyed, then destroy it after you've finished the movement to make the animation smooth
                        destroy();

                }
                break;

            case RIGHT:
                movementCompleted = false;                              //If it is the first time entering this movement, set to false to avoid bug where the motion does not go to completion

                if(!entrance) {
                    setY(myCoordY += 70);           //This is to resolve a bug where the creator sets the image 70 pixels to the right of where it should be
                    numberText.setY(myCoordY + 120);
                }
                entrance = true;                //States that the first movement has been recorded and started

                velocity += ACC;            //Increases the velocity by the acceleration constant to mimic linear motion

                if( (myCoordX + velocity) < targetCoordX) {       //If the increase by velocity does not exceed the target coordinate move the image to the new coordinate
                    setX(myCoordX += velocity);                 //Sets the new coordinate with the added velocity, and updates the current x coordinate
                    numberText.setX(myCoordX + 12);
                }
                else {                          //This occurs if the increase by velocity exceeds the target coordinate, meaning that it would go offscreen. Instead it wraps up the movement
                    velocity = 0 ;              //Resets the velocity for the next coming movement
                    setX(targetCoordX);         //Sets the x values to the desired coordinate at the end
                    myCoordX = targetCoordX;    //Updates the current x coordinate position
                    numberText.setX(myCoordX + 12);

                    this.movementCompleted = true;

                    if(GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size()-1).booleanX == this.booleanX && GameLoopTask.gameBlocks.get(GameLoopTask.gameBlocks.size() - 1 ).booleanY == this.booleanY)
                        makeBlock = true;           //If this is the last gameblock then set makeBlock to be true so that the next block can be created

                    if(destroyMe)               //If this block is to be destroyed, then destroy it after you've finished the movement to make the animation smooth
                        destroy();
                }

                break;

        }

    }

    public int getBlockNumberFromGrid(float[] check){
        for (int i = 0 ; i < GameLoopTask.gameBlocks.size() ; i++){
            if(GameLoopTask.gameBlocks.get(i).booleanX == check[0] && GameLoopTask.gameBlocks.get(i).booleanY == check[1]){
                return GameLoopTask.gameBlocks.get(i).blockNumber;
            }
        }
        return 0;
    }   //Iterates through all the GameBlocks in the GLT LinkedList
                                                            // and matches the boolean coordinates; If matched, returns its block number for comparison
    public GameBlock getBlockFromGrid (float[] check){
        for (int i = 0 ; i < GameLoopTask.gameBlocks.size() ; i++){
            if(GameLoopTask.gameBlocks.get(i).booleanX == check[0] && GameLoopTask.gameBlocks.get(i).booleanY == check[1]){
                return GameLoopTask.gameBlocks.get(i);
            }
        }
        return null;
    }   //Iterates through all the GameBlocks in the GLT LinkedList and matches the boolean coordinates;
                                                            // If matched, returns the block for comparison
    public void setNewText(){
        MainActivity.score += blockNumber*2;                      //Updates the score
        blockNumber += blockNumber;                               //Updates the block's number
        numberText.setText(String.format("    " + blockNumber));    //Updates the block's TextView
        if(blockNumber == 256)
            gameWinDetected = true;                                     //States that the game has been won (after reaching 256)
    }

    public  void resetGrid(){
        for(int xAxis = 0; xAxis < 4 ; xAxis ++){               //Iterates through the boolean grid and sets everything to false
            for(int yAxis = 0; yAxis < 4 ; yAxis++){
                GameLoopTask.gameBoardGrid[xAxis][yAxis] = false;
            }
        }

        for(int i = 0; i < GameLoopTask.gameBlocks.size() ; i ++) {         //Iterates through the GameBlocks in the LinkedList and sets the occupied coordinates to true
            GameLoopTask.gameBoardGrid[GameLoopTask.gameBlocks.get(i).booleanX][GameLoopTask.gameBlocks.get(i).booleanY] = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setDestination() {
        int blockCount = 0; int slotCount = 0;     //Variables used in determining how many blocks to move

        float[] check = new float[2];       //Used to store the boolean coordinates of the grid points that wish to be checked

        boolean foundFirst = false;         //Boolean variable that states that the first gameblock  in the direction of motion to this gameblock has been found

        int count = 0;          //Used in tracking how many gameblocks are merging


        if(GameLoopTask.gameBlocks.getFirst() == this) {            //If its the first gameblock coming in for setDirection then clear the merging boolean for all the booleans
            clearAll();
        }

        if(GameLoopTask.gameBlocks.getLast() == this)       //If its the last gameblock then reset the grid for added assurance that the game is working correctly
            resetGrid();

        switch (this.movement) {

            case UP:
                this.targetCoordX = this.myCoordX;          //X coordinate will not change so can just set that
                check[0] = this.booleanX;                       //Update the check to store the x coordinate as the current booleanX coordinate of this object

                slotCount = this.booleanY;              //Updates how many slots are between the object and the end goal (the first row)

                if(slotCount == 0 ) {                   //If there are no slots then you are at the end goal
                    this.targetCoordY = yMin;               //Update the targetCoord for completion
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;       //Update the GLT GameBlock grid
                    this.booleanY = 0;                                                      //Set the new boolean coordinate
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;        //Update the GLT Gameblock grid
                    break;      //Break out of the switch
                }

                for(int i = this.booleanY - 1; i >= 0; i--){
                    check[1] = i;
                    if(getBlockFromGrid(check) != null){            //Check ahead of the block, in the same coloumn if there are more gameBlocks, if so make note of it
                        blockCount++;
                    }
                }

                if(blockCount == 0){
                    this.targetCoordY = yMin;                                                //If there aren't any gameBlocks then the movement should go the whole way
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;       //Update the targetcoordinate, the GLT Gameblock grid and the boolean coordinate
                    this.booleanY = 0;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }

                    //Check what blocks are already merging, and subtract them from the block count as they will create an empty spot whnen they merge
                for(int i = this.booleanY - 1; i >= 0; i--){
                    check[1] = i;
                    if(getBlockFromGrid(check) != null) {
                        if (getBlockFromGrid(check).merging ) {
                            count++;
                            if(count%2 == 0)
                                blockCount--;
                        }
                    }
                }

                if(blockCount != 0){        //If block count doesnt equal zero that means that there is a block between the block and its destination
                    //Now trying to find that first block, and checking if it has the same block number, if so will trigger the boolean that will merge them
                    for(int i = slotCount - 1; i >=0 ; i--){
                        check[1] = i ;

                        if(foundFirst)          //found the first block so just get outta here
                            break;
                        if(getBlockFromGrid(check) != null) {           //If it isn't null then it has to be the first block
                            foundFirst = true;

                            if (getBlockNumberFromGrid(check) == this.blockNumber) {        //Check if the block numbers are the same
                                if (!getBlockFromGrid(check).merging) {                      //Check if that block isn't merging with another block
                                    blockCount--;                                           //go up one more block than originally anticipated
                                    this.destroyMe = true;
                                    this.merging = true;        //destroy the upward moving block
                                    getBlockFromGrid(check).merging = true;     //State the upper block is also merging
                                    getBlockFromGrid(check).setNewText();          //make the block that is above merge with this block and update that blocks number
                                }
                            }
                        }
                    }
                }

                //Now set up the displacement stuff

                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;           //Says the block isnt at its original coordinate and is now moving elsewhere
                if(blockCount != 0) {
                    this.targetCoordY = yMin + (blockCount * SLOT_ISOLATION);     //Takes the minimum boundary and adds slots too the target to move it down by the number
                    this.booleanY = blockCount;                                     // of blocks impeding it and then updates the Y boolean coordinate
                }
                else {
                    this.targetCoordY = yMin;          //Else update the grid to the minimum values (shouldn't occur, set as a net)
                    this.booleanY = 0;
                }
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;            //Updates the GLT Grid with the new coordinates

                break;

            case DOWN:
                this.targetCoordX = this.myCoordX;          //The x target will remain the same so that can be updated  immediately
                check[0] = this.booleanX;                   //The check's x value can then be updated immediately as well

                slotCount = 3 - this.booleanY;              //Finds how many slots are in between the object and the bottom row

                if(slotCount == 0) {                    //If the slot count is 0 then it updates the GLT Grid, the target coord, and the boolean coordinates accordingly
                    this.targetCoordY = yMax;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanY = 3;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }

                //look ahead and count blocks

                for(int i = this.booleanY + 1; i < 4 ; i++){            //Wont ever go out of bounds cause of the last check breaking out of the switch
                    check[1] = i;                                           //Checks all the spaces infront and if there are then add that to the bc
                    if(getBlockFromGrid(check) != null){
                        blockCount++;
                    }
                }

                if(blockCount == 0){                            //If no blocks in between the object and the target, it will go straight tothe end and the
                    this.targetCoordY = yMax;                           //parameters would be updated accordingly
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanY = 3;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }
                //subtract those that are being merged already

                for(int i = this.booleanY + 1; i < 4; i++){
                    check[1] = i;
                    if(getBlockFromGrid(check) != null) {
                        if (getBlockFromGrid(check).merging) {  //If the block infront is merging then delete one cause theres going to be an empty spot
                            count++;
                            if(count%2 == 0)
                                blockCount--;
                        }
                    }
                }

                //check the first block if you should merge it or nah
                if(blockCount != 0){

                    for ( int i = this.booleanY + 1 ; i < 4 ; i++) {
                        check[1] = i;

                        if (foundFirst)
                            break;

                        if (getBlockFromGrid(check) != null) {
                            foundFirst = true;

                            if (getBlockNumberFromGrid(check) == this.blockNumber) {
                                if (!getBlockFromGrid(check).merging) {
                                    blockCount--;
                                    this.destroyMe = true;
                                    this.merging = true;
                                    getBlockFromGrid(check).merging = true;
                                    getBlockFromGrid(check).setNewText();

                                }

                            }
                        }
                    }
                }

                //Now displacement stuff
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;

                if(blockCount != 0) {
                    this.targetCoordY = yMax - (blockCount * SLOT_ISOLATION);
                    this.booleanY = 3 - blockCount;
                }
                else {
                    this.targetCoordY = yMax;
                    this.booleanY = 3;
                }
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;

                break;

            case LEFT:
                this.targetCoordY = this.myCoordY;
                check[1] = this.booleanY;

                slotCount = this.booleanX;
                if(slotCount == 0 ) {
                    this.targetCoordX = xMin;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanX = 0;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;

                    break;
                }

                for(int i = this.booleanX - 1; i >= 0; i--){
                    check[0] = i;
                    if(getBlockFromGrid(check) != null){            //If there is something there then
                        blockCount++;
                    }
                }

                if(blockCount == 0){
                    this.targetCoordX = xMin;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanX = 0;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }


                for(int i = this.booleanX - 1; i >= 0; i--){             //If a block is already merging then move up one more space cause theres going to be an empty space from
                    check[0] = i;                                   //the merge
                    if(getBlockFromGrid(check) != null) {
                        if (getBlockFromGrid(check).merging ) {
                            count++;
                            if(count%2 == 0)
                                blockCount--;
                        }
                    }
                }

                if(blockCount != 0){        //If block count doesnt equal zero that means that there is a block between the block and its destination
                    //Now trying to find that first block, and checking if it has the same block number, if so will trigger the boolean that will merge them
                    for(int i = slotCount - 1; i >=0 ; i--){
                        check[0] = i ;

                        if(foundFirst)          //found the first block so just get outta here
                            break;
                        if(getBlockFromGrid(check) != null) {
                            foundFirst = true;

                            if (getBlockNumberFromGrid(check) == this.blockNumber) {
                                if (!getBlockFromGrid(check).merging) {                      //if that block isnt merging with another block
                                    blockCount--;                                           //go up one more block than originally anticipated
                                    this.destroyMe = true;
                                    this.merging = true;        //destroy the upward moving block
                                    getBlockFromGrid(check).merging = true;
                                    getBlockFromGrid(check).setNewText();          //make the block that is above merge with this block and update that blocks number
                                }
                            }
                        }
                    }
                }

                //Now set up the displacement stuff

                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                if(blockCount != 0) {
                    this.targetCoordX = xMin + (blockCount * SLOT_ISOLATION);     //Starting with
                    this.booleanX = blockCount;
                }
                else {
                    this.targetCoordX = xMin;
                    this.booleanX = 0;
                }
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;


                break;

            case RIGHT:

                this.targetCoordY = this.myCoordY;
                check[1] = this.booleanY;

                slotCount = 3 - this.booleanX;

                if(slotCount == 0) {
                    this.targetCoordX = xMax;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanX = 3;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }

                //look ahead and count blocks

                for(int i = this.booleanX + 1; i < 4 ; i++){            //Wont ever go out of bounds cause of the last check breaking out of the switch
                    check[0] = i;                                           //Checks all the spaces infront and if there are then add that to the bc
                    if(getBlockFromGrid(check) != null){
                        blockCount++;
                    }
                }

                if(blockCount == 0){
                    this.targetCoordX = xMax;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;
                    this.booleanX = 3;
                    GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;
                    break;
                }
                //subtract those that are being merged already

                for(int i = this.booleanX + 1; i < 4; i++){
                    check[0] = i;
                    if(getBlockFromGrid(check) != null) {
                        if (getBlockFromGrid(check).merging) {                            //If the block infront is merging then delete one cause theres going to be an empty spot
                            count++;
                            if(count%2 ==0)
                                blockCount--;
                        }
                    }
                }

                //check the first block if you should merge it or nah
                if(blockCount != 0){

                    for ( int i = this.booleanX + 1 ; i < 4 ; i++) {
                        check[0] = i;

                        if (foundFirst)
                            break;

                        if (getBlockFromGrid(check) != null) {
                            foundFirst = true;

                            if (getBlockNumberFromGrid(check) == this.blockNumber) {
                                if (!getBlockFromGrid(check).merging) {
                                    blockCount--;
                                    this.destroyMe = true;
                                    this.merging = true;
                                    getBlockFromGrid(check).merging = true;
                                    getBlockFromGrid(check).setNewText();

                                }

                            }
                        }
                    }
                }

                //Now displacement stuff
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = false;

                if(blockCount != 0) {
                    this.targetCoordX = xMax - (blockCount * SLOT_ISOLATION);
                    this.booleanX = 3 - blockCount;
                }
                else {
                    this.targetCoordX = xMax;
                    this.booleanX = 3;
                }
                GameLoopTask.gameBoardGrid[this.booleanX][this.booleanY] = true;

                break;

            case NO_MOVEMENT:
                //Nothing is to happen in this case, hence nothing does happen
                break;
        }

    }
}