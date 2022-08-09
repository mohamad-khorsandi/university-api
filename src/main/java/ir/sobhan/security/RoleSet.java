package ir.sobhan.security;

import lombok.Getter;

import static ir.sobhan.security.Role.*;
import static ir.sobhan.security.Role.STUDENT;

@Getter
public enum RoleSet{
    SA(STUDENT, ADMIN), IA(INSTRUCTOR, ADMIN), SIA(STUDENT, INSTRUCTOR, ADMIN), I(INSTRUCTOR), A(ADMIN), S(STUDENT);

    private final String[] roles;

    RoleSet(Role... roleEnums) {
        roles = new String[roleEnums.length];

        for (int i = 0; i < roleEnums.length; i++) {
            this.roles[i] = roleEnums[i].getStr();
        }
    }
}