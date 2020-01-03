package app.models.dto;

import java.io.Serializable;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import app.models.entity.User;

public class UserDto implements Serializable {

    private static final long serialVersionUID = -4512071323586705398L;

    private static final int MAX_CHARS = 100;

    private static final int MIN_CHARS = 3;

    private Long id;

    @NotNull
    @Size(min = MIN_CHARS, max = MAX_CHARS, message = "Username must at least 3 characters.")
    private String userName;

    @NotNull
    @Size(min = MIN_CHARS, max = MAX_CHARS, message = "Password must at least 3 characters.")
    private String password;

    @Transient
    private String confirmPassword;

    @Email(message = "Email address is not valid.")
    @NotNull
    private String email;

    private String token;

    private String role = "ROLE_USER";

    private String firstName;

    private String lastName;

    private String address;

    private String companyName;

    private String lastLogin;

    private String profilePicture;

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(final String name) {

        this.userName = name;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    public String getConfirmPassword() {

        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {

        this.confirmPassword = confirmPassword;
    }

    public String getToken() {

        return token;
    }

    public void setToken(final String token) {

        this.token = token;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    public String getRole() {

        return role;
    }

    public void setRole(final String role) {

        this.role = role;
    }

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName(final String firstName) {

        this.firstName = firstName;
    }

    public String getLastName() {

        return lastName;
    }

    public void setLastName(final String lastName) {

        this.lastName = lastName;
    }

    public String getAddress() {

        return address;
    }

    public void setAddress(final String address) {

        this.address = address;
    }

    public String getCompanyName() {

        return companyName;
    }

    public void setCompanyName(final String companyName) {

        this.companyName = companyName;
    }

    public String getLastLogin() {

        return lastLogin;
    }

    public void setLastLogin(final String lastLogin) {

        this.lastLogin = lastLogin;
    }

    public String getProfilePicture() {

        return profilePicture;
    }

    public void setProfilePicture(final String profilePicture) {

        this.profilePicture = profilePicture;
    }

    public boolean isAdmin() {

        return "ROLE_ADMIN".equals(this.role);
    }

    public boolean isMatchingPasswords() {

        return this.password.equals(this.confirmPassword);
    }

    public User toEntity() {

        final User user = new User();
        user.setUserName(this.getUserName());
        user.setPassword(this.getPassword());
        user.setConfirmPassword(this.getConfirmPassword());
        user.setEmail(this.getEmail());
        user.setToken(this.getToken());
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setAddress(this.getAddress());
        user.setCompanyName(this.getCompanyName());
        user.setProfilePicture(this.getProfilePicture());
        return user;
    }
}
