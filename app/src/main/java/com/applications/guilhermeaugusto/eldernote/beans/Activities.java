package com.applications.guilhermeaugusto.eldernote.beans;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by guilhermeaugusto on 07/08/2014.
 */
public class Activities implements Serializable {
    private  int id;
    private String title;

    public Activities(int id, String title){
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return this.title;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeInt(id);
        out.writeObject(title);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        id = in.readInt();
        title = (String) in.readObject();
    }

    public int getId(){
        return this.id;
    }
    public String getTitle(){
        return this.title;
    }
}