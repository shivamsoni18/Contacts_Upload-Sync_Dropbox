package com.example.contacts_dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Async task to upload a file to a directory
 */
class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);

        void onError(Exception e);
    }

    UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected FileMetadata doInBackground(String... params) {
        File localUri = new File(params[0]);
//        File localFile = localUri;

        if (localUri != null) {

            String remoteFolderPath = params[0];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localUri.getName();

            try (InputStream inputStream = new FileInputStream(localUri)) {
                return mDbxClient.files().uploadBuilder("/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
