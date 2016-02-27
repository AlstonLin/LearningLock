package io.alstonlin.learninglock;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class LockScreenActivity extends Activity implements OnLockStatusChangedListener {

    private Button btnUnlock;
    private Button btnEdit;
    private LockScreenUtil mLockscreenUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD

        );

        setContentView(R.layout.activity_lockscreen);

        mLockscreenUtil = new LockScreenUtil();

        btnEdit = (Button) findViewById(R.id.editPass);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LockScreenActivity.this, SetPasswordActivity.class);
                startActivity(i);
            }
        });

        btnUnlock = (Button) findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockScreen();
            }
        });

        if (getIntent() != null && getIntent().hasExtra("kill") && getIntent().getExtras().getInt("kill") == 1) {
            enableKeyguard();
            unlockScreen();
        } else {
            try {
                disableKeyguard();
                lockScreen();
                startService(new Intent(this, LockScreenService.class));
                StateListener phoneStateListener = new StateListener();
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            } catch (Exception e) {
            }
        }
    }

    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockScreen();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    };

    /**
     * Disables back button
     */
    @Override
    public void onBackPressed() {
        return;
    }

    /**
     * Key Listener
     * @param keyCode The code for the key pressed
     * @param event The Key press event
     * @return If the event was handled
     */
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_POWER
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_CAMERA
                || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            return false;
        }
        return event.getKeyCode() == KeyEvent.KEYCODE_HOME;
    }

    public void lockScreen() {
        mLockscreenUtil.lock(LockScreenActivity.this);
    }

    public void unlockScreen() {
        mLockscreenUtil.unlock();
    }

    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked) {
            unlockDevice();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unlockScreen();
    }

    @SuppressWarnings("deprecation")
    private void disableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.disableKeyguard();
    }

    @SuppressWarnings("deprecation")
    private void enableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.reenableKeyguard();
    }

    /**
     * Finishes Activity to unlock device
     */
    private void unlockDevice() {
        finish();
    }

}