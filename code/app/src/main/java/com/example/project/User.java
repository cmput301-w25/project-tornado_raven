package com.example.project;

/**
 * Represents a User with a username and password.
 * This class is used to manage user credentials.
 */
public class User {
    private String username; // The username of the user
    private String password; // The password of the user

    /**
     * Gets the username of the user.
     *
     * @return The username as a {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the user.
     *
     * @param username The username to be set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user.
     *
     * @return The password as a {@link String}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the user.
     *
     * @param password The password to be set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}

