package com.kuldeep.camerakitplugin;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.camerakitApp.R;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraView;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    DrawerLayout drawerLayout;
    ListView leftDrawer;

    ActionBarDrawerToggle drawerToggle;

    ViewGroup parent;
    CameraView camera;

    private ArrayAdapter drawerAdapter;

    private int cameraMethod = CameraKit.Constants.METHOD_STANDARD;
    private boolean cropOutput = false;
    private String actionbarname="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout)findViewById(R.id.activityMain);
        leftDrawer = (ListView)findViewById(R.id.leftDrawer);
        parent = (ViewGroup) findViewById(R.id.contentFrame);
        camera = (CameraView)findViewById(R.id.camera);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            actionbarname= null;
        } else {
            actionbarname= extras.getString("actionbarname");
        }
        setupDrawerAndToolbar();
        setTitle(actionbarname);
        camera.setMethod(cameraMethod);
        camera.setCropOutput(cropOutput);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    protected void onPause() {
        camera.stop();
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerAndToolbar() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

        };

        drawerLayout.setDrawerListener(drawerToggle);



        final List<CameraSetting> options = new ArrayList<>();
        options.add(captureMethodSetting);
        options.add(cropSetting);

        drawerAdapter = new ArrayAdapter<CameraSetting>(this, R.layout.drawer_list_item, R.id.text1, options) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(R.id.text1);
                text1.setTextColor(Color.WHITE);

                final TextView text2 = view.findViewById(R.id.text2);
                text2.setTextColor(Color.WHITE);

                text1.setText(options.get(position).getTitle());
                text2.setText(options.get(position).getValue());

                return view;
            }
        };

        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                options.get(i).toggle();
                drawerAdapter.notifyDataSetChanged();
            }
        });
        leftDrawer.setAdapter(drawerAdapter);
    }

    private static abstract class CameraSetting {

        abstract String getTitle();
        abstract String getValue();
        abstract void toggle();

    }

    private CameraSetting captureMethodSetting = new CameraSetting() {
        @Override
        String getTitle() {
            return "ckMethod";
        }

        @Override
        String getValue() {
            switch (cameraMethod) {
                case CameraKit.Constants.METHOD_STANDARD: {
                    return "standard";
                }

                case CameraKit.Constants.METHOD_STILL: {
                    return "still";
                }

                default: return null;
            }
        }

        @Override
        void toggle() {
            if (cameraMethod == CameraKit.Constants.METHOD_STANDARD) {
                cameraMethod = CameraKit.Constants.METHOD_STILL;
            } else {
                cameraMethod = CameraKit.Constants.METHOD_STANDARD;
            }

            camera.setMethod(cameraMethod);
        }
    };

    private CameraSetting cropSetting = new CameraSetting() {
        @Override
        String getTitle() {
            return "ckCropOutput";
        }

        @Override
        String getValue() {
            if (cropOutput) {
                return "true";
            } else {
                return "false";
            }
        }

        @Override
        void toggle() {
            cropOutput = !cropOutput;
            camera.setCropOutput(cropOutput);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagepath="";
        if(requestCode == 99){
            if(data == null){

            }else {
                imagepath = data.getStringExtra("imagepath");
                Intent i = new Intent();
                i.putExtra("imagepath", imagepath);
                setResult(99, i);
                finish();
            }
        }
    }
}
