package nusem.oz.uploadmanager.Model;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by oz.nusem on 21/9/14.
 */
public class UploadRequest {

    private static final String REQUEST_URL_EMPTY = "The Request URL cannot be null or with empty value";
    private static final String REQUEST_URL_SHOULD_START_WITH_HTTP = "Request URL should be http/https protocol url";
    private static final String FILES_CANNOT_BE_EMPTY = "Files to upload cannot be empty";

    private final String mUploadManagerCallbackBlocksClassName;
    private NotificationSettings mNotificationSettings;
    private final Context mContext;
    private final String mUrl;
    private final ArrayList<FileUploadData> mFilesToUpload;
    private final ArrayList<NameValue> mHeaders;
    private final ArrayList<NameValue> mParameters;
    private ArrayList<String> mAdditionalParams;
    private String method = "POST";


    public UploadRequest(final Context context, final String serverUrl,
                         ArrayList<FileUploadData> fileUploadDatas,
                         ArrayList<NameValue> headers,
                         ArrayList<NameValue> parameters,
                         NotificationSettings notificationSettings,
                         Class c) {

        mContext = context;
        mNotificationSettings = notificationSettings;
        mUrl = serverUrl;
        mFilesToUpload = fileUploadDatas;
        mHeaders = headers;
        mParameters = parameters;

        if (c != null) {
            mUploadManagerCallbackBlocksClassName = c.getCanonicalName();
        } else {
            mUploadManagerCallbackBlocksClassName = null;
        }
    }

    public void setNotificationSettings(final int iconResourceID, final String title, final String message,
                                        final String completed, final String error, final boolean autoClearOnSuccess) {
        mNotificationSettings = new NotificationSettings(iconResourceID, title, message, completed, error,
                                                          autoClearOnSuccess);
    }

    public String getUploadManagerCallbackBlocksClassName() {
        return mUploadManagerCallbackBlocksClassName;
    }

    public void checkIfValidAndThrow() throws IllegalArgumentException, MalformedURLException {
        if (mUrl == null || "".equals(mUrl)) {
            throw new IllegalArgumentException(REQUEST_URL_EMPTY);
        }

        if (!mUrl.startsWith("http")) {
            throw new IllegalArgumentException(REQUEST_URL_SHOULD_START_WITH_HTTP);
        }

        if (mFilesToUpload.isEmpty()) {
            throw new IllegalArgumentException(FILES_CANNOT_BE_EMPTY);
        }

        //Throw MalformedURLException if not valid
        new URL(mUrl);
    }


    public String getServerUrl() {
        return mUrl;
    }

    public ArrayList<FileUploadData> getmFilesToUpload() {
        return mFilesToUpload;
    }

    public ArrayList<NameValue> getmHeaders() {
        return mHeaders;
    }

    public ArrayList<NameValue> getmParameters() {
        return mParameters;
    }

    public NotificationSettings getNotificationSettings() {
        return mNotificationSettings;
    }

    public Context getContext() {
        return mContext;
    }

    public ArrayList<String> getAdditionalParams() {
        return mAdditionalParams;
    }

    public String getMethod() {
        return method;
    }

}
