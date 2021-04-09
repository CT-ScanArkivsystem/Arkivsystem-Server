package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.File;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
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
     * @param subFolder sub project folder file is in.
     * @return file with fileName and projectId like the params. Null if no file was found.
     * @throws IllegalArgumentException if fileName is empty or projectId is null.
     */
    public File getFileByNameAndProject(String fileName, UUID projectId, String subFolder) throws IllegalArgumentException {
        Query query = em.createNamedQuery(File.FIND_FILE_BY_NAME_AND_PROJECT);
        if(fileName == null || fileName.trim().isEmpty() || projectId == null) {
            throw new IllegalArgumentException("fileName is empty or projectId is null");
        }
        query.setParameter("fileName", fileName).setParameter("projectId", projectId).setParameter("subFolder", subFolder);
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

    /**
     * Adds one or more tags to a file.
     * @param file file to add tags to.
     * @param tags tags to be added to file.
     * @return file with added tags.
     */
    @Transactional
    public File addTagsToFile(File file, List<Tag> tags) {
        em.refresh(file);
        prepareFileForEdit(file);
        for(Tag tag:tags) {
            file.getTags().add(tag);
        }
        return saveFile(file);
    }

    /**
     * Prepares the database for change.
     * @param file to be changed in the database.
     */
    private void prepareFileForEdit(File file) {
        System.out.println("File getting ready for edit.");
        if(file != null) {
            try {
                em.lock(file, LockModeType.PESSIMISTIC_WRITE);
            } catch (Exception e) {
                System.out.println("Exception in prepare file for edit: " + e.getMessage());
            }
        }
    }

    /**
     * Merge a file into the database and then lock it.
     * @param file group to be merged.
     * @return file if merge was successful else null.
     */
    private File saveFile(File file) {
        System.out.println("Trying to save file.");
        if(file != null) {
            try {
                em.merge(file);
                em.lock(file, LockModeType.NONE);
                em.flush();
                return file;
            } catch (Exception e) {
                System.out.println("Exception in save file: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Removes tags from a file in the database.
     * @param file file to remove tags from.
     * @param tagsToBeRemoved tags to be removed from file.
     * @return true if saving file is successful.
     */
    @Transactional
    public boolean removeTag(File file, List<Tag> tagsToBeRemoved) {
        em.refresh(file);
        prepareFileForEdit(file);
        for(Tag tag:tagsToBeRemoved) {
            file.getTags().remove(tag);
        }
        return saveFile(file) != null;
    }
}
