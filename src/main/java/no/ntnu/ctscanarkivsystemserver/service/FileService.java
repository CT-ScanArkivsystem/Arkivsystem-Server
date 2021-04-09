package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.FileDao;
import no.ntnu.ctscanarkivsystemserver.exception.TagExistsException;
import no.ntnu.ctscanarkivsystemserver.model.File;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FileService {

    private final FileDao fileDao;

    @Autowired
    public FileService(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    /**
     *
     * @param fileName name of file to get.
     * @param projectId sub project folder file is in.
     * @param subFolder project file is in.
     * @return file with fileName in project with projectId like the params. Null if no file was found.
     * @throws IllegalArgumentException if fileName is empty or projectId is null.
     */
    public File getFile(String fileName, String subFolder, UUID projectId) throws IllegalArgumentException {
        return  fileDao.getFileByNameAndProject(fileName, projectId, subFolder);
    }

    /**
     * Adds a object File to the database.
     * If file already exist this will just return the existing file.
     * @param fileName name of file to be added. (Including file type)
     * @param subFolder sub project folder file is in.
     * @param project project file is in.
     * @return added file. Null if file was not added. Existing file if it already exist.
     * @throws IllegalArgumentException if fileName is empty or projectId is null.
     */
    public File addFileToDatabase(String fileName, String subFolder, Project project) throws IllegalArgumentException {
        File file = getFile(fileName, subFolder, project.getProjectId());
        if(file != null) {
            return file;
        } else {
            return fileDao.insertFile(new File(fileName, subFolder.toLowerCase(), project));
        }
    }

    /**
     * Returns true if tag already exist in file.
     * @param tag tag to see if exist.
     * @param file file to see if tag exist in.
     * @return true if tag exist in file.
     */
    private boolean doesTagExistInFile(Tag tag, File file) {
        for(Tag fileTag:file.getTags()) {
            if(fileTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new tag to a project.
     * @param file file for tag to be added to.
     * @param tagsToBeAdded tags to be added to file.
     * @return File if adding was successful. Null if something went wrong.
     * @throws TagExistsException if tag already exist in the file.
     */
    public File addTag(File file, List<Tag> tagsToBeAdded) throws TagExistsException {
        for(Tag tagToBeAdded:tagsToBeAdded) {
            if(doesTagExistInFile(tagToBeAdded, file)) {
                throw new TagExistsException(tagToBeAdded.getTagName());
            }
        }
        return fileDao.addTagsToFile(file, tagsToBeAdded);
    }

    /**
     * Gets all tags a file is associated with.
     * @param projectId id of project files are associated with.
     * @param subFolder sub project folder files are in (The folder name in the file-server).
     * @param allFileNamesInDir a List of all the files to get tags from.
     * @return HashMap with fileName as key and List of tags associated with the file.
     * @throws IllegalArgumentException if projectId, subFolder or allFileNames are null.
     */
    public Map<String, List<Tag>> getTagsOnFiles(UUID projectId, String subFolder, List<String> allFileNamesInDir) throws IllegalArgumentException {
        Map<String, List<Tag>> fileNamesWithTags = new HashMap<>();
        if(projectId == null || subFolder == null || subFolder.trim().isEmpty() || allFileNamesInDir == null) {
            throw new IllegalArgumentException("Fields projectId, subFolder and allFilesNamesInDir cannot be null!");
        } else {
            for(String fileName:allFileNamesInDir) {
                File file = fileDao.getFileByNameAndProject(fileName, projectId, subFolder.toLowerCase());
                if(file == null) {
                    fileNamesWithTags.put(fileName, Collections.emptyList());
                } else {
                    fileNamesWithTags.put(fileName, file.getTags());
                }
            }
        }
        return fileNamesWithTags;
    }
}
