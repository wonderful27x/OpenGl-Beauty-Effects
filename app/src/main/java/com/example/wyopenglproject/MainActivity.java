package com.example.wyopenglproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * openGL项目，将摄像头数据渲染到FBO缓存上，然后在做各种滤镜叠加效果，最后再渲染到屏幕上
 * 使用FBO离屏绘制可以直接使用Texture2D。
 */
public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,MyRecordButton.OnRecordListener{

    private WonderfulGLSurfaceView glSurface;
    private RadioGroup speedRadioGroup;
    private MyRecordButton myRecordButton;
    private CheckBox bigEye;
    private CheckBox stick;
    private CheckBox beauty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        List<String> permissionList = permissionCheck();
        if (permissionList.isEmpty()){
            //TODO
        }else {
            permissionRequest(permissionList,1);
        }

        glSurface = findViewById(R.id.glSurface);
        speedRadioGroup = findViewById(R.id.group_record_speed);
        speedRadioGroup.setOnCheckedChangeListener(this);
        myRecordButton = findViewById(R.id.btn_record);
        myRecordButton.setOnRecordListener(this);
        speedRadioGroup.check(R.id.rbtn_record_speed_normal);

        bigEye = findViewById(R.id.chk_bigeye);
        stick = findViewById(R.id.chk_stick);
        beauty = findViewById(R.id.chk_beauty);

        bigEye.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                glSurface.enableBigEye(isChecked);
            }
        });
        stick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                glSurface.enableStick(isChecked,-1);
            }
        });
        beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                glSurface.enableBeauty(isChecked);
            }
        });
    }

    //判断是否授权所有权限
    private List<String> permissionCheck(){
        List<String> permissions = new ArrayList<>();
        if (!checkPermission(Manifest.permission.CAMERA)){
            permissions.add(Manifest.permission.CAMERA);
        }
        if (!checkPermission(Manifest.permission.RECORD_AUDIO)){
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return permissions;
    }

    //发起权限申请
    private void permissionRequest(List<String> permissions,int requestCode){
        String[] permissionArray = permissions.toArray(new String[permissions.size()]);
        ActivityCompat.requestPermissions(this,permissionArray,requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            if (grantResults.length >0){
                for (int result:grantResults){
                    if (result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(MainActivity.this,"对不起，您拒绝了权限无法使用此功能！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                //TODO
            }else {
                Toast.makeText(MainActivity.this,"发生未知错误！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //判断是否有权限
    private boolean checkPermission(String permission){
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rbtn_record_speed_extra_slow: //极慢
                glSurface.setSpeedMode(SpeedMode.EXTRA_SLOW);
                break;
            case R.id.rbtn_record_speed_slow:   //慢
                glSurface.setSpeedMode(SpeedMode.SLOW);
                break;
            case R.id.rbtn_record_speed_normal: //正常
                glSurface.setSpeedMode(SpeedMode.NORMAL);
                break;
            case R.id.rbtn_record_speed_fast:   //快
                glSurface.setSpeedMode(SpeedMode.FAST);
                break;
            case R.id.rbtn_record_speed_extra_fast: //极快
                glSurface.setSpeedMode(SpeedMode.EXTRA_FAST);
                break;
        }
    }

    @Override
    public void onStartRecording() {
        glSurface.startRecording();
    }

    @Override
    public void onStopRecording() {
        glSurface.stopRecording();
        Toast.makeText(this,"录制完成！",Toast.LENGTH_SHORT).show();
    }
}
