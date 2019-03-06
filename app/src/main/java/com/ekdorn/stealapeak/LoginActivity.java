package com.ekdorn.stealapeak;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inner_login);

        final Button loginButton = (Button) findViewById(R.id.button);
        final EditText number = (EditText) findViewById(R.id.phone);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = number.getText().toString();
                if (phone.matches("\\+[0-9]([ -])?[0-9]{3}([ -])?[0-9]{3}([ -])?[0-9]{2}([ -])?[0-9]{2}")) {
                    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            signInWithPhoneAuthCredential(credential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Log.e("TAG", "onVerificationFailed: wrong creditional");
                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                Log.e("TAG", "onVerificationFailed: too many SMSs");
                            } else {
                                Log.e("TAG", "onVerificationFailed: don't give a fuck actually: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                            ConfirmationDialog cd = new ConfirmationDialog(LoginActivity.this, verificationId);
                            cd.show();
                            loginButton.setEnabled(false);
                        }
                    };


                    PhoneAuthProvider.getInstance().verifyPhoneNumber (
                            phone,        // Phone number to verify
                            60L,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            LoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                } else {
                    number.setError("not your phone :/");
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() +
                                            ":" /*+ KEY */))
                                    .build();

                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            LoginActivity.this.setResult(RESULT_OK);
                                            finish();
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Sorry, not logged in...", Toast.LENGTH_SHORT).show();
                            Button loginButton = (Button) findViewById(R.id.button);
                            loginButton.setEnabled(true);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        LoginActivity.this.setResult(RESULT_CANCELED);
        finish();
    }



    private class ConfirmationDialog extends AlertDialog {
        private ConfirmationDialog(@NonNull Context context, final String id) {
            super(context);

            this.setCancelable(false);
            this.setCanceledOnTouchOutside(false);

            this.setMessage("Insert your 6-digit code from SMS :");

            final EditText code = new EditText(context);
            code.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            code.setInputType(InputType.TYPE_CLASS_NUMBER);
            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 6) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, editable.toString());
                        signInWithPhoneAuthCredential(credential);
                        ConfirmationDialog.this.dismiss();
                    }
                }
            });
            this.setView(code);
        }
    }
}
