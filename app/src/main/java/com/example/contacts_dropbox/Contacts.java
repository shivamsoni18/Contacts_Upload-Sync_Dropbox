package com.example.contacts_dropbox;


/**
 * Created by Android on 8/23/2017.
 */
 
public class Contacts {
 
    String name;
    String phone;
    String id;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    boolean isChecked;


    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getPhone() {
        return phone;
    }
 
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}