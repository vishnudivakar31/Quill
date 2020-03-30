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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import edu.njit.quill.model.Journal;
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
    private static final String JOURNAL_IMAGES = "journal_images";
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
        storageReference = FirebaseStorage.getInstance().getReference();

        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        JournalApi journalApi = JournalApi.getInstance();
        currentUsername = journalApi.getUsername();
        currentUserId = journalApi.getUserId();
        usernameTextView.setText(String.format("%s%s", currentUsername.substring(0, 1).toUpperCase(),
                currentUsername.substring(1).toLowerCase()));

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        todayTextView.setText(df.format(new Date(System.currentTimeMillis())));

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
                saveJournal();
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

    private void saveJournal() {
        final String title = titleEditText.getText().toString().trim();
        final String thought = thoughtEditText.getText().toString().trim();
        progressBar.setVisibility(ProgressBar.VISIBLE);

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null) {
            final StorageReference filePath = storageReference
                    .child(JOURNAL_IMAGES)
                    .child("my_image" + Timestamp.now().getSeconds());
            filePath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thought);
                                    journal.setImageUrl(uri.toString());
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUsername(currentUsername);
                                    journal.setUserId(currentUserId);

                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                    Intent intent = new Intent(PostJournalActivity.this, JournalListActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                    Snackbar.make(findViewById(R.id.post_journal_activity), R.string.failure_message_txt, Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            Snackbar.make(findViewById(R.id.post_journal_activity), R.string.failure_message_txt, Snackbar.LENGTH_SHORT).show();
                        }
                    });

        } else {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            Snackbar.make(findViewById(R.id.post_journal_activity), R.string.mandatory_warning, Snackbar.LENGTH_SHORT).show();
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
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, "my_image_" + Timestamp.now().getSeconds(), null);
                imageUri = Uri.parse(path);
            }
        } else if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if(data != null) {
                imageUri = data.getData();
                journalImageView.setImageURI(imageUri);
            }
        }
    }
}
