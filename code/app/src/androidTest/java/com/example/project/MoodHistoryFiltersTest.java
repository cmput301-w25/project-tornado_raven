package com.example.project;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MoodHistoryFiltersTest {

    private List<MoodEvent> moodHistoryList;
    private List<MoodEvent> filteredList;

    @Before
    public void setup() {
        moodHistoryList = new ArrayList<>();
        filteredList = new ArrayList<>();

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

    @Test
    public void testFilterByAnger() {
        filteredList.clear();

        for (MoodEvent mood : moodHistoryList) {
            if (mood.getEmotion() == Emotion.ANGER) {
                filteredList.add(mood);
            }
        }

        // Assertions
        assertEquals(1, filteredList.size());
        assertEquals(Emotion.ANGER, filteredList.get(0).getEmotion());
    }

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


    @Test
    public void testClearFilters() {
        // Simulate clear filter by resetting filteredList
        filteredList.clear();
        filteredList.addAll(moodHistoryList);

        assertEquals(moodHistoryList.size(), filteredList.size());
    }
}
