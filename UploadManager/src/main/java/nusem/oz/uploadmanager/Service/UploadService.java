package nusem.oz.uploadmanager.Service;


import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nusem.oz.uploadmanager.Broadcast.BroadcastLogic;
import nusem.oz.uploadmanager.Model.FileUploadData;
import nusem.oz.uploadmanager.Model.NameValue;
import nusem.oz.uploadmanager.Model.NotificationSettings;
import nusem.oz.uploadmanager.Model.UploadRequest;
import nusem.oz.uploadmanager.Network.URLConnectionUpload;
import nusem.oz.uploadmanager.Notification.UploadNotificationManager;
import nusem.oz.uploadmanager.UploadManagerCallbackBlocks;

/**
 * Created by oz.nusem on 1/22/16.
 */
public class UploadService extends Service implements UploadServiceListener {

    private static final String TAG = UploadService.class.getName();

    private static final int KEEP_ALIVE_TIME = 30;
    private static final int MAXIMUM_POOL_SIZE = 3;
    private static final int CORE_POOL_SIZE = 3;
    private static final int QUEUE_INITIAL_CAPACITY = 10;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int RESPONSES_HTTP_OK_START = 200;
    private static final int RESPONSES_HTTP_OK_END = 299;
    public static final String UPLOAD_ID = "id";

    private static class Params {
        private static final String PARAM_NOTIFICATION_CONFIG = "notificationSettings";
        private static final String PARAM_ID = "id";
        private static final String PARAM_URL = "url";
        private static final String PARAM_METHOD = "method";
        private static final String PARAM_FILES = "files";
        private static final String PARAM_REQUEST_HEADERS = "requestHeaders";
        private static final String PARAM_REQUEST_PARAMETERS = "requestParameters";
        private static final String PARAM_CALLBACK_CLASSNAME_STRING = "callbackClassNameString";
    }

    public class Actions {
        private static final String ACTION_UPLOAD = "com.oznusem.upload_action";
        private static final String ACTION_CANCEL = "com.oznusem.cancel_action";
        public static final String BROADCAST_ACTION = "com.oznusem.uploadmanager.status";
    }

    private NotificationCompat.Builder mNotificationBuilder;
    private SparseArray<NotificationSettings> mNotificationSettingsMap;
    private SparseArray mCancelledIds;
    private PowerManager.WakeLock wakeLock;
    private ExecutorService mExecutorService;

    public static void startUpload(final UploadRequest task){

        final Intent intent = new Intent(task.getContext(), UploadService.class);
        intent.setAction(Actions.ACTION_UPLOAD);
        intent.putExtra(Params.PARAM_NOTIFICATION_CONFIG, task.getNotificationSettings());
        intent.putExtra(Params.PARAM_ID, task.getNotificationSettings().getNotificationId());
        intent.putExtra(Params.PARAM_URL, task.getServerUrl());
        intent.putExtra(Params.PARAM_METHOD, task.getMethod());
        intent.putParcelableArrayListExtra(Params.PARAM_FILES, task.getmFilesToUpload());
        intent.putParcelableArrayListExtra(Params.PARAM_REQUEST_HEADERS, task.getmHeaders());
        intent.putParcelableArrayListExtra(Params.PARAM_REQUEST_PARAMETERS, task.getmParameters());
        intent.putExtra(Params.PARAM_CALLBACK_CLASSNAME_STRING, task.getUploadManagerCallbackBlocksClassName());
        intent.putExtra(BroadcastLogic.PARAM_ADDITIONAL_DATA, task.getAdditionalParams());
        task.getContext().startService(intent);
    }

