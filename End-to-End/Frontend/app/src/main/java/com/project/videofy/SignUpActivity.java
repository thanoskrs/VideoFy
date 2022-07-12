package com.project.videofy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.project.videofy.databinding.SignupActivityBinding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SignUpActivity extends AppCompatActivity {

    private SignupActivityBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogInActivity.activities.add(this);

        binding = SignupActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.white));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_image, null);
        getSupportActionBar().setCustomView(v);

        signUp();
    }

    protected void signUp() {
        binding.infoLogoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.helpTextSignUp.setVisibility((binding.helpTextSignUp.getVisibility() == View.VISIBLE)
                        ? View.GONE : View.VISIBLE);
            }
        });

        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                EditText usernameET = binding.etUsernameLayout.getEditText();
                EditText passwordET = binding.etPasswordLayout.getEditText();

                String username = binding.InputTextUsername.getText().toString();
                String password = binding.InputTextPassword.getText().toString();
                String repeatPassword = binding.repeatInputTextPassword.getText().toString();


                if (usernameET.length() == 0 ) {
                    binding.etUsernameLayout.setError("Enter username.");
                } else  {
                    binding.etUsernameLayout.setError(null);
                    if (passwordET.length() == 0 ) {
                        binding.etPasswordLayout.setError("Enter password.");
                    } else if (passwordET.length() < 8) {
                        binding.etPasswordLayout.setError("At least 8 characters.");
                    } else if (passwordET.length() > 15) {
                        binding.etPasswordLayout.setError("At most 15 characters.");
                    } else {
                        binding.etPasswordLayout.setError(null);
                        if (!password.equals(repeatPassword)) {
                            binding.etRepeatPasswordLayout.setError("Passwords don't match");
                        } else {
                            binding.etRepeatPasswordLayout.setError(null);

                            SignUp signUp = new SignUp();

                            String[] params = new String[2];
                            params[0] = username;
                            params[1] = password;

                            signUp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                        }
                    }
                }
            }
        });
    }

    private class SignUp extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            String username = strings[0];
            String password = strings[1];

            boolean valid = false;

            try {
                Socket connectToMS = new Socket(LogInActivity.MainServerIp, LogInActivity.MainServerPort);
                ObjectOutputStream outputStream = null;
                outputStream = new ObjectOutputStream(connectToMS.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(connectToMS.getInputStream());

                outputStream.writeUTF("Appnode");
                outputStream.flush();

                outputStream.writeUTF("SIGN UP");
                outputStream.flush();

                outputStream.writeUTF(username);
                outputStream.flush();

                valid = (boolean) inputStream.readObject();

                outputStream.writeUTF(password);
                outputStream.flush();

                outputStream.close();
                inputStream.close();
                connectToMS.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return valid;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            Toast toast;
            if(s) {

                toast = Toast.makeText(SignUpActivity.this, "Successfully Signed Up!", Toast.LENGTH_SHORT);
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
            } else {
                toast = Toast.makeText(SignUpActivity.this, "User already exists.", Toast.LENGTH_SHORT);
            }

            toast.setGravity(Gravity.CENTER, 0, 600);
            toast.show();
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        LogInActivity.activities.remove(this);
    }

}