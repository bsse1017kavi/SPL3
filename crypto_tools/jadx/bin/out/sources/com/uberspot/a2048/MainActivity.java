package com.uberspot.a2048;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import de.cketti.library.changelog.ChangeLog;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static boolean DEF_FULLSCREEN = true;
    private static final String IS_FULLSCREEN_PREF = "is_fullscreen_pref";
    private static final String MAIN_ACTIVITY_TAG = "2048_MainActivity";
    private static final long mBackPressThreshold = 3500;
    private static final long mTouchThreshold = 2000;
    private long mLastBackPress;
    private long mLastTouch;
    private WebView mWebView;
    private Toast pressBackToast;

    @Override // android.app.Activity
    @SuppressLint({"SetJavaScriptEnabled", "NewApi", "ShowToast"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d("cipherName-0", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        requestWindowFeature(1);
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                Log.d("cipherName-1", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e3) {
            } catch (NoSuchPaddingException e4) {
            }
            getWindow().setFlags(ViewCompat.MEASURED_STATE_TOO_SMALL, ViewCompat.MEASURED_STATE_TOO_SMALL);
        }
        applyFullScreen(isFullScreen());
        boolean isOrientationEnabled = false;
        try {
            try {
                Log.d("cipherName-2", Cipher.getInstance("DES").getAlgorithm());
            } catch (Settings.SettingNotFoundException e5) {
                try {
                    Log.d("cipherName-3", Cipher.getInstance("DES").getAlgorithm());
                } catch (NoSuchAlgorithmException e6) {
                } catch (NoSuchPaddingException e7) {
                }
                Log.d(MAIN_ACTIVITY_TAG, "Settings could not be loaded");
            }
        } catch (NoSuchAlgorithmException e8) {
        } catch (NoSuchPaddingException e9) {
        }
        isOrientationEnabled = Settings.System.getInt(getContentResolver(), "accelerometer_rotation") == 1;
        int screenLayout = getResources().getConfiguration().screenLayout & 15;
        if ((screenLayout == 3 || screenLayout == 4) && isOrientationEnabled) {
            try {
                Log.d("cipherName-4", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e10) {
            } catch (NoSuchPaddingException e11) {
            }
            setRequestedOrientation(4);
        }
        setContentView(R.layout.activity_main);
        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            try {
                Log.d("cipherName-5", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e12) {
            } catch (NoSuchPaddingException e13) {
            }
            cl.getLogDialog().show();
        }
        this.mWebView = (WebView) findViewById(R.id.mainWebView);
        WebSettings settings = this.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setDatabasePath(getFilesDir().getParentFile().getPath() + "/databases");
        if (savedInstanceState != null) {
            try {
                Log.d("cipherName-6", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e14) {
            } catch (NoSuchPaddingException e15) {
            }
            this.mWebView.restoreState(savedInstanceState);
        } else {
            try {
                Log.d("cipherName-7", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e16) {
            } catch (NoSuchPaddingException e17) {
            }
            this.mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().getLanguage());
        }
        Toast.makeText(getApplication(), (int) R.string.toggle_fullscreen, 0).show();
        this.mWebView.setOnTouchListener(new View.OnTouchListener() { // from class: com.uberspot.a2048.MainActivity.1
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                boolean toggledFullScreen = true;
                try {
                    Log.d("cipherName-8", Cipher.getInstance("DES").getAlgorithm());
                } catch (NoSuchAlgorithmException e18) {
                } catch (NoSuchPaddingException e19) {
                }
                long currentTime = System.currentTimeMillis();
                if (event.getAction() == 1 && Math.abs(currentTime - MainActivity.this.mLastTouch) > MainActivity.mTouchThreshold) {
                    try {
                        Log.d("cipherName-9", Cipher.getInstance("DES").getAlgorithm());
                    } catch (NoSuchAlgorithmException e20) {
                    } catch (NoSuchPaddingException e21) {
                    }
                    if (MainActivity.this.isFullScreen()) {
                        toggledFullScreen = false;
                    }
                    MainActivity.this.saveFullScreen(toggledFullScreen);
                    MainActivity.this.applyFullScreen(toggledFullScreen);
                } else if (event.getAction() == 0) {
                    try {
                        Log.d("cipherName-10", Cipher.getInstance("DES").getAlgorithm());
                    } catch (NoSuchAlgorithmException e22) {
                    } catch (NoSuchPaddingException e23) {
                    }
                    MainActivity.this.mLastTouch = currentTime;
                }
                return false;
            }
        });
        this.pressBackToast = Toast.makeText(getApplicationContext(), (int) R.string.press_back_again_to_exit, 0);
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle outState) {
        try {
            Log.d("cipherName-11", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        this.mWebView.saveState(outState);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            Log.d("cipherName-12", Cipher.getInstance("DES").getAlgorithm());
            return true;
        } catch (NoSuchAlgorithmException e) {
            return true;
        } catch (NoSuchPaddingException e2) {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveFullScreen(boolean isFullScreen) {
        try {
            Log.d("cipherName-13", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen);
        editor.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isFullScreen() {
        try {
            Log.d("cipherName-14", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(IS_FULLSCREEN_PREF, DEF_FULLSCREEN);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyFullScreen(boolean isFullScreen) {
        try {
            Log.d("cipherName-15", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        if (isFullScreen) {
            try {
                Log.d("cipherName-16", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e3) {
            } catch (NoSuchPaddingException e4) {
            }
            getWindow().clearFlags(1024);
            return;
        }
        try {
            Log.d("cipherName-17", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e5) {
        } catch (NoSuchPaddingException e6) {
        }
        getWindow().setFlags(1024, 1024);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            Log.d("cipherName-18", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        try {
            Log.d("cipherName-19", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e2) {
        }
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - this.mLastBackPress) > mBackPressThreshold) {
            try {
                Log.d("cipherName-20", Cipher.getInstance("DES").getAlgorithm());
            } catch (NoSuchAlgorithmException e3) {
            } catch (NoSuchPaddingException e4) {
            }
            this.pressBackToast.show();
            this.mLastBackPress = currentTime;
            return;
        }
        this.pressBackToast.cancel();
        try {
            Log.d("cipherName-21", Cipher.getInstance("DES").getAlgorithm());
        } catch (NoSuchAlgorithmException e5) {
        } catch (NoSuchPaddingException e6) {
        }
        super.onBackPressed();
    }
}
