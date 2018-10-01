package com.example.pirathap.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextConfirm;
    Button btnRegister;
    TextView textView;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = (EditText) findViewById(R.id.RusernameText);
        editTextPassword = (EditText) findViewById(R.id.RpasswordText);
        editTextConfirm = (EditText) findViewById(R.id.RconfirmPasswordText) ;
        btnRegister = (Button) findViewById(R.id.RregisterButton) ;
        textView = (TextView) findViewById(R.id.textViewLogin);
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String cpassword = editTextConfirm.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(getApplicationContext(), "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 8){
                    Toast.makeText(getApplicationContext(), "Enter 8 Characters ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(cpassword)) {
                    Toast.makeText(getApplicationContext(), "Enter Confirm Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!password.equals(cpassword)){
                    Toast.makeText(getApplicationContext(), "Enter Correct Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setMessage("Registering Please Wait");
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(username,password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       progressDialog.dismiss();
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "User Registered Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                SignUpActivity.this.startActivity(loginIntent);
            }
        });
    }
}
