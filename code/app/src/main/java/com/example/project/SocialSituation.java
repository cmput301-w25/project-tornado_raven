package com.example.project;

public enum SocialSituation {
    None,
    ALONE,
    WITH_ONE_PERSON,
    WITH_TWO_TO_SEVERAL_PEOPLE,
    WITH_A_CROWD;

    // Static method to get the enum value from a position
    public static SocialSituation fromPosition(int position) {
        switch (position) {
            case 1:
                return ALONE;
            case 2:
                return WITH_ONE_PERSON;
            case 3:
                return WITH_TWO_TO_SEVERAL_PEOPLE;
            case 4:
                return WITH_A_CROWD;
            default:
                return None;
        }
    }
}