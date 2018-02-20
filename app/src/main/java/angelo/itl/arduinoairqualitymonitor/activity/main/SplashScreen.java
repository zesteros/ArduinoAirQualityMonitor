package angelo.itl.arduinoairqualitymonitor.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import angelo.itl.arduinoairqualitymonitor.R;

public class SplashScreen extends Activity {

    private Animation anim;
    private ImageView image;
    private Thread animationTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        startAnimations();

        SharedPreferences settings = getSharedPreferences("prefs", 0);

        boolean firstRun = settings.getBoolean("firstRun", false);

        if (firstRun == false) {//if running for first time
            //Splash will load for first time
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", true);
            editor.commit();

            animationTimer = new Thread() {
                public void run() {
                    try {
                        sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            };
            animationTimer.start();
        } else {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void startAnimations() {
        anim = AnimationUtils.loadAnimation(this, R.anim.rotation);
        image = (ImageView) findViewById(R.id.logo);
        image.startAnimation(anim);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}