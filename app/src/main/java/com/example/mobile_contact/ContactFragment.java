package com.example.mobile_contact;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.InputStream;
import java.util.ArrayList;

public class ContactFragment extends Fragment {
    private static final int REQUEST_READ_CONTACTS = 1;
    ArrayList<Contact> contacts;
    Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment,container, false);
        RecyclerView recyclerView = view.findViewById(R.id.RV);
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg){
                super.handleMessage(msg);

                contacts = (ArrayList<Contact>) msg.obj;
                ContactAdapter valuteAdapter = new ContactAdapter(contacts);
                recyclerView.setAdapter(valuteAdapter);

            }
        };
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.SW);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.removeAllViews();
                contacts.clear();
                DataThread dataThread = new DataThread();
                dataThread.start();
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        return view;    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Если разрешение получено, запускаем поток снова
                DataThread dataThread = new DataThread();
                dataThread.start();
            }
        }
    }

    class DataThread extends Thread {
        ArrayList<Contact> contacts = new ArrayList<>();

        @Override
        public void run() {
            super.run();
            // Получаем контакты
            ContentResolver resolver = getActivity().getContentResolver();
            Cursor cursor = resolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    if (idIndex < 0) {
                        Log.e("TAG", "Required column _ID not found");
                        return;
                    }
                    String id = cursor.getString(idIndex);
                    String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "No Name";
                    // Проверяем, есть ли у контакта телефон
                    int hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                    if (Integer.parseInt(cursor.getString(hasPhoneIndex)) > 0) {
                        // Получаем номера телефонов
                        Cursor phoneCursor = resolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null);
                        if (phoneCursor != null) {
                            while (phoneCursor.moveToNext()) {
                                int num = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                String phoneNumber = phoneCursor.getString(num);
                                // Получаем фото контакта
                                Bitmap photo = null;
                                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                                        resolver, ContentUris.withAppendedId(
                                                ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id)));
                                if (inputStream != null) {
                                    photo = BitmapFactory.decodeStream(inputStream);
                                }
                                // Добавляем контакт в список
                                contacts.add(new Contact(name, phoneNumber, photo));
                            }
                            phoneCursor.close();
                        }
                    }
                }
                cursor.close();
            }
            // Отправляем данные в основной поток
            Message msg = new Message();
            msg.obj = contacts;
            handler.sendMessage(msg);
        }
    }
}
