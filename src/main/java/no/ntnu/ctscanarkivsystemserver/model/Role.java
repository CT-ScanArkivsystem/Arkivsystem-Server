package no.ntnu.ctscanarkivsystemserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@Entity(name = "Roles")
@Data
@NoArgsConstructor
public class Role {

    public static final String ADMIN = "admin";
    public static final String PROFESSOR = "professor";
    public static final String USER = "user";

    @Id
    @Column(name = "role_name")
    private String roleName;
}