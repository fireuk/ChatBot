package com.example.dialoguebot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements DailogueFragment.OnListFragmentInteractionListener{

    private FragmentManager mFtM;
    private FragmentTransaction mFtT;
    private Fragment mFt_Record;
    private final String TAG = "braind_MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查权限
        // 目前SDK依赖录音和写SD卡权限，如获取不到,SDK无法正常使用
        boolean allPermissionsGranted = checkPermissions();
        Log.i(TAG, "onCreate allPermissionsGranted : " + allPermissionsGranted);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary, null));
                //window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        mFtM = getSupportFragmentManager();
        mFtT = mFtM.beginTransaction();
        mFt_Record = new DailogueFragment();
        mFtT.add(R.id.fragmentContainer, mFt_Record);
        mFtT.commit();
    }


    @Override
    public void onListFragmentInteraction(DialogueContent.DialogueItem item) {

    }

    /** 启动时申请权限 */
    private static final int PERMISSIONS_REQUEST_PERMISSIONS = 1;
    /**
     * 启动的时候检查录音、写SD卡权限，如果没有就退出
     */
    private boolean checkPermissions() {
        boolean allPermissionsGranted = true;

        List<String> permissionsToRequest = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGranted = false;
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGranted = false;
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            allPermissionsGranted = false;
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]),
                    PERMISSIONS_REQUEST_PERMISSIONS);
        }

        return allPermissionsGranted;
    }
}
