package com.android.hardcore.crashreport;
import android.os.Bundle;

import com.pay.telcel.main.R;

public class ApplicationCore extends CrashReportingApplication {

    /* (non-Javadoc)
     * @see com.task.killer.exception.CrashReportingApplication#getReportEmail()
     */
    public String getReportEmail() {
        return getString(R.string.report_email);
    }

    /* (non-Javadoc)
     * @see com.task.killer.exception.CrashReportingApplication#getCrashResources()
     */
    public Bundle getCrashResources() {
        final Bundle result = new Bundle();
        result.putString(RES_EMAIL_SUBJECT,getString(R.string.crash_report_email_subject, getAppName() ,getVersionName()));
        result.putString(RES_EMAIL_TEXT, getString(R.string.crash_report_email_text));
        result.putString(RES_DIALOG_TITLE, getString(R.string.crash_report_dialog_title));
        result.putString(RES_DIALOG_TEXT, getString(R.string.crash_report_dialog_text));
        result.putString(RES_BUTTON_REPORT, getString(R.string.crash_report_btn_report));
        result.putString(RES_BUTTON_CANCEL, getString(R.string.crash_report_btn_exit));
        // Hide restart button
        result.putString(RES_BUTTON_RESTART, "");
        result.putInt(RES_DIALOG_ICON, R.drawable.ic_launcher);
        return result;
    }

}
