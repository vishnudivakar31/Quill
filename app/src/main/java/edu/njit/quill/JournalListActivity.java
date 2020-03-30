package edu.njit.quill;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class JournalListActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private FloatingActionButton popUpButton;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        popUpButton = findViewById(R.id.popup_button);

        popUpButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.popup_button:
                showPopupMenu();
        }
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, popUpButton);
        popupMenu.inflate(R.menu.menu);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_add:
                if(firebaseUser != null && auth != null) {
                    startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                }
                break;
            case R.id.action_signout:
                if(firebaseUser != null && auth != null) {
                    auth.signOut();
                    startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
