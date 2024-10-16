// User.java
package com.example.pookies;

public class User {
    private int id;
    private String email;
    private String name;
    private String password;

    public User(int id, String email, String name, String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPassword() { return password; }
}
