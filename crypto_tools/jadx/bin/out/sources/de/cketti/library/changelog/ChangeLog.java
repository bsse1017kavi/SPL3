package de.cketti.library.changelog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
public class ChangeLog {
    public static final String DEFAULT_CSS = "h1 { margin-left: 0px; font-size: 1.2em; }\nli { margin-left: 0px; }\nul { padding-left: 2em; }";
    protected static final String LOG_TAG = "ckChangeLog";
    protected static final int NO_VERSION = -1;
    protected static final String VERSION_KEY = "ckChangeLog_last_version_code";
    protected final Context mContext;
    protected final String mCss;
    private int mCurrentVersionCode;
    private String mCurrentVersionName;
    private int mLastVersionCode;

    /* loaded from: classes.dex */
    protected interface ChangeLogTag {
        public static final String NAME = "changelog";
    }

    /* loaded from: classes.dex */
    protected interface ChangeTag {
        public static final String NAME = "change";
    }

    /* loaded from: classes.dex */
    protected interface ReleaseTag {
        public static final String ATTRIBUTE_VERSION = "version";
        public static final String ATTRIBUTE_VERSION_CODE = "versioncode";
        public static final String NAME = "release";
    }

    public ChangeLog(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context), DEFAULT_CSS);
    }

    public ChangeLog(Context context, String css) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context), css);
    }

    public ChangeLog(Context context, SharedPreferences preferences, String css) {
        this.mContext = context;
        this.mCss = css;
        this.mLastVersionCode = preferences.getInt(VERSION_KEY, -1);
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.mCurrentVersionCode = packageInfo.versionCode;
            this.mCurrentVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            this.mCurrentVersionCode = -1;
            Log.e(LOG_TAG, "Could not get version information from manifest!", e);
        }
    }

    public int getLastVersionCode() {
        return this.mLastVersionCode;
    }

    public int getCurrentVersionCode() {
        return this.mCurrentVersionCode;
    }

    public String getCurrentVersionName() {
        return this.mCurrentVersionName;
    }

    public boolean isFirstRun() {
        return this.mLastVersionCode < this.mCurrentVersionCode;
    }

    public boolean isFirstRunEver() {
        return this.mLastVersionCode == -1;
    }

    public void skipLogDialog() {
        updateVersionInPreferences();
    }

    public AlertDialog getLogDialog() {
        return getDialog(isFirstRunEver());
    }

    public AlertDialog getFullLogDialog() {
        return getDialog(true);
    }

    protected AlertDialog getDialog(boolean full) {
        WebView wv = new WebView(this.mContext);
        wv.loadDataWithBaseURL(null, getLog(full), "text/html", "UTF-8", null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle(this.mContext.getResources().getString(full ? R.string.changelog_full_title : R.string.changelog_title)).setView(wv).setCancelable(false).setPositiveButton(this.mContext.getResources().getString(R.string.changelog_ok_button), new DialogInterface.OnClickListener() { // from class: de.cketti.library.changelog.ChangeLog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                ChangeLog.this.updateVersionInPreferences();
            }
        });
        if (!full) {
            builder.setNegativeButton(R.string.changelog_show_full, new DialogInterface.OnClickListener() { // from class: de.cketti.library.changelog.ChangeLog.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                    ChangeLog.this.getFullLogDialog().show();
                }
            });
        }
        return builder.create();
    }

    protected void updateVersionInPreferences() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(VERSION_KEY, this.mCurrentVersionCode);
        editor.commit();
    }

    public String getLog() {
        return getLog(false);
    }

    public String getFullLog() {
        return getLog(true);
    }

    protected String getLog(boolean full) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style type=\"text/css\">");
        sb.append(this.mCss);
        sb.append("</style></head><body>");
        String versionFormat = this.mContext.getResources().getString(R.string.changelog_version_format);
        List<ReleaseItem> changelog = getChangeLog(full);
        for (ReleaseItem release : changelog) {
            sb.append("<h1>");
            sb.append(String.format(versionFormat, release.versionName));
            sb.append("</h1><ul>");
            for (String change : release.changes) {
                sb.append("<li>");
                sb.append(change);
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    public List<ReleaseItem> getChangeLog(boolean full) {
        SparseArray<ReleaseItem> masterChangelog = getMasterChangeLog(full);
        SparseArray<ReleaseItem> changelog = getLocalizedChangeLog(full);
        List<ReleaseItem> mergedChangeLog = new ArrayList<>(masterChangelog.size());
        int len = masterChangelog.size();
        for (int i = 0; i < len; i++) {
            int key = masterChangelog.keyAt(i);
            ReleaseItem release = changelog.get(key, masterChangelog.get(key));
            mergedChangeLog.add(release);
        }
        Collections.sort(mergedChangeLog, getChangeLogComparator());
        return mergedChangeLog;
    }

    protected SparseArray<ReleaseItem> getMasterChangeLog(boolean full) {
        return readChangeLogFromResource(R.xml.changelog_master, full);
    }

    protected SparseArray<ReleaseItem> getLocalizedChangeLog(boolean full) {
        return readChangeLogFromResource(R.xml.changelog, full);
    }

    protected final SparseArray<ReleaseItem> readChangeLogFromResource(int resId, boolean full) {
        XmlResourceParser xml = this.mContext.getResources().getXml(resId);
        try {
            return readChangeLog(xml, full);
        } finally {
            xml.close();
        }
    }

    protected SparseArray<ReleaseItem> readChangeLog(XmlPullParser xml, boolean full) {
        SparseArray<ReleaseItem> result = new SparseArray<>();
        try {
            int eventType = xml.getEventType();
            while (eventType != 1) {
                if (eventType == 2) {
                    if (xml.getName().equals("release") && parseReleaseTag(xml, full, result)) {
                        break;
                    }
                }
                eventType = xml.next();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (XmlPullParserException e2) {
            Log.e(LOG_TAG, e2.getMessage(), e2);
        }
        return result;
    }

    private boolean parseReleaseTag(XmlPullParser xml, boolean full, SparseArray<ReleaseItem> changelog) throws XmlPullParserException, IOException {
        int versionCode;
        String version = xml.getAttributeValue(null, ReleaseTag.ATTRIBUTE_VERSION);
        try {
            String versionCodeStr = xml.getAttributeValue(null, ReleaseTag.ATTRIBUTE_VERSION_CODE);
            versionCode = Integer.parseInt(versionCodeStr);
        } catch (NumberFormatException e) {
            versionCode = -1;
        }
        if (!full && versionCode <= this.mLastVersionCode) {
            return true;
        }
        int eventType = xml.getEventType();
        List<String> changes = new ArrayList<>();
        while (true) {
            if (eventType != 3 || xml.getName().equals(ChangeTag.NAME)) {
                if (eventType == 2 && xml.getName().equals(ChangeTag.NAME)) {
                    xml.next();
                    changes.add(xml.getText());
                }
                eventType = xml.next();
            } else {
                ReleaseItem release = new ReleaseItem(versionCode, version, changes);
                changelog.put(versionCode, release);
                return false;
            }
        }
    }

    protected Comparator<ReleaseItem> getChangeLogComparator() {
        return new Comparator<ReleaseItem>() { // from class: de.cketti.library.changelog.ChangeLog.3
            @Override // java.util.Comparator
            public int compare(ReleaseItem lhs, ReleaseItem rhs) {
                if (lhs.versionCode < rhs.versionCode) {
                    return 1;
                }
                if (lhs.versionCode > rhs.versionCode) {
                    return -1;
                }
                return 0;
            }
        };
    }

    /* loaded from: classes.dex */
    public static class ReleaseItem {
        public final List<String> changes;
        public final int versionCode;
        public final String versionName;

        ReleaseItem(int versionCode, String versionName, List<String> changes) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.changes = changes;
        }
    }
}
