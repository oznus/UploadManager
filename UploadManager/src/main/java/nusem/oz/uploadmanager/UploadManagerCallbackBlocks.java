package nusem.oz.uploadmanager;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by oznusem on 1/22/16.
 */
public interface UploadManagerCallbackBlocks {

    void onProgress(final int uploadId, ArrayList<String> additionalData, final int progress);

    void onFailure(final int uploadId, ArrayList<String> additionalData, @Nullable final Exception exception);

    void onCompleted(final int uploadId, final int serverResponseCode, final String serverResponseMessage,
                     ArrayList<String> additionalData, String responseString);

    void onCancel(final int uploadId, ArrayList<String> additionalData);
}
