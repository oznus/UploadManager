package nusem.oz.uploadmanager.Model;

import android.os.Parcel;
import android.os.Parcelable;

import nusem.oz.uploadmanager.Utils.GeneralUtils;


/**
 * Created by oznusem on 1/24/16.
 */
public class NotificationSettings implements Parcelable {

    private final int iconResourceID;
    private final String title;
    private final String message;
    private final String completed;
    private final String error;
    private final boolean autoClearOnSuccess;
    private int notificationId;

    public NotificationSettings(final int iconResourceID,
                                final String title,
                                final String message,
                                final String completed,
                                final String error,
                                final boolean autoClearOnSuccess)
            throws IllegalArgumentException {

        if (title == null || message == null || completed == null || error == null) {
            throw new IllegalArgumentException("You can't provide null parameters");
        }

        this.iconResourceID = iconResourceID;
        this.title = title;
        this.message = message;
        this.completed = completed;
        this.error = error;
        this.autoClearOnSuccess = autoClearOnSuccess;
        this.notificationId = GeneralUtils.generateUID();
    }

    public int getNotificationId() {
        return notificationId;
    }


    public final int getIconResourceID() {
        return iconResourceID;
    }

    public final String getTitle() {
        return title;
    }

    public final String getMessage() {
        return message;
    }

    public final String getCompleted() {
        return completed;
    }

    public final String getError() {
        return error;
    }

    public final boolean isAutoClearOnSuccess() {
        return autoClearOnSuccess;
    }


    public static final Creator<NotificationSettings> CREATOR =
            new Creator<NotificationSettings>() {
                @Override
                public NotificationSettings createFromParcel(final Parcel in) {
                    return new NotificationSettings(in);
                }

                @Override
                public NotificationSettings[] newArray(final int size) {
                    return new NotificationSettings[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeInt(iconResourceID);
        parcel.writeString(title);
        parcel.writeString(message);
        parcel.writeString(completed);
        parcel.writeString(error);
        parcel.writeByte((byte) (autoClearOnSuccess ? 1 : 0));
        parcel.writeInt(notificationId);
    }

    private NotificationSettings(Parcel in) {
        iconResourceID = in.readInt();
        title = in.readString();
        message = in.readString();
        completed = in.readString();
        error = in.readString();
        autoClearOnSuccess = in.readByte() == 1;
        notificationId = in.readInt();
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }
}
