package edu.njit.quill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import edu.njit.quill.model.Journal;
import edu.njit.quill.ui.JournalRecyclerAdapter;
import edu.njit.quill.util.JournalApi;

public class JournalListActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private static final String JOURNAL = "journal";
    private FloatingActionButton popUpButton;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;
    private CollectionReference collectionReference = db.collection(JOURNAL);
    private TextView noJournalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        popUpButton = findViewById(R.id.popup_button);

        popUpButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        journalList = new ArrayList<>();
        noJournalTextView = findViewById(R.id.list_no_items_txt);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            noJournalTextView.setVisibility(View.INVISIBLE);
                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                journalList.add(snapshot.toObject(Journal.class));
                            }

                            journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this, journalList);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            journalRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            noJournalTextView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
