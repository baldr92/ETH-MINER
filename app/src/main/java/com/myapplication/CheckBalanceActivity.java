package com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class CheckBalanceActivity extends AppCompatActivity {

    String mFileName="data.txt";
    TextView Balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_balance);
        Balance = (TextView)findViewById(R.id.TextBalance);
        read();
    }

    public void BackFromBalance(View view)
    {
        finish();
    }

    private void read()
    {
        Balance.setText(getIntent().getStringExtra("balance")+" ETH");
    }
}
