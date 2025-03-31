package com.example.project.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.models.Usercomment;
import com.example.project.adapters.CommentAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Activity that displays and posts comments related to a specific mood event.
 * It connects to Firestore to retrieve and store comments.
 */
public class CommentActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerComments;
    private CommentAdapter commentAdapter;
    private List<Usercomment> commentList = new ArrayList<>();
    private String moodEventId;



    /**
     * Called when the activity is first created.
     * Initializes the activity and sets up views and data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        db = FirebaseFirestore.getInstance();
        moodEventId = getIntent().getStringExtra("MOOD_EVENT_ID"); // intent from commonspace
        recyclerComments = findViewById(R.id.recyclerComments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerComments.setAdapter(commentAdapter);

        loadComments();
        findViewById(R.id.btnPostComment).setOnClickListener(v -> postComment());
    }


    /**
     * Loads comments from Firestore where the moodEventId matches the current mood event.
     * Updates the RecyclerView with the retrieved comments in real-time using a snapshot listener.
     */
    private void loadComments() {
        // search comments
        db.collection("Comments")
                .whereEqualTo("moodEventId", moodEventId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "errors!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    commentList.clear();
                    for (DocumentSnapshot doc:snapshots.getDocuments()) {
                        Usercomment comment = doc.toObject(Usercomment.class);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }


    /**
     * Posts a new comment to Firestore after validating that the comment is not empty.
     * Uses SharedPreferences to retrieve the author username and clears the input on success.
     */
    private void postComment() {
        String content = ((EditText) findViewById(R.id.editComment)).getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String author = prefs.getString("username", null);

        Usercomment comment = new Usercomment();
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setMoodEventId(moodEventId);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setTimestamp(new Date());

        db.collection("Comments")
                .document(comment.getCommentId())
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    //type bar clear
                    ((EditText) findViewById(R.id.editComment)).setText("");
                    Toast.makeText(this, "posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "errors!", Toast.LENGTH_SHORT).show());
    }
}