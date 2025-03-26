package com.example.project.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.Usercomment;
import com.example.project.adapters.CommentAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerComments;
    private CommentAdapter commentAdapter;
    private List<Usercomment> commentList = new ArrayList<>();
    private String moodEventId;

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

        //load comments
        loadComments();
        findViewById(R.id.btnPostComment).setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        // search comments
        db.collection("Comments")
                .whereEqualTo("moodEventId", moodEventId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "errors!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // add new data
                    commentList.clear();
                    for (DocumentSnapshot doc:snapshots.getDocuments()) {
                        Usercomment comment = doc.toObject(Usercomment.class);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void postComment() {
        //validation
        String content = ((EditText) findViewById(R.id.editComment)).getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String author = prefs.getString("username", null);
//        if (author == null) {
//            Toast.makeText(this,

        //comment instance
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