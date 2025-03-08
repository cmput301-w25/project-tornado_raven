package com.example.project;

import java.util.ArrayList;
import java.util.List;

/**
 * A naive global store for user-followed moods.
 * In a real production app, you'd store these in a DB or ViewModel.
 */
public class GlobalData {
    public static List<String> followedMoods = new ArrayList<>();
}

