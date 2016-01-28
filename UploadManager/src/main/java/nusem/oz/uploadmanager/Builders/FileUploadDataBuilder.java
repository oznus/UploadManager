package nusem.oz.uploadmanager.Builders;

import android.content.Context;
import android.net.Uri;

import java.net.URISyntaxException;

import nusem.oz.uploadmanager.Model.FileUploadData;
import nusem.oz.uploadmanager.Utils.FileUtils;

/**
 * Created by oznusem on 1/22/16.
 */
public class FileUploadDataBuilder {

    private static final String DEFAULT_PARAM_NAME = "file";
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private String fileName;
    private String paramName;
    private String contentType;
    private Uri uri;
    private Context context;
    private long fileSize = 0;

    public FileUploadDataBuilder(Uri uri,Context context) {
        this.uri = uri;
        this.context = context;
    }

    public FileUploadDataBuilder setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public FileUploadDataBuilder setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    public FileUploadDataBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public FileUploadDataBuilder setFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public FileUploadData build() throws URISyntaxException {

        if (fileName == null) {
            fileName = FileUtils.getNameStringOrReturnGeneral(context, uri);
        }

        if (paramName == null) {
            paramName = DEFAULT_PARAM_NAME;
        }

        if (fileSize == 0) {
            fileSize = FileUtils.getFileSizeByUri(context,uri);
        }

        if (contentType == null || contentType.equals("")) {
            contentType = DEFAULT_CONTENT_TYPE ;
        }

        FileUploadData data =  new FileUploadData(paramName, fileName, contentType, uri, fileSize);

        context = null;

        return data;
    }
}
