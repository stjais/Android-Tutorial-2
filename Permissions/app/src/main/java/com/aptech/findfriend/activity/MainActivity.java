package com.aptech.findfriend.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aptech.findfriend.R;
import com.aptech.findfriend.database.UsersProvider;
import com.aptech.findfriend.interfaces.ProceedAfterPermissionListener;

public class MainActivity extends AppActivity implements View.OnClickListener {

    EditText etName, etMobileNo;
    RadioButton rbMale, rbFemale;
    CheckBox cbTerms;
    Button btnSubmit, btnFind;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //binding views
        etName = (EditText) findViewById(R.id.etName);
        etMobileNo = (EditText) findViewById(R.id.etMobileNo);
        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);
        cbTerms = (CheckBox) findViewById(R.id.cbTerms);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnFind = (Button) findViewById(R.id.btnFind);

        proceedAfterPermissionListener(new ProceedAfterPermissionListener() {
            @Override
            public void proceedAfterPermission() {
                init();
            }
        });

        checkAppPermissions();

    }

    /**
     *
     */
    private void init() {
        btnSubmit.setOnClickListener(this);
        btnFind.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSubmit:

                String mobile_no = etMobileNo.getText().toString();
                if (mobile_no.length() != 10) {
                    Toast.makeText(getApplicationContext(), "Invalid Mobile Number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = etName.getText().toString();
                if (name.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid Name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String gender = null;
                if (rbMale.isChecked()) {
                    gender = "Male";
                }
                if (rbFemale.isChecked()) {
                    gender = "Female";
                }
                if (gender == null) {
                    Toast.makeText(getApplicationContext(), "Select Gender.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cbTerms.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Accept Terms and Conditions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                addData(name, mobile_no, gender);

                break;
            case R.id.btnFind:

                mobile_no = etMobileNo.getText().toString();

                if (mobile_no.length() != 10) {
                    Toast.makeText(getApplicationContext(), "Invalid Mobile Number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                findData(mobile_no);

                break;
        }
    }

    private void addData(String name, String mobile_no, String gender) {

        // Add a new student record
        ContentValues values = new ContentValues();
        values.put(UsersProvider.MOBILE_NUMBER, mobile_no);
        values.put(UsersProvider.NAME, name);
        values.put(UsersProvider.GENDER, gender);

        Uri uri = getContentResolver().insert(UsersProvider.CONTENT_URI, values);

        if (uri == null) {
            Toast.makeText(getBaseContext(), "Failed.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Success.", Toast.LENGTH_LONG).show();
        }

    }

    private void findData(String mobile_no) {
        // Retrieve student records
        String URL = "content://com.aptech.findfriend.database.UsersProvider/users/" + mobile_no;

        Uri uri = Uri.parse(URL);

        Cursor c = getContentResolver().query(uri, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            do {

                String mobile = c.getString(c.getColumnIndex(UsersProvider.MOBILE_NUMBER));
                String name = c.getString(c.getColumnIndex(UsersProvider.NAME));
                String gender = c.getString(c.getColumnIndex(UsersProvider.GENDER));

                etMobileNo.setText(mobile);
                etName.setText(name);

                if (gender.equalsIgnoreCase("male")) {
                    rbMale.setChecked(true);
                }

                if (gender.equalsIgnoreCase("female")) {
                    rbFemale.setChecked(true);
                }

            } while (c.moveToNext());
        } else {
            Toast.makeText(getBaseContext(), "User does not exist.", Toast.LENGTH_LONG).show();
        }
    }


}
