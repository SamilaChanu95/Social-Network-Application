package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.TextUtilsCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidPassword(UserPassword.getText().toString().trim())) {
                    Toast.makeText(RegisterActivity.this, "Valid", Toast.LENGTH_SHORT).show();
                    CreateNewAccount();
                } else {
                    Toast.makeText(RegisterActivity.this, "Invalid and one digit,uppercase letter,lowercase letter,special character must occur at least and no white space and at least six characters.", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    public static boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9]+(\\.[a-z0-9]+)*(\\.[a-z]{3})$";
        final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        final Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    private void CreateNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Please enter your email address...", Toast.LENGTH_SHORT).show();

        }
        if (!isEmailValid(email)){

            Toast.makeText(this,"Your email is invalid...", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please enter your password...", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(this, "Please confirm your password...", Toast.LENGTH_SHORT).show();

        }
        else if(!password.equals(confirmPassword)) {

            Toast.makeText(this, "Your password don't match with your confirm password.", Toast.LENGTH_SHORT).show();

        }
        else {

            loadingBar.setTitle("Creating new account.");
            loadingBar.setMessage("Please wait, while we are creating your new account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {

                        SendUserToSetupActivity();

                        Toast.makeText(RegisterActivity.this, "You are authenticated successfully...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else {

                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error occured: "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }

    }

    private void SendUserToSetupActivity() {
                                        //dan inna activity eka  //data pass krnn oni activity eka
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //using the validation part
        startActivity(setupIntent);
        finish();
    }
}
