package com.alpha.mymemories;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton Camera;
    private BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        Camera = findViewById(R.id.btnCamera);

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent CameraIntent = new Intent(MainActivity.this, CameraBTN.class);
                startActivity(CameraIntent);
            }
        });
    }
}