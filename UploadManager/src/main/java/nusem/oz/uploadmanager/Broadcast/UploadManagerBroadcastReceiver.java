
package nusem.oz.uploadmanager.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import nusem.oz.uploadmanager.Service.UploadService;


/**
 * Created by oznusem on 1/24/16.
 */
public abstract class UploadManagerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = UploadManagerBroadcastReceiver.class.getSimpleName();

    public abstract void onProgress(final int uploadId, final int progress);
    public abstract void onError(final int uploadId, final Exception exception);
    public abstract void onCompleted(final int uploadId, final int serverResponseCode, final String serverResponseMessage, String responceString);

    public void register(Context context) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadService.Actions.BROADCAST_ACTION);
        context.registerReceiver(this, intentFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            if (UploadService.Actions.BROADCAST_ACTION.equals(intent.getAction())) {
                final int status = intent.getIntExtra(BroadcastLogic.STATUS, 0);
                final int uploadId = intent.getIntExtra(UploadService.UPLOAD_ID, 0);

                Log.d(TAG, "onReceive uploadId - " + uploadId);

                switch (status) {
                    case BroadcastLogic.STATUS_ERROR:
                        final Exception exception = (Exception) intent
                                .getSerializableExtra(BroadcastLogic.ERROR_EXCEPTION);
                        onError(uploadId, exception);
                        break;

                    case BroadcastLogic.STATUS_COMPLETED:
                        final int responseCode = intent.getIntExtra(BroadcastLogic.SERVER_RESPONSE_CODE, 0);
                        final String responseMsg = intent.getStringExtra(BroadcastLogic.SERVER_RESPONSE_MESSAGE);
                        final String responseString = intent.getStringExtra(BroadcastLogic.SERVER_RESPONSE_STRING);
                        onCompleted(uploadId, responseCode, responseMsg, responseString);
                        break;

                    case BroadcastLogic.STATUS_IN_PROGRESS:
                        final int progress = intent.getIntExtra(BroadcastLogic.PROGRESS, 0);
                        onProgress(uploadId, progress);
                        break;

                    default:
                        break;
                }
            }
        }

    }
}
