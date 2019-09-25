package com.example.contacts_dropbox;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    private static final String ACCESS_TOKEN = "3XkmBSHo7aAAAAAAAAAACwkd7ZPeELsf3hDSLDEuZsbpDKybkuOcVaYOmsy2PMM-";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void main(String args[]) throws DbxException {
        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/ContactsSyncDB").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }


    }
}