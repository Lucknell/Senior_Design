package seniordesign.lucknell.com.seniordesign;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Scroller;
import android.widget.TextView;

public class credits extends Activity {
    public int counter =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        counter =0;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.credits);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        final TextView creds = findViewById(R.id.credits);
        final Button exit = findViewById(R.id.Exitbutton);
        View view = getCurrentFocus();
        view.animate().translationY(view.getHeight()).start();
        view.animate();
        creds.setTextColor(Color.YELLOW);
        creds.setVisibility(View.INVISIBLE);
        exit.setVisibility(View.INVISIBLE);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Animation translatebu= AnimationUtils.loadAnimation(this, R.anim.animationfile);
        translatebu.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(counter ==0)
                    creds.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                creds.setVisibility(View.INVISIBLE);
                if(counter ==0)
                    counter = 1;
                switch (counter)
                {
                    case 1:
                        creds.setText("\nTeam Members" +
                                "\n\nEnrique Espinet \nLucknell Madestin  Eric Bacchus \nItalo Periano  Mike Holmes\n");
                        creds.setVisibility(View.INVISIBLE);
                        creds.startAnimation(translatebu);
                        creds.setVisibility(View.VISIBLE);
                        exit.setVisibility(View.VISIBLE);
                        counter =2;
                        break;
                    case 2:
                        creds.setText("Programmers\n\nLucknell Madestin\nEnrique Espinet\n\n Circuit Board Design" +
                                "\n Great Scott On YouTube ");
                        creds.setVisibility(View.INVISIBLE);
                        creds.clearAnimation();
                        creds.startAnimation(translatebu);
                        creds.setVisibility(View.VISIBLE);
                        exit.setVisibility(View.VISIBLE);
                        counter = 3;
                        break;
                    case 3:
                        creds.setText("We will miss all of you\n\n\n\n\n\n Yes even you Eric...");
                        creds.setVisibility(View.INVISIBLE);
                        creds.clearAnimation();
                        creds.startAnimation(translatebu);
                        creds.setVisibility(View.VISIBLE);
                        counter=4;

                    case 5:
                        finish();
                        break;
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        creds.startAnimation(translatebu);



        creds.setText("Credits\n\n\n\nMentor\n\nDr.Osama Mohammed");



}
}
