package com.example.manhuntapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Start page activity which has a single field for user to input their name
 *
 * @author Devin Grant-Miles
 */

public class StartActivity extends AppCompatActivity {

    //extra message for sending inputted user name
    public static final String EXTRA_MESSAGE = "com.example.manhuntapp2.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void viewUserList(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        EditText editText = (EditText) findViewById(R.id.nameField);
        String userName = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, userName);
        startActivity(intent);
    }
}
