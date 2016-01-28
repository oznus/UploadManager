package nusem.oz.uploadmanager.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    private static final long DEFAULT_FILE_SIZE = 100;

    public static String getNameStringOrReturnGeneral(final Context context, final Uri uri) {
        String name = getFileNameByUri(context,uri);
        if (name == null || name.length() == 0 || name.equals("null")) {
            return generateRandomName();
        }
        return name;
    }

    public static String generateRandomName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return"Attachment_" + timeStamp + "_";
    }

    public static String getFileNameByUri(Context context, Uri uri) {
        String fileName = "unknown";
        if (uri.getScheme().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                try {
                    int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    fileName = cursor.getString(columnIndex);
                } catch (IllegalArgumentException e) {
                    Log.v(TAG, "getFileNameByUri " + e.getMessage());
                            cursor.close();
                    return fileName;
                }

            }
            cursor.close();
        } else if (uri.getScheme().compareTo("file") == 0) {
            fileName = uri.getLastPathSegment();
        } else {
            fileName = fileName + "_" + uri.getLastPathSegment();
        }
        return fileName;
    }


    public static long getFileSizeByUri(Context context, Uri uri) {
        if (uri.getScheme().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                try {
                   int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
                   Long result = Long.valueOf(cursor.getString(columnIndex));
                   cursor.close();
                   return result;
                } catch (IllegalArgumentException e) {
                    cursor.close();
                    Log.e(TAG, "getFileSizeByUri " + e);
                    return DEFAULT_FILE_SIZE;
                }
            }
            cursor.close();
        } else {
            try {
                File f = new File(uri.getPath());
                return f.length();
            } catch (Exception e) {
                //eat it
                return DEFAULT_FILE_SIZE;
            }
        }
        return DEFAULT_FILE_SIZE;
    }


}
