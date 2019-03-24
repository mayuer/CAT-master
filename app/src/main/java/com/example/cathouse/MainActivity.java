package com.example.cathouse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Toast;
import android.text.TextUtils;
import android.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;




public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Feedback feedback;
    private Button loginButton;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * If the user just registered an account from Register.class,
         * the parcelable should be retrieved
         */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Retrieve the parcelable
            Feedback feedback = bundle.getParcelable("feedback");
            // Get the from the object
            String userName = feedback.getName();
            TextView display = findViewById(R.id.display);
            display.setVisibility(View.VISIBLE);
            String prompt = userName.substring(0, 1).toUpperCase() + userName.substring(1) + " " + getString(R.string.account_created);
            display.setText(prompt);

        }

        inputEmail = findViewById(R.id.emaillogin);
        inputPassword = findViewById(R.id.passwordlogin);
        loginButton = findViewById(R.id.Login);


        /**
         * Prepare the dialog to display when the login button is pressed
         */


//    progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);


        /**
         * Use the SessionManager class to check whether
         * the user already logged in, is yest  then go to the MainActivity
         */

    }


    /**
     * Process the user input and log in if credentials are correct
     * Disable the button while login is processing
     *
     * @param view from activity_login.xml
     */


    public void btnLogin(View view) {


        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        context = MainActivity.this;


        // Check for empty data in the form
        if ((!email.isEmpty()) && (!password.isEmpty())) {

            // Avoid multiple clicks on the button
            loginButton.setClickable(false);

            //Todo : ensure the user has Internet connection
            if (IsConnection(context)) {
//                progressDialog.setMessage("Logging in ...");
//                if (!progressDialog.isShowing())
//                    progressDialog.show();

                //Todo: need to check weather the user has Internet before attempting checking the data
                // Start fetching the data from the Internet
                new OnlineCredentialValidation().execute(email, password);
                InternetInformation();
            } else {
                SharedPreferences share = context.getSharedPreferences("share", MODE_PRIVATE);
                String UserName = share.getString("username", "");
                String PassWord = share.getString("password", "");
                if (TextUtils.isEmpty(UserName)) {
                    Toast.makeText(context, "Must access online first time ", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(PassWord)) {
                    Toast.makeText(context, "Must access online first time ", Toast.LENGTH_SHORT).show();
                    return;
                } else if ((!UserName.equals(email)) || (!PassWord.equals(password))) {
                    Toast.makeText(context, "ERROR.Please resume load the email and the password ", Toast.LENGTH_SHORT).show();
                } else {
                    feedback = new Feedback();
                    feedback.setName(email);
                    Intent intent = new Intent(getApplication(), CatActivity.class);
                    intent.putExtra("feedback", feedback);
                    startActivity(intent);
                    finish();
                }
            }

            // Display the progress Dialog


        } else {
            // Prompt user to enter credentials
            Toast.makeText(getApplicationContext(),
                    R.string.enter_credentials, Toast.LENGTH_LONG)
                    .show();
        }
    }


    /**
     * Press the button register, go to Registration form
     */
    public void btnRegister(View view) {
        startActivity(new Intent(getApplicationContext(), Register.class));
        finish();
    }

/**
 * Use the email and password provided to log the user in if the credentials are valid
 */


class OnlineCredentialValidation extends AsyncTask<String, Void, Integer> {

    @Override
    protected Integer doInBackground(String... strings) {
        feedback = new Feedback();

        String response = null;
        OutputStreamWriter request = null;
        int parsingFeedback = feedback.FAIL;


        // Variables
        final String BASE_URL = new Config().getLoginUrl();
        final String EMAIL = "email";
        final String PASSWORD = "password";
        final String PARAMS = EMAIL + "=" + strings[0] + "&" + PASSWORD + "=" + strings[1];
        Log.d("TAG", "Email and Pass - " + EMAIL + "=" + strings[0] + "&" + PASSWORD + "=" + strings[1]);

        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(BASE_URL);
            connection = (HttpURLConnection) url.openConnection();
            //Set the request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(15000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(15000);

            // Output the stream to the server
            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(PARAMS);
            request.flush();
            request.close();

            // Get the inputStream using the same connection
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            response = readStream(inputStream, 500);
            inputStream.close();

            // Parsing the response
            parsingFeedback = parsingResponse(response);


        } catch (MalformedURLException e) {
            Log.e("TAG", "URL - " + e);
            feedback.setError_message(e.toString());
            return feedback.FAIL;
        } catch (IOException e) {
            Log.e("TAG", "openConnection() - " + e);
            feedback.setError_message(e.toString());
            return feedback.FAIL;
        } finally {
            if (connection != null) // Make sure the connection is not null before disconnecting
                connection.disconnect();
            Log.d("TAG", "Response " + response);

            return parsingFeedback;
        }
    }


    @Override
    protected void onPostExecute(Integer mFeedback) {
        super.onPostExecute(mFeedback);
//        if (progressDialog.isShowing()) progressDialog.dismiss();

        if (mFeedback == feedback.SUCCESS) {
            // Update the session
            // Move the user to MainActivity and pass in the User name which was form the server
            Intent intent = new Intent(getApplication(), CatActivity.class);
            intent.putExtra("feedback", feedback);
            startActivity(intent);
        } else {
            // Allow the user to click the button
            loginButton.setClickable(true);
            Toast.makeText(getApplication(), feedback.getError_message(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    String readStream(InputStream stream, int maxReadSize)
            throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }

        Log.d("TAG", buffer.toString());
        return buffer.toString();
    }
}


    /**
     * Parsing the string response from the Server
     *
     * @param response
     * @return
     */
    public int parsingResponse(String response) {

        try {
            JSONObject jObj = new JSONObject(response);
            /**
             * If the registration on the server was successful the return should be
             * {"error":false}
             * Else, an object for error message is added
             * Example: {"error":true,"error_msg":"Invalid email format."}
             * Success of the registration can be checked based on the
             * object error, where true refers to the existence of an error
             */
            boolean error = jObj.getBoolean("error");

            if (!error) {
                //No error, return from the server was {"error":false}
                JSONObject user = jObj.getJSONObject("user");
                String email = user.getString("email");
                feedback.setName(email);
                return feedback.SUCCESS;
            } else {
                // The return contains error messages
                String errorMsg = jObj.getString("error_msg");
                Log.d("TAG", "errorMsg : " + errorMsg);
                feedback.setError_message(errorMsg);
                return feedback.FAIL;
            }
        } catch (JSONException e) {
            feedback.setError_message(e.toString());
            return feedback.FAIL;
        }

    }

    //judge if it id connection to the Internet
    public boolean IsConnection(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            return false;
        }
    }

    //the data last the stored online
    public void InternetInformation() {
        SharedPreferences sharedpreferences = context.getSharedPreferences("share", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        editor.putString("username", email);
        editor.putString("password", password);
        //commit
        editor.commit();
    }
}
