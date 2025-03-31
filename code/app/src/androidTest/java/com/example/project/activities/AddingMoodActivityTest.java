package com.example.project.activities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.project.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class AddingMoodActivityTest {

    @Before
    public void setUpFakeLogin() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("username", "test_user").apply();
    }

    @Test
    public void testAddMoodForm_SubmissionFlow() {
        ActivityScenario.launch(AddingMoodActivity.class);

        // Select emotion from spinner
        onView(withId(R.id.emotionSpinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Enter reason and location
        onView(withId(R.id.reasonEditText)).perform(typeText("Espresso Mood Test"), closeSoftKeyboard());
        onView(withId(R.id.locationEditText)).perform(typeText("Test City"), closeSoftKeyboard());

        // Submit the mood event
        onView(withId(R.id.submitButton)).perform(click());
    }
}
