package com.example.yg.contactsgenerator;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_WRITE_CONTACT_REQUEST_CODE = 144;
    private final int PERMISSION_READ_CONTACT_REQUEST_CODE = 145;
    private EditText numbersInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numbersInput = findViewById(R.id.numbers);
        Button generator = findViewById(R.id.generateButton);
        generator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPermission(Manifest.permission.WRITE_CONTACTS, PERMISSION_WRITE_CONTACT_REQUEST_CODE)){
                    validateAndProcess();
                }
            }
        });

        Button deleteAll = findViewById(R.id.DeleteAllButton);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPermission(Manifest.permission.READ_CONTACTS, PERMISSION_READ_CONTACT_REQUEST_CODE)){
                    deleteAllContact();
                }
            }
        });
    }

    private void deleteAllContact(){
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            contentResolver.delete(uri, null, null);
        }
    }

    private void validateAndProcess(){
        int number = getNumbers();
        if(number == 0){
            Toast.makeText(getApplicationContext(), "Please enter a valid number", Toast.LENGTH_LONG).show();
        }else{
            generateContacts(number);
        }
    }

    private int getNumbers(){
        String numberString = numbersInput.getText().toString();
        if(numberString != null){
            return Integer.parseInt(numberString);
        }
        return 0;
    }

    private void generateContacts(int quantity){

        for(int i=0; i <quantity; i++){
            ArrayList<ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();
            String DisplayName = "test " + i;
            String MobileNumber = "123456" + i;
            String emailID = "test" + i + "@nomail.com";

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    //name
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, DisplayName)
                    //mobile number
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    //email
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(getApplicationContext(), quantity + " contacts have been created.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private Boolean hasPermission(String permission, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasPermission = (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                return false;
            }

            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_WRITE_CONTACT_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    validateAndProcess();
                }
                break;
            }
            case PERMISSION_READ_CONTACT_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deleteAllContact();
                }
                break;
            }
        }
    }
}
