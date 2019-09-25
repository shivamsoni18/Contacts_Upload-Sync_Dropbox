package com.example.contacts_dropbox;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends DropboxActivity {
    ArrayList<Contacts> selectUsers;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    RecyclerAdapter adapter;
    Cursor phones;

    Cursor cursor;
    ArrayList<String> vCard;
    String vfile;
    String name;
    String phoneNumber;
    TextView namedb, getbtn, loadbtn;
    LinearLayout loginButton;
    CheckBox selectall;
    ProgressDialog progress;
    FileMetadata mSelectedFile;
    Contacts selectUser;
    boolean isSelectedAll = false;
    private static final int PICKFILE_REQUEST_CODE = 1;


    String mPath = " ";
    String DBname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        recyclerView = (RecyclerView) findViewById(R.id.contacts_list);
        recyclerView.setHasFixedSize(true);
        namedb = (TextView) findViewById(R.id.namedb);
        selectall = (CheckBox) findViewById(R.id.checkboxall);
        TextView buttonUpload = (TextView) findViewById(R.id.files_button);
        TextView syncData = (TextView) findViewById(R.id.syncData);
        recyclerView.setLayoutManager(layoutManager);
        selectUsers = new ArrayList<Contacts>();

        showContacts();


        loginButton = (LinearLayout) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.startOAuth2Authentication(MainActivity.this, getString(R.string.app_key));
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DBname != null) {

                    String savedFileName = adapter.savefile();
                    uploadFile(savedFileName);

                } else {
                    Snackbar.make(v, "Please Login to Dropbox", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
//                    Toast.makeText(MainActivity.this, "Please Login to Dropbox", Toast.LENGTH_SHORT).show();
                }

                adapter.listNames.clear();
                selectall.setChecked(false);
                adapter.deselectAll();

            }
        });



        syncData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (DBname != null) {

                    downloadFile();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SyncContacts();
                            //Do something after 100ms
                        }
                    }, 3000);
                } else {


                    Snackbar.make(v, "Please Login to Dropbox", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
//                    Toast.makeText(MainActivity.this, "Please Login to Dropbox", Toast.LENGTH_SHORT).show();
                }

            }
        });

        selectall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    adapter.selectAll();
                } else {
                    adapter.deselectAll();
                }
            }
        });

    }


    class LoadContact extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            while (phones.moveToNext()) {
                String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                selectUser = new Contacts();

                selectUser.setName(name);
                selectUser.setPhone(phoneNumber);
                selectUser.setId(id);
                selectUsers.add(selectUser);

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            progress.show();
            adapter = new RecyclerAdapter(getApplicationContext(), inflater, selectUsers);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

            progress.dismiss();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progress.show();
            super.onProgressUpdate(values);
            progress.dismiss();
        }
    }

    private void uploadFile(String FileUri) {

        progress.show();

        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {

                Toast.makeText(MainActivity.this, "Dropbox backup complete", Toast.LENGTH_SHORT).show();

                progress.dismiss();

            }

            @Override
            public void onError(Exception e) {

                Log.e("", "Failed to upload file.", e);
                Toast.makeText(MainActivity.this,
                        "Failed to upload file.",
                        Toast.LENGTH_SHORT)
                        .show();

                progress.dismiss();
            }
        }).execute(FileUri, mPath);
    }

    private void downloadFile() {

        progress.show();

        new DownloadFileTask(MainActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {

                progress.dismiss();

            }

            @Override
            public void onError(Exception e) {
                //
            }
        }).execute();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void SyncContacts() {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/ContactsToDropbox.json");
        StringBuilder text = new StringBuilder();
        try {
            progress.show();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
            progress.dismiss();

        } catch (IOException e) {
        }


        try {

            JSONArray jsonArray = new JSONArray(text.toString());

            for (int n = 0; n < jsonArray.length(); n++) {
                progress.show();
                final JSONObject jsonObject = jsonArray.getJSONObject(n);

                Contacts contactsSync = new Contacts();
                contactsSync.setName(jsonObject.getString("name"));
                contactsSync.setPhone(jsonObject.getString("phone"));
                contactsSync.setId(jsonObject.getString("id"));

                String numberSTR = null;
                try {
                    numberSTR = jsonObject.getString("phone");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (numberSTR != null) {
                    Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberSTR));
                    String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
                    Cursor cur = MainActivity.this.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
                    try {
                        if (cur.moveToFirst()) {

                        } else {
                            selectUsers.add(contactsSync);
                        }
                    } finally {
                        if (cur != null)
                            cur.close();
                    }

                } else {
                }


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SetToPhonebook(jsonObject);
                    }
                }, 1000);

                progress.dismiss();

            }


            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            adapter = new RecyclerAdapter(getApplicationContext(), inflater, selectUsers);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);
            progress.dismiss();

            Toast.makeText(MainActivity.this, "Contacts updated", Toast.LENGTH_SHORT).show();



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean SetToPhonebook(JSONObject jsonObject) {

        String numberSTR = null;
        try {
            numberSTR = jsonObject.getString("phone");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (numberSTR != null) {

            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberSTR));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cur = MainActivity.this.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
            try {
                if (cur.moveToFirst()) {

                    return true;

                } else {

                    ContentValues contact = new ContentValues();

                    try {

                        Uri insertUri = null;
                        contact.put(android.provider.Contacts.People.NAME, jsonObject.getString("name"));
                        insertUri = getContentResolver().insert(android.provider.Contacts.People.CONTENT_URI, contact);
                        Log.d(getClass().getSimpleName(), insertUri.toString());
                        Uri phoneUri = Uri.withAppendedPath(insertUri, android.provider.Contacts.People.Phones.CONTENT_DIRECTORY);
                        contact.clear();
                        contact.put(android.provider.Contacts.People.Phones.TYPE, android.provider.Contacts.People.TYPE_MOBILE);
                        contact.put(android.provider.Contacts.People.NUMBER, jsonObject.getString("phone"));
                        Uri updateUri = getContentResolver().insert(phoneUri, contact);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } finally {
                if (cur != null)
                    cur.close();
            }

            return false;
        } else {
            return false;
        }

    }

    @Override
    protected void loadData() {


        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {

            @Override
            public void onComplete(FullAccount result) {

                DBname = result.getName().getDisplayName();
                namedb.setText("welcome, " + DBname);

            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();

    }


    @Override
    protected void onResume() {
//        progress.show();
        super.onResume();
//        progress.show();
    }


    private void showContacts() {

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            phones = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            LoadContact loadContact = new LoadContact();
            loadContact.execute();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showContacts();

        } else {

        }

    }


}