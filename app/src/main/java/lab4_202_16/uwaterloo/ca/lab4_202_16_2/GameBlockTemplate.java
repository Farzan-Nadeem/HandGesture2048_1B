package lab4_202_16.uwaterloo.ca.lab4_202_16_2;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Nadeem Amin on 2017-03-07.
 */

public abstract class GameBlockTemplate extends ImageView{
    public Context myContext;   public RelativeLayout relativeLayout;   //Fields for keeping the incoming items (Used in constructor

    public GameBlockTemplate(Context context, RelativeLayout relativeLayout ) {
        super(context);                             //Takes the context from the super class (MainActivity)
        this.relativeLayout = relativeLayout;       //Updates its RelativeLayout field (for code expansion)
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public abstract void move();

    public abstract void setDestination();

    //States that all functions extending this must have a set destination method and must have a move method

}
