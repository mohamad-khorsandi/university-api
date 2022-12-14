package ir.sobhan.business.SecurityService;

import lombok.Getter;

@Getter
public enum Role {
    STUDENT, ADMIN, INSTRUCTOR;

    Role() {
        ROLE_str = "ROLE_" + this;
    }

    private final String ROLE_str;

    public String getStr() {
        return this.toString();
    }
}