package nusem.oz.uploadmanager.Builders;


import android.content.Context;

import nusem.oz.uploadmanager.Model.NotificationSettings;
import nusem.oz.uploadmanager.R;


/**
 * Created by oznusem on 1/21/16.
 */
public class NotificationSettingsBuilder {

    private int iconResourceID;
    private  String title;
    private  String message;
    private  String completed;
    private  String error;
    private  boolean autoClearOnSuccess;

    public NotificationSettingsBuilder(Context context) {

        iconResourceID = android.R.drawable.ic_menu_upload;
        title = context.getString(R.string.notification_file_upload);
        message = context.getString(R.string.notification_upload_in_progress);
        completed = context.getString(R.string.notification_upload_complete);
        error = context.getString(R.string.notification_error_during_upload);

        autoClearOnSuccess = false;
    }

    public NotificationSettingsBuilder setIconResourceID(int iconResourceID) {
        this.iconResourceID = iconResourceID;
        return this;
    }

    public NotificationSettingsBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NotificationSettingsBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public NotificationSettingsBuilder setCompleted(String completed) {
        this.completed = completed;
        return this;
    }

    public NotificationSettingsBuilder setError(String error) {
        this.error = error;
        return this;
    }

    public NotificationSettingsBuilder setAutoClearOnSuccess(boolean autoClearOnSuccess) {
        this.autoClearOnSuccess = autoClearOnSuccess;
        return this;
    }

    public NotificationSettings create() {
        return new NotificationSettings(iconResourceID,title,message,completed,error,autoClearOnSuccess);
    }

}
