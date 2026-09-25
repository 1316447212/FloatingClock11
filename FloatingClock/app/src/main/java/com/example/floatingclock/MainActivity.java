package com.example.floatingclock;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(v -> checkOverlayPermissionAndStart());
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingClockService.class));
            Toast.makeText(this, "悬浮时钟已关闭", Toast.LENGTH_SHORT).show();
        });
    }
    private void checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this).setTitle("需要悬浮窗权限").setMessage("请在弹窗中点击「允许显示在其他应用的上层」以开启悬浮窗权限").setPositiveButton("去设置", (dialog, which) -> { Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())); startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST); }).setNegativeButton("取消", null).show();
                return;
            }
        }
        startFloatingService();
    }
    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingClockService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
        Toast.makeText(this, "悬浮时钟已启动", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) startFloatingService();
            else Toast.makeText(this, "权限被拒绝，无法启动悬浮窗", Toast.LENGTH_LONG).show();
        }
    }
}