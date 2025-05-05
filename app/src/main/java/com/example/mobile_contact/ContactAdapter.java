package com.example.mobile_contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    ArrayList<Contact> contacts = new ArrayList<>();

    public ContactAdapter(ArrayList<Contact> contacts){
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.one_contact, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.tel.setText(contact.getTel());
        holder.imageView.setImageBitmap(contact.getImg());
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactHolder extends RecyclerView.ViewHolder{
        TextView name, tel;
        ImageView imageView;
        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            tel = itemView.findViewById(R.id.tel);
            imageView = itemView.findViewById(R.id.IV);
        }
    }
}
