package com.example.contacts_dropbox;

/**
 * Created by Android on 9/6/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.dropbox.core.v2.files.FileMetadata;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    public List<Contacts> cont;
    Contacts list;
    boolean isSelectedAll = false;
    Context context;
    ArrayList<JSONObject> listNames;
    Cursor phones;

    String FILE_NAME = "ContactsToDropbox.json";


    public RecyclerAdapter(Context context, LayoutInflater inflater, List<Contacts> items) {
        this.layoutInflater = inflater;
        this.cont = items;
        this.context = context;
        listNames = new ArrayList<>();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layoutInflater.inflate(R.layout.contactlist_raw, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        list = cont.get(position);


        final String id = (list.getId());
        final String name = (list.getName());
        final String num = (list.getPhone());

        holder.title.setText(name);
        holder.phone.setText(list.getPhone());

        holder.selectCheckbox.setChecked(list.isChecked);
        holder.selectCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                JSONObject data = new JSONObject();
                list.setChecked(holder.selectCheckbox.isChecked());

                if (holder.selectCheckbox.isChecked()) {

                    try {
                        data.put("id", id);
                        data.put("name", name);
                        data.put("phone", num);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    listNames.add(data);

                } else {

                    listNames.remove(data);

                }

            }
        });
        cont.set(position, list);


        if (!isSelectedAll) {
            holder.selectCheckbox.setChecked(false);


        } else {
            holder.selectCheckbox.setChecked(true);
        }


    }

    public String savefile() {

        File file = setExtDir(context, FILE_NAME);

        String fileContents = listNames.toString();
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(fileContents.getBytes());
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //
                }

            }
        }
        return file.getAbsolutePath();

    }

    public File setExtDir(Context context, String FILE_NAME) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        if (!file.mkdir()) {
            file.mkdir();
        }

        file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), FILE_NAME);
        return file;
    }


    public void selectAll() {

        isSelectedAll = true;
        notifyDataSetChanged();
    }

    public void deselectAll() {

        isSelectedAll = false;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return cont.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView phone;
        public LinearLayout contact_select_layout;
        CheckBox selectCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);

            title = (TextView) itemView.findViewById(R.id.name);
            phone = (TextView) itemView.findViewById(R.id.no);
            contact_select_layout = (LinearLayout) itemView.findViewById(R.id.contact_select_layout);
            selectCheckbox = (CheckBox) itemView.findViewById(R.id.checkbox);

        }

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}