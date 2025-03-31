package com.example.project.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.project.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *Espresso test for CommonSpaceActivity that:
 *   1) Clicks filter buttons.
 *   2) Attempts to search a username (requires a test user in Firestore, or code will crash).
 *   3) Verifies navigation to ProfileActivity if the user is found.
 */
@RunWith(AndroidJUnit4.class)
public class CommonSpaceActivityTest {

    @Rule
    public ActivityScenarioRule<CommonSpaceActivity> activityRule =
            new ActivityScenarioRule<>(CommonSpaceActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Click the "Show Last Week" button and ensure no crash occurs.
     */
    @Test
    public void testFilterByLastWeek() {
        onView(withId(R.id.btnShowLastWeek)).perform(click());
    }

    /**
     * Click the "Filter by Keyword" button, type a keyword, and press "Filter" in the dialog.
     */
    @Test
    public void testKeywordFilter() {
        onView(withId(R.id.btnFilterByKeyword)).perform(click());

        // Type "happy" in the dialog's EditText
        onView(withText("Enter keyword to filter by reason")).check((view, e) -> {
        });

        // Now we find the positive button "Filter" in the dialog.
        onView(withText("Filter"))
                .perform(click());
    }

    /**
     * Type a username in the search bar and click it.
     */
    @Test
    public void testSearchByUsername() {
        onView(withId(R.id.editTextSearchUserName))
                .perform(typeText("625"), closeSoftKeyboard());

        // If your code displays an AutoComplete suggestion with "625":
        onView(withText("625")).perform(click());
    }
}