    public static void cancelUpload(Context context,int uploadId) {
        Log.i(TAG,"cancelUpload, id = " + uploadId);
        final Intent intent = new Intent(context, UploadService.class);
        intent.setAction(Actions.ACTION_CANCEL);
        intent.putExtra(UPLOAD_ID, uploadId);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<>(QUEUE_INITIAL_CAPACITY);
        mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mWorkQueue);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        mNotificationBuilder = new  NotificationCompat.Builder(this);
        mNotificationSettingsMap = new SparseArray<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {
            final String action = intent.getAction();
            if (Actions.ACTION_UPLOAD.equals(action)) {
                mExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        startUpLoadAction(intent);
                    }
                });
            }
            else if (Actions.ACTION_CANCEL.equals(action)){
                mExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        addUploadIdToCancelList(intent.getIntExtra(UPLOAD_ID, 0));
                    }
                });
            }
        }
        return START_NOT_STICKY;
    }

    private void addUploadIdToCancelList(int uploadId){
        if (mCancelledIds == null){
            mCancelledIds = new SparseArray();
        }
        mCancelledIds.append(uploadId, null);
    }

    public void startUpLoadAction(Intent intent) {

        final NotificationSettings notificationSettings = intent.getParcelableExtra(Params.PARAM_NOTIFICATION_CONFIG);
        final int uploadId = intent.getIntExtra(Params.PARAM_ID,0);
        final String url = intent.getStringExtra(Params.PARAM_URL);
        final String method = intent.getStringExtra(Params.PARAM_METHOD);
        final ArrayList<FileUploadData> files = intent.getParcelableArrayListExtra(Params.PARAM_FILES);
        final ArrayList<NameValue> headers = intent.getParcelableArrayListExtra(Params.PARAM_REQUEST_HEADERS);
        final ArrayList<NameValue> parameters = intent.getParcelableArrayListExtra(Params.PARAM_REQUEST_PARAMETERS);
        final ArrayList<String> additionalData = intent.getStringArrayListExtra(BroadcastLogic.PARAM_ADDITIONAL_DATA);
        final String callbackClassName = intent.getStringExtra(Params.PARAM_CALLBACK_CLASSNAME_STRING);

        wakeLock.acquire();

        mNotificationSettingsMap.put(uploadId,notificationSettings);

        UploadNotificationManager.createNotification(this, mNotificationBuilder, notificationSettings);
        try {
            startUpload(uploadId, url, method, files, headers, parameters, additionalData, callbackClassName);
        }  finally {
            wakeLock.release();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startUpload(final int uploadId, final String url, final String method,
                             final ArrayList<FileUploadData> filesToUpload,
                             final ArrayList<NameValue> requestHeaders,
                             final ArrayList<NameValue> requestParameters,
                             ArrayList<String> additionalData, String callbackClassName) {

        UploadManagerCallbackBlocks callBackBlocks = null;
        try {
            if (callbackClassName != null) {
                Class c = Class.forName(callbackClassName);
                callBackBlocks = (UploadManagerCallbackBlocks) c.newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Log.e(TAG, "startUpload - " +  e.getMessage());
        }

        boolean doubleTry = true;
        if (additionalData != null && additionalData.size() > 1) {
            doubleTry = Boolean.valueOf(additionalData.get(1));
        }
        try {
            URLConnectionUpload.upload(uploadId, url, method, filesToUpload, requestHeaders, requestParameters,
                    additionalData, callBackBlocks, this);

        } catch (IOException e) {
            if (doubleTry) {
                try {
                    Log.d(TAG, "startUpload - entering double try");
                    URLConnectionUpload.upload(uploadId, url, method, filesToUpload, requestHeaders,
                            requestParameters, additionalData, callBackBlocks, this);
                } catch (IOException e1) {
                    onError(uploadId, e, additionalData, callBackBlocks);
                }
            } else {
                onError(uploadId, e, additionalData, callBackBlocks);
            }
        }
    }

    @Override
    public synchronized boolean checkIfCanceledAndRemove(int uploadId){

        if (mCancelledIds != null && ( (int) mCancelledIds.get(uploadId,-1)) > 0 )  {
            Log.i(TAG, "checkIfCanceledAndRemove  upload id = " + uploadId + " found and removed");
            mCancelledIds.remove(uploadId);
            return true;
        }

        return false;
    }

    @Override
    public void onNetworkProgress(final int uploadId, final int progress, ArrayList<String> additionalData,
                                  UploadManagerCallbackBlocks callBackBlocks) {

        UploadNotificationManager.updateNotificationProgress(progress,mNotificationSettingsMap.get(uploadId),mNotificationBuilder,this);

        BroadcastLogic.broadcastProgress(uploadId, progress, this);

        runProgressCallback(uploadId, progress, additionalData, callBackBlocks);
    }


    @Override
    public void onNetworkComplete(final int uploadId, final int responseCode, final String responseMessage, String responseString,
                                  ArrayList<String> additionalData, UploadManagerCallbackBlocks callBackBlocks) {

        final String filteredMessage = responseMessage == null ? "" : responseMessage;

        if (responseCode >= RESPONSES_HTTP_OK_START && responseCode <= RESPONSES_HTTP_OK_END) {

            onSuccess(uploadId, responseCode, responseMessage, responseString, additionalData, callBackBlocks, filteredMessage);

        } else {

            onError(uploadId, null, additionalData, callBackBlocks);
        }
    }

    @Override
    public void onCancel(int uploadId,UploadManagerCallbackBlocks callBackBlocks, ArrayList<String> additionalData) {

        UploadNotificationManager.cancelNotification(mNotificationSettingsMap.get(uploadId), this);

        BroadcastLogic.broadCastCancel(uploadId, additionalData, this);

        runOnCancelCallback(uploadId, callBackBlocks, additionalData);
    }

    private void onError(final int uploadId, final Exception exception, ArrayList<String>
            additionalData, UploadManagerCallbackBlocks callBackBlocks) {

        runOnFailleCallback(uploadId, exception, additionalData, callBackBlocks);

        UploadNotificationManager.updateNotificationError(mNotificationSettingsMap.get(uploadId), mNotificationBuilder, this);

        BroadcastLogic.broadCastError(uploadId, exception, additionalData, this);
    }



    private void onSuccess(int uploadId, int responseCode, String responseMessage, String responseString,
                           ArrayList<String> additionalData, UploadManagerCallbackBlocks callBackBlocks, String filteredMessage) {

        UploadNotificationManager.updateNotificationCompleted(mNotificationSettingsMap.get(uploadId), mNotificationBuilder, this);

        BroadcastLogic.broadCastComplete(uploadId, responseCode, responseString, additionalData, filteredMessage, this);

        runCompleteCallback(uploadId,responseCode,responseMessage,responseString,additionalData,callBackBlocks);
    }


    @Override
    public Context getContext() {
        return this;
    }

    private void runOnCancelCallback(int uploadId, UploadManagerCallbackBlocks callBackBlocks, ArrayList<String> additionalData) {
        if (callBackBlocks != null) {
            callBackBlocks.onCancel(uploadId, additionalData);
        }
    }

    private void runOnFailleCallback(int uploadId, Exception exception, ArrayList<String> additionalData,
                                     UploadManagerCallbackBlocks callBackBlocks) {
        if (callBackBlocks != null) {
            callBackBlocks.onFailure(uploadId, additionalData, exception);
        }
    }

    public void runCompleteCallback(int uploadId, int responseCode, String responseMessage,
                                    String responseString, ArrayList<String> additionalData,
                                    UploadManagerCallbackBlocks callBackBlocks) {
        if (callBackBlocks != null) {
            callBackBlocks.onCompleted(uploadId, responseCode, responseMessage, additionalData, responseString);
        }
    }

    private void runProgressCallback(int uploadId, int progress, ArrayList<String> additionalData,
                                     UploadManagerCallbackBlocks callBackBlocks) {
        if (callBackBlocks != null) {
            callBackBlocks.onProgress(uploadId, additionalData, progress);
        }
    }

}
