package nusem.oz.uploadmanager.Builders;

import android.content.Context;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import nusem.oz.uploadmanager.Model.FileUploadData;
import nusem.oz.uploadmanager.Model.NameValue;
import nusem.oz.uploadmanager.Model.NotificationSettings;
import nusem.oz.uploadmanager.Model.UploadRequest;

/**
 * Created by oznusem on 1/21/16.
 */
public class UploadManagerRequestBuilder  {

    private final UploadManagerRequestBuilderInterface mUploadManagerRequestBuilderInterface;

    public interface UploadManagerRequestBuilderInterface {
        int execute(UploadManagerRequestBuilder builder) throws MalformedURLException;
    }

    private NotificationSettings mNotificationSettings;
    private Context mContext;
    private String mUrl;
    private final ArrayList<FileUploadData> mFilesToUpload = new ArrayList<>();
    private final ArrayList<NameValue> mHeaders = new ArrayList<>();
    private final ArrayList<NameValue> mParameters= new ArrayList<>();
    private final ArrayList<String> mAdditionalParams = new ArrayList<>();
    private Class mUploadManagerCallbackBlocksClass;

    public UploadManagerRequestBuilder(Context context,UploadManagerRequestBuilderInterface uploadManagerRequestBuilderInterface) {
        mUploadManagerRequestBuilderInterface = uploadManagerRequestBuilderInterface;
        mContext = context;
        mNotificationSettings = new NotificationSettingsBuilder(context).create();
    }
    
    public UploadManagerRequestBuilder to(String url) {
        mUrl = url;
        return this;
    }

    public UploadManagerRequestBuilder addNotificationSettings(NotificationSettings mNotificationSettings) {
        this.mNotificationSettings = mNotificationSettings;
        return this;
    }

    public UploadManagerRequestBuilder uploadFile(Uri uri) throws URISyntaxException {
        this.mFilesToUpload.add(new FileUploadDataBuilder(uri,mContext).build());
        return this;
    }

    public UploadManagerRequestBuilder andFile(Uri uri) throws URISyntaxException {
        this.mFilesToUpload.add(new FileUploadDataBuilder(uri,mContext).build());
        return this;
    }

    public UploadManagerRequestBuilder uploadFile(FileUploadData data) throws URISyntaxException {
        this.mFilesToUpload.add(data);
        return this;
    }

    public UploadManagerRequestBuilder andFile(FileUploadData data) throws URISyntaxException {
        this.mFilesToUpload.add(data);
        return this;
    }

    public UploadManagerRequestBuilder addHeader(NameValue header) {
        this.mHeaders.add(header);
        return this;
    }

    public UploadManagerRequestBuilder addHeader(String key, String token) {
        this.mHeaders.add(new NameValue(key,token));
        return this;
    }

    public UploadManagerRequestBuilder addNetworkParameter(NameValue parameter) {
        this.mParameters.add(parameter);
        return this;
    }

    public UploadManagerRequestBuilder addAdditionalParam(String additionalParam) {
        this.mAdditionalParams.add(additionalParam);
        return this;
    }

    public UploadManagerRequestBuilder withCallbacksClass(Class uploadManagerCallbackBlocksClass) {
        this.mUploadManagerCallbackBlocksClass = uploadManagerCallbackBlocksClass;
        return this;
    }

    public int execute() throws MalformedURLException {
        return mUploadManagerRequestBuilderInterface.execute(this);
    }

    public UploadRequest create() throws MalformedURLException {

        UploadRequest request = new UploadRequest(mContext,mUrl,mFilesToUpload,mHeaders,
                mParameters,
                mNotificationSettings,
                mUploadManagerCallbackBlocksClass);

        request.checkIfValidAndThrow();

        return request;
    }
}
