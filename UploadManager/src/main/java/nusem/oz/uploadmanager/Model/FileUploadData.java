package nusem.oz.uploadmanager.Model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by oznusem on 1/24/16.
 */
public class FileUploadData implements Parcelable {

    private static final String NEW_LINE = "\r\n";

    private String fileName;
    private String paramName;
    private String contentType;
    private Uri uri;
    private long fileSize;

    public FileUploadData(final String parameterName, final String fileName, final String contentType, final Uri uri, long fileSize) {
        this.paramName = parameterName;
        this.contentType = contentType;
        this.fileName = fileName;
        this.uri = uri;
        this.fileSize = fileSize;
    }

    public final InputStream getStream(Context context) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uri);
    }

    public File getFile() {
        return new File(uri.toString());
    }

    public byte[] getMultipartHeader() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        builder.append("Content-Disposition: form-data; name=\"")
                .append(paramName)
                .append("\"; filename=\"")
                .append(fileName)
                .append("\"")
                .append(NEW_LINE);

        if (contentType != null) {
            builder.append("Content-Type: ")
                    .append(contentType)
                    .append(NEW_LINE);
        }

        builder.append(NEW_LINE);

        return builder.toString().getBytes("UTF-8");
    }

    public long length() {
        return fileSize;
    }


    public static final Creator<FileUploadData> CREATOR = new Creator<FileUploadData>() {
        @Override
        public FileUploadData createFromParcel(final Parcel in) {
            return new FileUploadData(in);
        }

        @Override
        public FileUploadData[] newArray(final int size) {
            return new FileUploadData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeString(paramName);
        parcel.writeString(contentType);
        parcel.writeString(fileName);
        parcel.writeParcelable(uri, arg1);
        parcel.writeLong(this.fileSize);
    }

    private FileUploadData(Parcel in) {
        paramName = in.readString();
        contentType = in.readString();
        fileName = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        fileSize = in.readLong();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
