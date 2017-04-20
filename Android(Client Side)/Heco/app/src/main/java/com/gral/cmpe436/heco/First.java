package com.gral.cmpe436.heco;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class First extends AppCompatActivity {
    public static boolean isKuro;
    public static int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }
    // for host device
    public void gotoHost(View view) {
        isKuro=true;
        port=44444;
        startActivity(new Intent(this, Game.class));

    }
    // for client device
    public void gotoClient(View view) {
        isKuro=false;
        port=55555;
        startActivity(new Intent(this, Game.class));
    }


}
