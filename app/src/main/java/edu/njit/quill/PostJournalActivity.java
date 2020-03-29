package edu.njit.quill;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class PostJournalActivity extends AppCompatActivity {

    private static final String JOURNAL = "journal";
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton, journalImageView;
    private EditText titleEditText, thoughtEditText;
    private TextView usernameTextView, todayTextView;

    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection(JOURNAL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.post_progressbar);
        titleEditText = findViewById(R.id.post_title_et);
        thoughtEditText = findViewById(R.id.post_thought_et);
        usernameTextView = findViewById(R.id.post_username_txt);
        todayTextView = findViewById(R.id.post_date_txt);
        addPhotoButton = findViewById(R.id.post_camera_button);
        journalImageView = findViewById(R.id.post_camera_background);
        saveButton = findViewById(R.id.post_save_button);
    }
}
