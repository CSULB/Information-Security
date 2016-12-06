package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.adapters.SampleFragmentPagerAdapter;
import com.gauravbhor.securechat.fragments.FriendListFragment;
import com.gauravbhor.securechat.fragments.GroupListFragment;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.services.GroupMessageService;
import com.gauravbhor.securechat.services.MessageService;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* Reference for implementation of ViewPage https://guides.codepath.com/android/Sliding-Tabs-with-PagerSlidingTabStrip
* Used https://github.com/astuetz/PagerSlidingTabStrip for ViewPager
* */
public class TabbedActivity extends SuperActivity {

    private static final int REQUEST_CODE = 99;
    private FriendListFragment friendsFragment;
    private GroupListFragment groupsFragment;
    private int activeTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showandscan);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeTab == 0) {
                    // Add Friend
                    IntentIntegrator integrator = new IntentIntegrator(TabbedActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                    integrator.setPrompt("Scan Friend's Publick Key");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(false);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.initiateScan();
                } else {
                    // Create group
                    createGroup();
                }
            }
        });

        startService(new Intent(this, MessageService.class));
        startService(new Intent(this, GroupMessageService.class));

        friendsFragment = new FriendListFragment();
        groupsFragment = new GroupListFragment();

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(friendsFragment);
        fragmentList.add(groupsFragment);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);

        tabsStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                TabbedActivity.this.activeTab = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void createGroup() {
        startActivityForResult(new Intent(this, CreateGroupActivity.class), REQUEST_CODE);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_key:
                Intent intent1 = new Intent(TabbedActivity.this, GeneratorActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            groupsFragment.refresh();
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                String publicKeyID = result.getContents();
                final String[] info = publicKeyID.split(StaticMembers.DELIMITER);
                JSONObject json = new JSONObject();
                try {
                    json.put("sender_id", String.valueOf(user.getId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RetroBuilder.buildOn(ChatServer.class).getUser(json, info[1]).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            final User user = response.body();
                            user.setPublicKey(info[0]);
                            friendsFragment.addUser(user);
                        } else {
                            Toast.makeText(TabbedActivity.this, "Error adding friend. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(TabbedActivity.this, "Error adding friend. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
