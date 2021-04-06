package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.File;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * The job of this class is to access the file database to get, remove or edit data.
 * @author TrymV
 */
@Repository("fileRepo")
public class FileDao {

    @PersistenceContext
    EntityManager em;

    /**
     * Gets a file from the database by file name and project id.
     * @param fileName name of file to get.
     * @param projectId id of project file is associated with.
     * @return file with fileName and projectId like the params. Null if no file was found.
     * @throws IllegalArgumentException if fileName is empty or projectId is null.
     */
    public File getFileByNameAndProject(String fileName, UUID projectId) throws IllegalArgumentException {
        Query query = em.createNamedQuery(File.FIND_FILE_BY_NAME_AND_PROJECT);
        if(fileName == null || fileName.trim().isEmpty() || projectId == null) {
            throw new IllegalArgumentException("fileName is empty or projectId is null");
        }
        query.setParameter("fileName", fileName).setParameter("projectId", projectId);
        List<File> queryResult = query.getResultList();
        if(queryResult.size() != 1) {
            return null;
        } else {
            return queryResult.get(0);
        }
    }

    /**
     * Insert a file into the database.
     * @param file file to be added into the database.
     * @return the added file.
     * @throws IllegalArgumentException if fileName is empty or projectId is null.
     */
    @Transactional
    public File insertFile(File file) throws IllegalArgumentException {
        if(file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        em.persist(file);
        em.flush();
        return file;
    }
}
