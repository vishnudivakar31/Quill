package edu.njit.quill;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
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

import edu.njit.quill.util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String JOURNAL = "journal";
    private static final int CAMERA_CODE = 1;
    private static final int GALLERY_CODE = 2;
    private static final String[] IMAGE_OPS = new String[] {
            "Gallery",
            "Camera",
            "Cancel"
    };
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton, journalImageView;
    private EditText titleEditText, thoughtEditText;
    private TextView usernameTextView, todayTextView;
    private AlertDialog dialog;

    private String currentUserId;
    private String currentUsername;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection(JOURNAL);
    private Uri imageUri;

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

        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        JournalApi journalApi = JournalApi.getInstance();
        currentUsername = journalApi.getUsername();
        currentUserId = journalApi.getUserId();
        usernameTextView.setText(String.format("%s%s", currentUsername.substring(0, 1).toUpperCase(),
                currentUsername.substring(1).toLowerCase()));

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {

                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_save_button:
                break;
            case R.id.post_camera_button:
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.journal_image_dialog_view, null);
                dialogView.findViewById(R.id.camera_cardview).setOnClickListener(this);
                dialogView.findViewById(R.id.gallery_cardview).setOnClickListener(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(PostJournalActivity.this);
                builder.setView(dialogView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.camera_cardview:
                dialog.dismiss();
                setCameraIntent();
                break;
            case R.id.gallery_cardview:
                dialog.dismiss();
                setGalleryIntent();
                break;
        }
    }

    private void setCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_CODE);
        }
    }

    private void setGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                Bundle bundle = data.getExtras();
                Bitmap imageBitmap = (Bitmap) bundle.get("data");
                journalImageView.setImageBitmap(imageBitmap);
            }
        } else if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                imageUri = data.getData();
                journalImageView.setImageURI(imageUri);
            }
        }
    }
}
