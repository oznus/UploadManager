package nusem.oz.uploadmanager.Network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import nusem.oz.uploadmanager.Model.FileUploadData;
import nusem.oz.uploadmanager.Model.NameValue;
import nusem.oz.uploadmanager.Service.UploadServiceListener;
import nusem.oz.uploadmanager.UploadManagerCallbackBlocks;

/**
 * Created by oznusem on 1/22/16.
 */
public class URLConnectionUpload {

    private static final String TAG = URLConnectionUpload.class.getSimpleName();

    private static final int BUFFER_SIZE = 4096;
    private static final String NEW_LINE = "\r\n";
    private static final String TWO_HYPHENS = "--";


    public static void upload(final Integer uploadId, final String url, final String method,
                              final ArrayList<FileUploadData> filesToUpload,
                              final ArrayList<NameValue> requestHeaders,
                              final ArrayList<NameValue> requestParameters,
                              ArrayList<String> additionalData,
                              UploadManagerCallbackBlocks callBackBlocks,
                              UploadServiceListener listener
    ) throws IOException {

        HttpURLConnection conn = null;
        OutputStream requestStream = null;
        try {

            final String boundary = getBoundary();
            final byte[] boundaryBytes = getBoundaryBytes(boundary);

            conn = createMultipartyHttpURLConnection(url, method, boundary);

            setRequestHeaders(conn, requestHeaders);

            requestStream = conn.getOutputStream();
            setRequestParameters(requestStream, requestParameters, boundaryBytes);

            if (uploadFiles(uploadId, requestStream, filesToUpload, boundaryBytes, additionalData,callBackBlocks,listener)) {
                final byte[] trailer = getTrailerBytes(boundary);
                requestStream.write(trailer, 0, trailer.length);
                final int responseCode = conn.getResponseCode();
                final String responseMessage = conn.getResponseMessage();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer stringBuffer = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    if (isInterrputed(uploadId, additionalData, callBackBlocks, listener)) {
                        return;
                    }
                    stringBuffer.append(inputLine);
                }
                in.close();

                listener.onNetworkComplete(uploadId, responseCode, responseMessage, stringBuffer.toString(),
                        additionalData, callBackBlocks);
            }
        } finally {
            closeOutputStream(requestStream);
            closeConnection(conn);
        }
    }

    private static long getTotalBytes(final ArrayList<FileUploadData> filesToUpload) {
        long total = 0;

        for (FileUploadData file : filesToUpload) {
            total += file.length();
        }
        return total;
    }

    private static void closeInputStream(final InputStream stream) {
        Log.v(TAG,"closeInputStream enter");
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeOutputStream -" + e.getMessage());
            }
        }
    }

    private static void closeOutputStream(final OutputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeOutputStream - " + e.getMessage());
            }
        }
    }

    private static void closeConnection(final HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private static String getBoundary() {
        final StringBuilder builder = new StringBuilder();

        builder.append("---------------------------").append(System.currentTimeMillis());

        return builder.toString();
    }

    private static byte[] getBoundaryBytes(final String boundary) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE).append(TWO_HYPHENS).append(boundary).append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private static byte[] getTrailerBytes(final String boundary) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();

        builder.append(NEW_LINE).append(TWO_HYPHENS).append(boundary).append(TWO_HYPHENS).append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    private static HttpURLConnection createMultipartyHttpURLConnection(final String url, final String method,
                                                                       final String boundary) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setChunkedStreamingMode(0);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Accept-Encoding","utf8");
        return conn;
    }

    private static void setRequestHeaders(final HttpURLConnection conn, final ArrayList<NameValue> requestHeaders) {
        if (!requestHeaders.isEmpty()) {
            for (final NameValue param : requestHeaders) {
                conn.setRequestProperty(param.getName(), param.getValue());
            }
        }
    }

    private static void setRequestParameters(final OutputStream requestStream, final ArrayList<NameValue> requestParameters,
                                             final byte[] boundaryBytes) throws IOException {
        if (!requestParameters.isEmpty()) {

            for (final NameValue parameter : requestParameters) {
                requestStream.write(boundaryBytes, 0, boundaryBytes.length);
                byte[] formItemBytes = parameter.getBytes();
                requestStream.write(formItemBytes, 0, formItemBytes.length);
            }
        }
    }

    private static boolean uploadFiles(final int uploadId, final OutputStream requestStream, final ArrayList<FileUploadData> filesToUpload,
                                       final byte[] boundaryBytes, final ArrayList<String> additionalData,
                                       final UploadManagerCallbackBlocks callBlocks,
                                       UploadServiceListener listener) throws IOException {

        Log.v(TAG, "uploadFile  - enter");
        final long totalBytes = getTotalBytes(filesToUpload);
        int progress = -1;
        long uploadedBytes = 0;
        int lastPublishedProgress = 0;

        for (FileUploadData file : filesToUpload) {
            requestStream.write(boundaryBytes, 0, boundaryBytes.length);
            byte[] headerBytes = file.getMultipartHeader();
            requestStream.write(headerBytes, 0, headerBytes.length);

            final InputStream stream = file.getStream(listener.getContext());
            byte[] buffer = new byte[BUFFER_SIZE];
            long bytesRead;

            try {
                while ((bytesRead = stream.read(buffer, 0, buffer.length)) > 0) {
                    if (isInterrputed(uploadId, additionalData, callBlocks, listener)) {
                        return false;
                    }
                    requestStream.write(buffer, 0, buffer.length);
                    uploadedBytes += bytesRead;
                    final long finalUploadedBytes = uploadedBytes;
                    double percentageUsed = 100.0 * finalUploadedBytes / totalBytes;
                    final int newProgress = (int) Math.round(percentageUsed);
                    if (newProgress != progress) {
                        progress = newProgress;
                        if (newProgress > lastPublishedProgress) {
                            listener.onNetworkProgress(uploadId, newProgress, additionalData, callBlocks);
                            Log.i(TAG, "onNetworkProgress  upload id " + uploadId + ". progress = " + newProgress);
                        }
                        lastPublishedProgress = newProgress;
                    }
                }

            }
            finally {
                closeInputStream(stream);
            }
        }

        return true;
    }

    private static boolean isInterrputed(int uploadId, ArrayList<String> additionalData, UploadManagerCallbackBlocks callBlocks, UploadServiceListener listener) {
        if (listener.checkIfCanceledAndRemove(uploadId)) {
            listener.onCancel(uploadId,callBlocks,additionalData);
            return true;
        }
        return false;
    }


}
