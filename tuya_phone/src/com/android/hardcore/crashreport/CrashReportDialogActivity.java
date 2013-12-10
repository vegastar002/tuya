package com.android.hardcore.crashreport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:theme="@android:style/Theme.Dialog" and
 * android:launchMode="singleInstance" in your AndroidManifest to work properly.
 *
 * @author Kevin Gaudin
 *
 */
public class CrashReportDialogActivity extends Activity {

    /**
     * Default left title icon.
     */
    private static final int CRASH_DIALOG_LEFT_ICON = android.R.drawable.ic_dialog_alert;
    private String mReportFileName = null;
    private String mReportEmail = null;
    private Bundle mCrashResources;


    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReportFileName = getIntent().getStringExtra(ErrorReporter.EXTRA_REPORT_FILE_NAME);
        mReportEmail = getIntent().getStringExtra(ErrorReporter.EXTRA_REPORT_EMAIL);
        if(TextUtils.isEmpty(mReportFileName) || TextUtils.isEmpty(mReportEmail)) {
            finish();
        }
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        final CrashReportingApplication application =
                (CrashReportingApplication) getApplication();
        mCrashResources = application.getCrashResources();

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

        final TextView text = new TextView(this);

        text.setText(mCrashResources
                .getString(CrashReportingApplication.RES_DIALOG_TEXT));
        scroll.addView(text, LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);


        final LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        buttons.setPadding(buttons.getPaddingLeft(), 10, buttons
                .getPaddingRight(), buttons.getPaddingBottom());

        final Button yes = new Button(this);
        yes.setText(mCrashResources.getString(CrashReportingApplication.RES_BUTTON_REPORT));
        yes.setOnClickListener(new View.OnClickListener() {


			public void onClick(final View v) {
                // Start email to send report
                sendEmail();
                exit();
            }

        });
        buttons.addView(yes, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        final Button no = new Button(this);
        no.setText(mCrashResources.getString(CrashReportingApplication.RES_BUTTON_CANCEL));
        no.setOnClickListener(new View.OnClickListener() {


			public void onClick(final View v) {
                final File file = new File(mReportFileName);
                if(file.exists()) {
                    file.delete();
                }
                exit();
            }

        });
        buttons.addView(no, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));

        final String restartButtonText = mCrashResources.getString(CrashReportingApplication.RES_BUTTON_RESTART);
        if(null != restartButtonText && restartButtonText.length() > 0) {
            final Button restart = new Button(this);
            restart.setText(restartButtonText);
            restart.setOnClickListener(new View.OnClickListener() {


				public void onClick(final View v) {
                    application.onRestart();
                    exit();
                }

            });
            buttons.addView(restart, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        }

        root.addView(buttons, new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        setContentView(root);

        setTitle(mCrashResources
                .getString(CrashReportingApplication.RES_DIALOG_TITLE));

        final int resLeftIcon = mCrashResources.getInt(CrashReportingApplication.RES_DIALOG_ICON);
        if (resLeftIcon != 0) {
            getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    resLeftIcon);
        } else {
            getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    CRASH_DIALOG_LEFT_ICON);
        }
    }

    public static String readFile(final File file){
        final StringBuilder builder = new StringBuilder();
        if(null != file && file.exists()){
            try {
                final FileReader fileReader = new FileReader(file);
                final BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    builder.append(line+"\n");
                }
                bufferedReader.close();
                fileReader.close();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            } finally{
                System.gc();
            }
        }
        return builder.toString();
    }


    private void sendEmail() {
        try {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            final String[] tos = new String[] { mReportEmail };
            intent.putExtra(Intent.EXTRA_EMAIL, tos);
            intent.putExtra(Intent.EXTRA_SUBJECT, mCrashResources.getString(CrashReportingApplication.RES_EMAIL_SUBJECT));
            final StringBuilder builder = new StringBuilder();
            builder.append(getAppInfo());
            builder.append("\n\n");
            final String crashInfo = readFile(new File(mReportFileName));
            builder.append(crashInfo);
            builder.append("\n\n");
            builder.append(mCrashResources.getString(CrashReportingApplication.RES_EMAIL_TEXT));
            builder.append("\n\n");
            intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
            intent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://" + mReportFileName));
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally{
            System.gc();
        }
    }

    private final String PACKAGE_NAME = "Package name : ";
    private final String VERSION_NAME = "Version name : ";
    private final String VERSION_CODE = "Version code : ";

    private String getAppInfo() {
        final PackageManager pm = getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
        } catch (final NameNotFoundException e) {
            e.printStackTrace();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(PACKAGE_NAME).append(getPackageName()).append("\n");
        if (pi != null) {
            sb.append(VERSION_NAME).append(pi.versionName).append("\n");
            sb.append(VERSION_CODE).append(pi.versionCode).append("\n");
        }
        return sb.toString();
    }

    private void exit() {

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
