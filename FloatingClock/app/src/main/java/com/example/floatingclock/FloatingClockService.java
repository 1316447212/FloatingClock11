package com.example.floatingclock;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class FloatingClockService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private TextView clockText;
    private WindowManager.LayoutParams params;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    @Override
    public void onCreate() { super.onCreate(); windowManager = (WindowManager) getSystemService(WINDOW_SERVICE); }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "floating_clock_channel";
            NotificationChannel channel = new NotificationChannel(channelId, "悬浮时钟", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, channelId).setContentTitle("悬浮时钟").setContentText("正在运行中").setSmallIcon(android.R.drawable.ic_dialog_info).build();
            startForeground(1, notification);
        }
        if (floatingView != null) windowManager.removeView(floatingView);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_clock, null);
        clockText = floatingView.findViewById(R.id.clock_text);
        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100; params.y = 200;
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY; private float touchX, touchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: initialX = params.x; initialY = params.y; touchX = event.getRawX(); touchY = event.getRawY(); return true;
                    case MotionEvent.ACTION_MOVE: params.x = initialX + (int)(event.getRawX() - touchX); params.y = initialY + (int)(event.getRawY() - touchY); windowManager.updateViewLayout(floatingView, params); return true;
                    case MotionEvent.ACTION_UP: return true;
                }
                return false;
            }
        });
        windowManager.addView(floatingView, params);
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                clockText.setText(sdf.format(new Date()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timeUpdater);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && timeUpdater != null) handler.removeCallbacks(timeUpdater);
        if (floatingView != null) windowManager.removeView(floatingView);
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}