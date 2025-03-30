package com.example.project;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

import com.example.project.models.Emotion;
import com.example.project.models.MoodEvent;
import com.example.project.models.SocialSituation;

/**
 * Unit tests for the MoodHistory functionality.
 * This class includes tests for filtering, adding, and clearing mood events.
 */
public class MoodHistoryTests {

    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> filteredList;
    /**
     * Sets up the test environment by initializing moodHistoryList with sample data.
     */
    @Before
    public void setup() {
        moodHistoryList = new ArrayList<>();
        filteredList = new ArrayList<>();
        // Adding sample mood events to the history
        moodHistoryList.add(new MoodEvent(
                Emotion.HAPPINESS,
                new Date(),
                "Feeling good!",
                SocialSituation.ALONE,
                "Home"
        ));

        moodHistoryList.add(new MoodEvent(
                Emotion.SADNESS,
                new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000),
                "Rough day",
                SocialSituation.WITH_ONE_PERSON,
                "Cafe"
        ));

        moodHistoryList.add(new MoodEvent(
                Emotion.ANGER,
                new Date(System.currentTimeMillis() - 10 *24 * 60 * 60 * 1000),
                "Bad service",
                SocialSituation.ALONE,
                "Restaurant"
        ));
    }
    /**
     * Tests the filtering of mood events by the "ANGER" emotion.
     */
    @Test
    public void testFilterByAnger() {
        filteredList.clear();

        for (MoodEvent mood : moodHistoryList) {
            if (mood.getEmotion() == Emotion.ANGER) {
                filteredList.add(mood);
            }
        }

        // Assertions to ensure filtering is correct
        assertEquals(1, filteredList.size());
        assertEquals(Emotion.ANGER, filteredList.get(0).getEmotion());
    }
    /**
     * Tests the filtering of mood events from the past week.
     */
    @Test
    public void testFilterByLastWeek() {
        filteredList.clear();

        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        for (MoodEvent mood : moodHistoryList) {
            if (mood.getDate().getTime() >= oneWeekAgo) {
                filteredList.add(mood);
            }
        }

        // Assertions
        assertEquals(2, filteredList.size());
        for (MoodEvent mood : filteredList) {
            assert mood.getDate().getTime() >= oneWeekAgo;
        }
    }
    /**
     * Tests filtering by a keyword in the reason field of mood events.
     */
    @Test
    public void testFilterByReasonKeyword() {
        filteredList.clear();

        String keyword = "rough";  // Example keyword for testing
        String lowerKeyword = keyword.trim().toLowerCase();

        for (MoodEvent mood : moodHistoryList) {
            String reason = mood.getReason();
            if (reason != null) {
                String lowerReason = reason.toLowerCase();
                String[] words = lowerReason.split("\\s+");

                boolean matchFound = false;

                for (String word : words) {
                    if (word.startsWith(lowerKeyword) || word.contains(lowerKeyword) || word.endsWith(lowerKeyword)) {
                        matchFound = true;
                        break;
                    }
                }

                if (matchFound) {
                    filteredList.add(mood);
                }
            }
        }

        // Assertions
        assertEquals(1, filteredList.size());  // Should find one mood with "Rough day"
        assertEquals("Rough day", filteredList.get(0).getReason());
    }
    /**
     * Tests adding a new mood event to the mood history list.
     */
    @Test
    public void testAddNewMoodEvent() {
        int initialSize = moodHistoryList.size();

        MoodEvent newMood = new MoodEvent(
                Emotion.SURPRISE,
                new Date(),
                "JUnit Add Mood Test",
                SocialSituation.ALONE,
                "Mall"
        );

        moodHistoryList.add(newMood);

        // Assert mood is added
        assertEquals(initialSize + 1, moodHistoryList.size());

        // Verify the last mood added is the one we expect
        MoodEvent addedMood = moodHistoryList.get(moodHistoryList.size() - 1);
        assertEquals("JUnit Add Mood Test", addedMood.getReason());
        assertEquals(Emotion.SURPRISE, addedMood.getEmotion());
        assertEquals(SocialSituation.ALONE, addedMood.getSocialSituation());
    }

    /**
     * Tests clearing the applied filters, restoring the full list of mood events.
     */
    @Test
    public void testClearFilters() {
        // Simulate clear filter by resetting filteredList
        filteredList.clear();
        filteredList.addAll(moodHistoryList);
        // Assert that the filtered list is restored to the full list
        assertEquals(moodHistoryList.size(), filteredList.size());
    }
}
