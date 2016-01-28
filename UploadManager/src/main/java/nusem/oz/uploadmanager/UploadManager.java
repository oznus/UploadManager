package nusem.oz.uploadmanager;

import android.content.Context;

import java.net.MalformedURLException;

import nusem.oz.uploadmanager.Builders.UploadManagerRequestBuilder;
import nusem.oz.uploadmanager.Model.UploadRequest;
import nusem.oz.uploadmanager.Service.UploadService;

/**
 * Created by oznusem on 1/21/16.
 */
public class UploadManager implements UploadManagerRequestBuilder.UploadManagerRequestBuilderInterface {

    private static UploadManager singleton;

    public static UploadManagerRequestBuilder with(Context context) {

        if (singleton == null) {
            synchronized (UploadManager.class) {
                if (singleton == null) {
                    singleton = new UploadManager();
                }
            }
        }
        return new UploadManagerRequestBuilder(context,singleton);
    }


    @Override
    public int execute(UploadManagerRequestBuilder builder) throws MalformedURLException {
        UploadRequest request = builder.create();
        UploadService.startUpload(request);
        return request.getNotificationSettings().getNotificationId();
    }

    public static void cancel(int uploadId,Context context) {
        UploadService.cancelUpload(context,uploadId);
    }
}
