package com.example.project.activities;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.project.R;
import com.example.project.activities.AddingMoodActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.After;  // Added to handle logout after the test
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class AddingMoodActivityTest {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Before
    public void loginWithFirebaseAuth() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        // Look up email by username
        db.collection("users")
                .whereEqualTo("username", "test_user_jt")  // Replace with the entered username
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        //  Get email from Firestore
                        String email = task.getResult().getDocuments().get(0).getString("email");

                        //  Log in with email and password
                        auth.signInWithEmailAndPassword("test_user_jt@moodapp.com", "LandWhale921")  // Corrected email
                                .addOnCompleteListener(loginTask -> {
                                    if (!loginTask.isSuccessful()) {
                                        throw new RuntimeException("Firebase login failed", loginTask.getException());
                                    }
                                    latch.countDown();
                                });
                    } else {
                        throw new RuntimeException("Username not found in Firestore");
                    }
                });

        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("Login timeout");
        }
    }

    @Test
    public void testAddRealisticMood() {
        ActivityScenario.launch(AddingMoodActivity.class);

        onView(withId(R.id.emotionSpinner)).perform(click());
        onData(anything()).atPosition(2).perform(click());

        onView(withId(R.id.reasonEditText)).perform(typeText("Had a great mocha"), closeSoftKeyboard());
        onView(withId(R.id.locationEditText)).perform(typeText("Tim Hortons, Edmonton"), closeSoftKeyboard());

        onView(withId(R.id.submitButton)).perform(click());
    }

    @After
    public void logoutFromFirebase() {
        // Log out to prevent repeated logins causing issues
        auth.signOut();
    }
}
