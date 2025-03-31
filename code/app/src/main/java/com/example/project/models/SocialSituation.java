package com.example.project.models;
/**
 * Enum representing different social situations that a person might experience during a mood event.
 * This enum is used to categorize the environment in which the mood event occurs.
 */
public enum SocialSituation {
    None,
    ALONE,
    WITH_ONE_PERSON,
    WITH_TWO_TO_SEVERAL_PEOPLE,
    WITH_A_CROWD;

    /**
     * Static method to get the corresponding {@link SocialSituation} from a given position.
     * This is useful for mapping an index (e.g., from a UI or data source) to the appropriate enum value.
     *
     * @param position The position representing a social situation.
     * @return The corresponding {@link SocialSituation} based on the position. If no match is found, returns {@link SocialSituation#None}.
     */
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