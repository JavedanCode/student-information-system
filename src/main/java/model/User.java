package model;

public class User {

    private String username;
    private String password;
    private Role role;
    private String fullName;
    private String referenceId;

    public User(String username, String password, Role role, String fullName, String referenceId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.referenceId = referenceId;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getReferenceId() { return referenceId; }

    // File format: username,password,role,fullName,referenceId
    public String toFileString() {
        return username + "," + password + "," + role + "," + fullName + "," + referenceId;
    }

    public static User fromFileString(String line) {
        String[] parts = line.split(",");
        return new User(
                parts[0],
                parts[1],
                Role.valueOf(parts[2]),
                parts[3],
                parts[4]
        );
    }
}