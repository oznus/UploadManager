package nusem.oz.uploadmanager.Broadcast;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import nusem.oz.uploadmanager.Service.UploadService;

/**
 * Created by oznusem on 1/24/16.
 */
public class BroadcastLogic {

    public static final String SERVER_RESPONSE_CODE = "serverResponseCode";
    public static final String SERVER_RESPONSE_MESSAGE = "serverResponseMessage";
    public static final String SERVER_RESPONSE_STRING = "serverResponseString";
    public static final String STATUS = "status";
    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_CANCEL = 3;
    public static final String PROGRESS = "progress";
    public static final String PARAM_ADDITIONAL_DATA = "additionalData";
    public static final String ERROR_EXCEPTION = "errorException";

    public static void broadcastProgress(int uploadId, int progress, Context context) {
        final Intent intent = new Intent(UploadService.Actions.BROADCAST_ACTION);
        intent.putExtra(UploadService.UPLOAD_ID, uploadId);
        intent.putExtra(STATUS, STATUS_IN_PROGRESS);
        intent.putExtra(PROGRESS, progress);
        context.sendBroadcast(intent);
    }

    public static void broadCastComplete(int uploadId, int responseCode, String responseString,
                                         ArrayList<String> additionalData, String filteredMessage, Context context) {
        final Intent intent = new Intent(UploadService.Actions.BROADCAST_ACTION);
        intent.putExtra(UploadService.UPLOAD_ID, uploadId);
        intent.putExtra(STATUS, STATUS_COMPLETED);
        intent.putExtra(SERVER_RESPONSE_CODE, responseCode);
        intent.putExtra(SERVER_RESPONSE_MESSAGE, filteredMessage);
        intent.putExtra(SERVER_RESPONSE_STRING, responseString);
        intent.putStringArrayListExtra(PARAM_ADDITIONAL_DATA, additionalData);
        context.sendBroadcast(intent);
    }

    public static void broadCastError(int uploadId, Exception exception, ArrayList<String> additionalData,Context context) {
        final Intent intent = new Intent(UploadService.Actions.BROADCAST_ACTION);
        intent.putExtra(UploadService.UPLOAD_ID, uploadId);
        intent.putExtra(STATUS, STATUS_ERROR);
        intent.putExtra(ERROR_EXCEPTION, exception);
        intent.putStringArrayListExtra(PARAM_ADDITIONAL_DATA, additionalData);
        context.sendBroadcast(intent);
    }

    public static void broadCastCancel(int uploadId, ArrayList<String> additionalData,Context context) {
        final Intent intent = new Intent(UploadService.Actions.BROADCAST_ACTION);
        intent.putExtra(UploadService.UPLOAD_ID, uploadId);
        intent.putExtra(STATUS, STATUS_CANCEL);
        intent.putStringArrayListExtra(PARAM_ADDITIONAL_DATA, additionalData);
        context.sendBroadcast(intent);
    }

}
