package no.ntnu.ctscanarkivsystemserver.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "Roles")
@Data
@NoArgsConstructor
public class Role {

    public static final String ADMIN = "ADMIN";
    public static final String ACADEMIC = "ACADEMIC";
    public static final String USER = "USER";

    @Id
    @Column(name = "role_name")
    private String roleName;
}