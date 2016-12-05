package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gauravbhor.securechat.R;


public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showandscan);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
    switch(item.getItemId()){
        case R.id.show_key:
            Intent intent = new Intent(FriendListActivity.this, ReaderActivity.class);
            startActivity(intent);
            break;
        case R.id.scan_key:
            Intent intent1 = new Intent(FriendListActivity.this, GeneratorActivity.class);
            startActivity(intent1);
            break;
        default:
            break;

    }
        return false;
    }
}
