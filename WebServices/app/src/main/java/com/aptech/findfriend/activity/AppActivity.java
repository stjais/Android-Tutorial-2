package com.aptech.findfriend.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.aptech.findfriend.R;
import com.aptech.findfriend.network.RestClient;
import com.aptech.findfriend.interfaces.AsyncHttpPostListener;
import com.aptech.findfriend.interfaces.ProceedAfterPermissionListener;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;


public class AppActivity extends AppCompatActivity {

    // id to handle runtime permissions
    public static final int PERMISSION_CALLBACK_CONSTANT = 100;
    public static final int REQUEST_PERMISSION_SETTING = 101;

    public static String[] permissionsRequired = new String[]{
            //network
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.READ_PHONE_STATE,
            //location
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            //storage
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ProceedAfterPermissionListener proceedAfterPermissionListener;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;

    private AsyncHttpPostListener asyncHttpPostListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //checking permissions status
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

    }

    /**
     * @param title   dialog title
     * @param message dialog message
     */
    public void showDialog(String title, String message) {
        android.app.AlertDialog dialog;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /**
     * @return returns if network is available on device or not
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivityManager != null) {
                //noinspection deprecation
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            Log.d("Network",
                                    "NETWORKNAME: " + anInfo.getTypeName());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     */
    protected void checkAppPermissions() {
        if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[4]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[5]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permissionsRequired[6]) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[0]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[1]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[2]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[3]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[4]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[5]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[6])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_permission_dialog_title);
                builder.setMessage(R.string.app_permission_dialog_details);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(AppActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_permission_dialog_title);
                builder.setMessage(R.string.app_permission_dialog_details);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getApplicationContext(), R.string.app_permission_go_to_settings, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[0]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[1]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[2]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[3]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[4]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[5]) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRequired[6])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_permission_dialog_title);
                builder.setMessage(R.string.app_permission_dialog_details);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(AppActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.app_permission_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * @param listener proceedAfterPermissionListener
     */
    public void proceedAfterPermissionListener(ProceedAfterPermissionListener listener) {
        proceedAfterPermissionListener = listener;
    }

    /**
     *
     */
    protected void proceedAfterPermission() {
        if (proceedAfterPermissionListener != null)
            proceedAfterPermissionListener.proceedAfterPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @param url
     * @param keys
     * @param values
     * @throws Exception
     */
    public void callWebService(String url, ArrayList<String> keys, ArrayList<String> values, RestClient.RequestMethod REQUEST_METHOD) throws Exception {
        if (isConnectedToInternet(AppActivity.this)) {
            AsyncHttpPost asyncHttpPost = new AsyncHttpPost(keys, values, REQUEST_METHOD);
            asyncHttpPost.execute(url);
        } else {
            showDialog("Error", "No Internet Connection");
        }
    }

    /**
     * @param listener asyncHttpPostListener
     */
    public void asyncHttpPostListener(AsyncHttpPostListener listener) {
        asyncHttpPostListener = listener;
    }

    /**
     * @param result asyncHttpPostListener
     */
    protected void asyncHttpPostListener(String result) {
        if (asyncHttpPostListener != null)
            asyncHttpPostListener.asyncHttpPostListener(result);
    }

    /**
     *
     */
    private class AsyncHttpPost extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        ArrayList<String> keys, values;
        RestClient.RequestMethod REQUEST_METHOD;
        String result;

        AsyncHttpPost(ArrayList<String> keys, ArrayList<String> values, RestClient.RequestMethod REQUEST_METHOD) {
            this.keys = keys;
            this.values = values;
            this.REQUEST_METHOD = REQUEST_METHOD;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AppActivity.this);
            progressDialog.setMessage("Please wait while loading...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                RestClient restClient = new RestClient(params[0], keys, values);
                result = restClient.execute(REQUEST_METHOD);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonResponseObject = new JSONObject(result);
                int responseCode = jsonResponseObject.getInt("responseCode");
                String responseMessage = jsonResponseObject.getString("responseMessage");

                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        asyncHttpPostListener(responseMessage);
                        break;
                    default:
                        showDialog(getResources().getString(R.string.str_error_message), getResources().getString(R.string.str_connection_error_message) + responseCode);
                }

            } catch (Exception exception) {
                showDialog(getResources().getString(R.string.str_error_message), getResources().getString(R.string.str_unknown_error_message));
            }
        }
    }

}
