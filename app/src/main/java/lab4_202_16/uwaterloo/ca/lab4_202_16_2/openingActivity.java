package lab4_202_16.uwaterloo.ca.lab4_202_16_2;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class openingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_opening);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(this);
        button.findViewById(R.id.button);
        button.setBackgroundColor(Color.DKGRAY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }

    public void sendMessage (View view){
        Intent startNewActivity = new Intent(this, MainActivity.class);
        startActivity(startNewActivity);
    }

}
