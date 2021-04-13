package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.dao.FileDao;
import no.ntnu.ctscanarkivsystemserver.exception.MyFileNotFoundException;
import no.ntnu.ctscanarkivsystemserver.exception.TagExistsException;
import no.ntnu.ctscanarkivsystemserver.exception.TagNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.File;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import no.ntnu.ctscanarkivsystemserver.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class handles the business logic related to files in the database.
 * @author TrymV
 */
@Service
public class FileService {

    private final FileDao fileDao;

    @Autowired
    public FileService(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    /**
     * Return a file from the database which has projectId, subFolder and fileName like params.
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
     * Return all files in database which has projectId like params.
     * @param projectId sub project folder files are in.
     * @return files with projectId like the params. Null if no file was found.
     * @throws IllegalArgumentException if projectId is null.
     */
    public List<File> getFiles(UUID projectId) throws IllegalArgumentException {
        return  fileDao.getFilesByProject(projectId);
    }

    /**
     * Return a list of all unique file tag names associated with a project.
     * @param project project to get all file tag names associated with it.
     * @return list of all unique file tag names associated with a project.
     */
    public List<String> getAllTagNamesAssociatedWithProject(Project project) {
        Set<String> allTagNames = new HashSet<>();
        for(File file:getFiles(project.getProjectId())) {
            allTagNames.addAll(file.getTags().stream().map(Tag::getTagName).collect(Collectors.toList()));
        }
        return new ArrayList<>(allTagNames);
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

    /**
     * Gets all tag names which are used on files.
     * This will only return unique tag names.
     * @param projectId id of project files are associated with.
     * @param subFolder sub project folder files are in (The folder name in the file-server).
     * @param allFileNamesInDir a List of all the files to get tag names from.
     * @return set of all tag names which are used on files.
     * @throws IllegalArgumentException if projectId, subFolder or allFileNames are null.
     */
    public Set<String> getAllFileTagNames(UUID projectId, String subFolder, List<String> allFileNamesInDir) throws IllegalArgumentException {
        Set<String> fileTagNames = new LinkedHashSet<>();
        if(projectId == null || subFolder == null || subFolder.trim().isEmpty() || allFileNamesInDir == null) {
            throw new IllegalArgumentException("Fields projectId, subFolder and allFilesNamesInDir cannot be null!");
        } else {
            for(String fileName:allFileNamesInDir) {
                File file = fileDao.getFileByNameAndProject(fileName, projectId, subFolder.toLowerCase());
                if(file != null) {
                    fileTagNames.addAll(file.getTags().stream().map(Tag::getTagName).collect(Collectors.toList()));
                }
            }
        }
        return fileTagNames;
    }

    /**
     * Removes tags from a file in the database.
     * @param projectId id of project the file is associated with.
     * @param tagsToBeRemoved tags to be removed from file.
     * @param subFolder sub project folder file is in (The folder name in the file-server).
     * @param fileName name of file to remove tags from.
     * @return true if removing of tags was successful.
     * @throws MyFileNotFoundException if file was not found in the database.
     * @throws IllegalArgumentException if projectId, subFolder, tagsToBeRemoved or fileName are null.
     */
    public boolean removeTags(UUID projectId, List<Tag> tagsToBeRemoved, String subFolder, String fileName) throws MyFileNotFoundException, IllegalArgumentException {
        if (projectId == null || subFolder == null || subFolder.trim().isEmpty() || tagsToBeRemoved == null || fileName == null) {
            throw new IllegalArgumentException("Fields projectId, subFolder, tagsToBeRemoved and fileName cannot be null!");
        } else {
            File file = getFile(fileName, subFolder, projectId);
            if (file == null) {
                throw new MyFileNotFoundException("File with name: " + fileName + ", in project with id: " + projectId
                        + ", in sub-folder: " + subFolder + " was not found in the database.");
            }
            for (Tag tagToBeRemoved : tagsToBeRemoved) {
                if (!doesTagExistInFile(tagToBeRemoved, file)) {
                    throw new TagNotFoundException(tagToBeRemoved.getTagName());
                }
            }
            return fileDao.removeTag(file, tagsToBeRemoved);
        }
    }
}
