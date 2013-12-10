package com.android.hardcore.crashreport;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;

import xu.ye.bean.ContactBean;
import xu.ye.service.T9Service;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class CrashReportingApplication extends Application implements OnSharedPreferenceChangeListener {

	protected static final String LOG_TAG = "ACRA";
    private static final String CRASH_FILE = "crash.txt";
    
    public static Context appContext;
    
    public static final String RES_BUTTON_CANCEL = "RES_BUTTON_CANCEL";
    public static final String RES_BUTTON_REPORT = "RES_BUTTON_REPORT";
    public static final String RES_BUTTON_RESTART = "RES_BUTTON_RESTART";
    public static final String RES_DIALOG_ICON = "RES_DIALOG_ICON";
    public static final String RES_DIALOG_TITLE = "RES_DIALOG_TITLE";
    public static final String RES_DIALOG_TEXT = "RES_DIALOG_TEXT";
    public static final String RES_EMAIL_SUBJECT = "RES_EMAIL_SUBJECT";
    public static final String RES_EMAIL_TEXT = "RES_EMAIL_TEXT";
    public static final String PREF_ENABLE_ACRA = "acra.enable";
    
    public static String packName = "";
    public static String pape = "";
    public static ArrayList<Byte> svdatList = new ArrayList<Byte>();
    public static byte [] svtte = new byte[10];

    
    public boolean inner = false;
	private List<ContactBean> contactBeanList;
	
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}
	
	
	
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();
		
        Intent startService = new Intent(CrashReportingApplication.this, T9Service.class);
		startService(startService);
		
		
        final SharedPreferences prefs = getACRASharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        boolean enableAcra = true;
        try {
            enableAcra = prefs.getBoolean(PREF_ENABLE_ACRA,isCrashReportEnableByDefault());
        } catch (final Exception e) {
            e.printStackTrace();
        } finally{
            System.gc();
        }

        if (enableAcra) {
            initAcra();
        }
    }
    
    
    public String retrieveInputStream(HttpEntity httpEntity) {
		int length = (int) httpEntity.getContentLength();
		if (length < 0)
			length = 10000;
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("", e.getMessage());
		} catch (IllegalStateException e) {
			Log.e("", e.getMessage());
		} catch (IOException e) {
			Log.e("", e.getMessage());
		}
		return stringBuffer.toString();
	}
    
    
    public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}
    
    
	public String getAppName() {
		String appName = "";
		final PackageManager packageManager = getPackageManager();
		try {
			final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
			appName = (String) applicationInfo.loadLabel(packageManager);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		} finally {
			System.gc();
		}
		return appName;
	}
	
	public String getVersionName() {
		final PackageManager manager = getPackageManager();
		final String name = getPackageName();
		try {
			final PackageInfo info = manager.getPackageInfo(name, 0);
			return "v" + info.versionName;
		} catch (final NameNotFoundException e) {
			return "";
		} finally {
			System.gc();
		}
	}
	
    private void initAcra() {
        final ErrorReporter errorReporter = ErrorReporter.getInstance();
        String report = "";
        report = getReportEmail();
        errorReporter.setReportEmail(report);
        errorReporter.init(this);
    }

    public abstract String getReportEmail();

    public abstract Bundle getCrashResources();

    public File getCrashReportFile() {
        return new File(Environment.getExternalStorageDirectory(),CRASH_FILE);
    }

    public void onRestart() {

    }

    public void onCrashed(final Thread t, final Throwable e) {

    }

	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
            final String key) {
        if (PREF_ENABLE_ACRA.equals(key)) {
            Boolean enableAcra = true;
            try {
                enableAcra = sharedPreferences.getBoolean(key, isCrashReportEnableByDefault());
            } catch (final Exception e) {
                e.printStackTrace();
            } finally{
                System.gc();
            }
            if (enableAcra) {
                initAcra();
            } else {
                ErrorReporter.getInstance().disable();
            }
        }
    }

    public void setCrashReportEnable(final boolean enable){
        final SharedPreferences prefs = getACRASharedPreferences();
        final Editor editor = prefs.edit();
        editor.putBoolean(PREF_ENABLE_ACRA, enable);
        editor.commit();
    }

    public SharedPreferences getACRASharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public boolean isCrashReportEnableByDefault(){
        return true;
    }

    public boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
