package nusem.oz.uploadmanager.Service;

import android.content.Context;

import java.util.ArrayList;

import nusem.oz.uploadmanager.UploadManagerCallbackBlocks;

/**
 * Created by oznusem on 1/24/16.
 */
public interface UploadServiceListener {

    boolean checkIfCanceledAndRemove(int uploadId);

    Context getContext();

    void onCancel(int uploadId, UploadManagerCallbackBlocks callBackBlocks, ArrayList<String> additionalData);

    void onNetworkComplete(int uploadId, int responseCode, String responseMessage,
                           String s, ArrayList<String> additionalData,
                           UploadManagerCallbackBlocks callBackBlocks);

    void onNetworkProgress(int uploadId, int newProgress, ArrayList<String> additionalData, UploadManagerCallbackBlocks callBlocks);

}
