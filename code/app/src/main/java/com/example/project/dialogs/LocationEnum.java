package com.example.project;

/**
 * Enum to choose if we we want to add a location
 */
public enum LocationEnum {
    YES,
    NO;

    public static LocationEnum fromPosition(int position) {
        switch (position) {
            case 0:
                return NO;
            case 1:
                return YES;
            default:
                return NO; // Default
        }
    }
}