package ex.dev.sample.pos.control;

import android.os.Bundle;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ex.dev.sample.pos.control.cash.CashActivity;
import ex.dev.sample.pos.control.display.SecondDisplayControlActivity;
import ex.dev.sample.pos.control.vid.VidAllowListActivity;

/**
 * MainActivity
 * Entry point of the sample application.
 * Provides navigation buttons to:
 * - CashActivity
 * - SecondDisplayActivity
 * - VidActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigate to CashActivity
        findViewById(R.id.btn_cash_drawer).setOnClickListener(
                v -> startActivity(new Intent(this, CashActivity.class))
        );

        // Navigate to SecondDisplayActivity
        findViewById(R.id.btn_second_display).setOnClickListener(
                v -> startActivity(new Intent(this, SecondDisplayControlActivity.class))
        );

        // Navigate to VidActivity
        findViewById(R.id.btn_vid).setOnClickListener(
                v -> startActivity(new Intent(this, VidAllowListActivity.class))
        );
    }
}