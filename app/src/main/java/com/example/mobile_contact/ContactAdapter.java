package com.example.mobile_contact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private final ArrayList<Object> items = new ArrayList<>();

    public ContactAdapter(ArrayList<Contact> contacts) {
        Collections.sort(contacts, Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER));
        char lastHeader = '\0';

        for (Contact contact : contacts) {
            char firstChar = Character.toUpperCase(contact.getName().charAt(0));
            if (firstChar != lastHeader) {
                items.add(String.valueOf(firstChar));
                lastHeader = firstChar;
            }
            items.add(contact);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_CONTACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.section_header, parent, false);
            return new HeaderHolder(view);
        } else {
            View view = inflater.inflate(R.layout.one_contact, parent, false);
            return new ContactHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).header.setText((String) items.get(position));
        } else {
            Contact contact = (Contact) items.get(position);
            ContactHolder ch = (ContactHolder) holder;
            ch.name.setText(contact.getName());
            ch.tel.setText(contact.getTel());
            if (contact.getImg() != null) {
                ch.imageView.setImageBitmap(contact.getImg());
            } else {
                ch.imageView.setImageResource(R.drawable.screen); // запасное изображение
            }

            ch.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + contact.getTel()));
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), "Нет разрешения на звонок", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ContactHolder extends RecyclerView.ViewHolder {
        TextView name, tel;
        ImageView imageView;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            tel = itemView.findViewById(R.id.tel);
            imageView = itemView.findViewById(R.id.IV);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeaderHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.section_text);
        }
    }
}
