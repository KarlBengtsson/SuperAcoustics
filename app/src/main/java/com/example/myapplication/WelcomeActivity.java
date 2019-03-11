package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button newButton, loadButton;
    private int READ_REQUEST_CODE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        newButton = (Button) findViewById(R.id.newButton);
        loadButton = (Button) findViewById(R.id.loadButton);

        Button.OnClickListener newListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Intent intent;
                        intent = new Intent(WelcomeActivity.this,FileName.class);
                        startActivity(intent);
                    }
                };
        newButton.setOnClickListener(newListener);


        Button.OnClickListener loadListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Intent intent = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        }
                        startActivityForResult(intent, READ_REQUEST_CODE);
                        
                    }
                };
        loadButton.setOnClickListener(loadListener);
    }
}
