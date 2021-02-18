package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.User;

import java.util.List;
import java.util.UUID;

public interface ProjectDao {
    Project createProject(Project newProject);

    boolean doesNameExist(String name);
}
