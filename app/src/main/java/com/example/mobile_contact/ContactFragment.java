package com.example.mobile_contact;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.InputStream;
import java.util.ArrayList;

public class ContactFragment extends Fragment {
    private static final int REQUEST_READ_CONTACTS = 1;

    private Handler handler;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (requireContext().checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                requireContext().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            new DataThread().start();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, container, false);

        recyclerView = view.findViewById(R.id.RV);
        swipeRefreshLayout = view.findViewById(R.id.SW);

        handler = new Handler(msg -> {
            ArrayList<Contact> contacts = (ArrayList<Contact>) msg.obj;
            ContactAdapter adapter = new ContactAdapter(contacts);
            recyclerView.setAdapter(adapter);
            return true;
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recyclerView.removeAllViews();
            new DataThread().start();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new DataThread().start();
        } else {
            Log.e("Permission", "Доступ к контактам не получен");
        }
    }

    class DataThread extends Thread {
        @Override
        public void run() {
            ArrayList<Contact> contactList = new ArrayList<>();
            ContentResolver resolver = requireActivity().getContentResolver();

            try (Cursor cursor = resolver.query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {

                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        int hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone > 0) {
                            try (Cursor phoneCursor = resolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id}, null)) {

                                if (phoneCursor != null) {
                                    while (phoneCursor.moveToNext()) {
                                        String phone = phoneCursor.getString(
                                                phoneCursor.getColumnIndexOrThrow(
                                                        ContactsContract.CommonDataKinds.Phone.NUMBER));

                                        Bitmap photo = null;
                                        InputStream stream = ContactsContract.Contacts
                                                .openContactPhotoInputStream(resolver,
                                                        ContentUris.withAppendedId(
                                                                ContactsContract.Contacts.CONTENT_URI,
                                                                Long.parseLong(id)));
                                        if (stream != null) {
                                            photo = BitmapFactory.decodeStream(stream);
                                        }

                                        String clean = phone.replaceAll("[^+0-9]", "");
                                        String formatted = format(clean);

                                        boolean duplicate = false;
                                        for (Contact c : contactList) {
                                            if (c.getName().equalsIgnoreCase(name) &&
                                                    c.getTel().replaceAll("[^+0-9]", "")
                                                            .equals(clean.replaceAll("[^+0-9]", ""))) {
                                                duplicate = true;
                                                break;
                                            }
                                        }
                                        if (!duplicate) {
                                            contactList.add(new Contact(name, formatted, photo));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Message msg = new Message();
            msg.obj = contactList;
            handler.sendMessage(msg);
        }
    }

    private String format(String phoneNumber) {
        if (phoneNumber.length() < 11)
            return phoneNumber;
        String phon = "";
        int k = 1;
        for (int i = phoneNumber.length() - 1; i > -1;i--){
            phon = phoneNumber.charAt(i) + phon;
            if (k == 4 || k == 2)
                phon = "-"+phon;
            else if (k == 7)
                phon = ") "+phon;
            else if (k == 10)
                phon = " ("+phon;
            k++;
        }
        System.out.println(phoneNumber.charAt(0) + " "+ phoneNumber);
        return phon;
    }
}
