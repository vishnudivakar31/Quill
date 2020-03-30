package edu.njit.quill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import edu.njit.quill.util.JournalApi;


public class LoginActivity extends AppCompatActivity {

    private static final String USERS = "users";
    private Button loginButton, createAccountButton;
    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(USERS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.emailSignInBtn);
        createAccountButton = findViewById(R.id.loginCreateAccoutBtn);
        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.login_progress);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmailPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        });
    }

    private void loginEmailPassword(String email, String pwd) {
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            auth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = auth.getCurrentUser();
                            final String currentUserId = user.getUid();

                            collectionReference
                                    .whereEqualTo("user_id", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if(e == null) {
                                                assert queryDocumentSnapshots != null;
                                                if(!queryDocumentSnapshots.isEmpty()) {
                                                    for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                        JournalApi journalApi = JournalApi.getInstance();
                                                        journalApi.setUsername(snapshot.getString("username"));
                                                        journalApi.setUserId(currentUserId);

                                                        startActivity(new Intent(LoginActivity.this,
                                                                PostJournalActivity.class));
                                                        finish();
                                                    }
                                                }
                                            } else {
                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                Snackbar.make(findViewById(R.id.login_activitiy), R.string.invalid_user, Snackbar.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            Snackbar.make(findViewById(R.id.login_activitiy), R.string.invalid_user, Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Snackbar.make(findViewById(R.id.login_activitiy), R.string.mandatory_warning, Snackbar.LENGTH_SHORT).show();
        }
    }
}
