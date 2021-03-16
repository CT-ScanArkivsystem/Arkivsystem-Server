package no.ntnu.ctscanarkivsystemserver.service;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.Configuration;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.*;
import no.ntnu.ctscanarkivsystemserver.config.FileStorageProperties;
import no.ntnu.ctscanarkivsystemserver.exception.DirectoryCreationException;
import no.ntnu.ctscanarkivsystemserver.exception.FileStorageException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * This class has the job of handling files and creating project directories.
 * @author trymv
 */
@Service
public class FileStorageService {

    private final String DOCUMENT_PATH;
    private final String IMAGE_PATH;
    private final String LOG_PATH;
    private final String DICOM_PATH;
    private final String TIFF_PATH;
    private final String fileStorageLocation;

    private final String userName;
    private final String password;
    private final String domain;
    private final String url;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = fileStorageProperties.getUploadDir();
        this.DOCUMENT_PATH = fileStorageProperties.getDocumentDir();
        this.IMAGE_PATH = fileStorageProperties.getImageDir();
        this.LOG_PATH = fileStorageProperties.getLogDir();
        this.DICOM_PATH = fileStorageProperties.getDicomDir();
        this.TIFF_PATH = fileStorageProperties.getTiffDir();
        this.userName = fileStorageProperties.getUser();
        this.password = fileStorageProperties.getPass();
        this.domain = fileStorageProperties.getDomain();
        this.url = fileStorageProperties.getUrl();
    }

    /**
     * Return the name of file including file type.
     * @param file file to get name from.
     * @return name of file including file type.
     */
    private String getFileName(MultipartFile file) {
        return StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
    }

    /**
     * Tries to create directories then sort files into correct directory.
     * @param files files to store.
     * @param project project linked to files.
     * @throws FileStorageException if storing of files failed.
     * @throws DirectoryCreationException if creation of directories failed.
     * @return list of all files which was not added.
     */
    public List<String> storeFile(MultipartFile[] files, Project project) throws FileStorageException, DirectoryCreationException {
        List<String> notAddedFiles = new ArrayList<>();
        createProjectDirectories(project);
        for(MultipartFile file:files) {
            if (file != null && file.getOriginalFilename() != null) {
                try {
                    // Check if the file's name contains invalid characters
                    if (getFileName(file).contains("..")) {
                        throw new FileStorageException("Sorry! Filename contains invalid path sequence " + getFileName(file));
                    }
                    String notAddedFile = storeFileInDirectory(file, fileStorageLocation + dateNameToPath(project));
                    if(notAddedFile != null) {
                        notAddedFiles.add(notAddedFile);
                    }
                } catch (Exception ex) {
                    throw new FileStorageException("Could not store file " + getFileName(file) + ". Please try again!\nMessage: "
                            + ex.getMessage(), ex);
                }
            }
        }
        return notAddedFiles;
    }

    /**
     * Gets the file content from a file in the file server as byte array.
     * @param fileName Name of file including file type.
     * @param project Project file is associated with.
     * @return file content as a byte array.
     * @throws IOException if this method failed to close stream.
     * @throws FileStorageException if this method failed to setup connection or get file.
     * @throws FileNotFoundException if file with fileName was not found.
     */
    public byte[] loadFileAsBytes(String fileName, Project project) throws IOException, FileStorageException, FileNotFoundException {
        SmbFile smbFile = null;
        SmbFileInputStream inputStream = null;
        byte[] bytes;
        try {
            smbFile = new SmbFile(url + "/" + getFileLocation(fileName, project) + "/" + fileName, getContextWithCred());
            inputStream = new SmbFileInputStream(smbFile);
            bytes = IOUtils.toByteArray(inputStream);
        } catch (SmbException e) {
          throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        } finally {
            if(smbFile != null) {
                smbFile.close();
            }
            if(inputStream != null) {
                inputStream.close();
            }
        }
        return bytes;
    }

    /**
     * Get all file names in a directory.
     * Valid arguments is: documents, images, logs, dicom, tiff and all.
     * @param directory directory to get all file names from.
     * @param project project associated with directory you want to get file names from.
     * @return list of file names from directory.
     * @throws FileNotFoundException if directory was not found.
     * @throws FileStorageException if getting file names failed.
     * @throws BadRequestException if directory is not a valid directory.
     * @throws IllegalArgumentException if project is null or directory is empty.
     */
    public List<String> getAllFileNames(String directory, Project project) throws FileNotFoundException, FileStorageException, BadRequestException, IllegalArgumentException {
        List<String> filesInDir = new ArrayList<>();
        if(directory.trim().isEmpty() || project == null) {
            throw new IllegalArgumentException("Project is null or directory string is empty.");
        } else {
            //To make variable not case sensitive.
            directory = directory.toUpperCase();
        }
        switch (directory) {
            case "DOCUMENTS":
                filesInDir = getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project) + DOCUMENT_PATH);
                break;

            case "IMAGES":
                filesInDir = getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project) + IMAGE_PATH);
                break;

            case "LOGS":
                filesInDir = getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project) + LOG_PATH);
                break;

            case "DICOM":
                filesInDir = getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project) + DICOM_PATH);
                break;

            case "TIFF":
                filesInDir = getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project) + TIFF_PATH);
                break;

            case "ALL":
                List<String> allDirs = createProjectDirList(project);
                //Removing dir Archives.
                allDirs.remove(0);
                for(String dir:allDirs) {
                    filesInDir.addAll(getAllFileNamesInDirectory(dir));
                }
                break;

            default:
                throw new BadRequestException(directory + " is not a valid document");
        }
        return filesInDir;
    }

    /**
     * Return all files in a directory as a list.
     * @param directoryPath path to directory to list out all files in.
     * @return list with all files in a directory.
     * @throws FileNotFoundException if directory was not found.
     * @throws FileStorageException if something went wrong when trying to get files in directory.
     */
    private List<String> getAllFileNamesInDirectory(String directoryPath) throws FileNotFoundException, FileStorageException {
        SmbFile smbFile = null;
        List<String> filesInDir = new ArrayList<>();
        try {
            smbFile = new SmbFile(url + "/" + directoryPath + "/", getContextWithCred());
            for(SmbFile fileInDir:smbFile.listFiles()) {
                if(fileInDir.getName().contains(".")) {
                    filesInDir.add(fileInDir.getName());
                }
            }
        } catch (SmbException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        } finally {
            if(smbFile != null) {
                smbFile.close();
            }
        }
        return filesInDir;
    }

    /**
     * Return the full path to where the file is located.
     * Does not include the file name.
     * @param fileName name of file to find path location to including file type.
     * @param project project file is associated with.
     * @return Full path to where the file is located.
     */
    private String getFileLocation(String fileName, Project project) {
        String fileType = getFileType(fileName);
        String fileLocation = fileStorageLocation + dateNameToPath(project);
        switch (fileType) {
            case "IMA":
                fileLocation += DICOM_PATH;
                break;

            case "tiff":
                fileLocation += TIFF_PATH;
                break;

            case "jpg":
            case "png":
            case "gif":
                fileLocation += IMAGE_PATH;
                break;

            default:
                fileLocation += DOCUMENT_PATH;
        }
        return fileLocation;
    }

    /**
     * Save the file into the correct directory depending on the file type.
     * @param file to save.
     * @param path to project folder.
     * @throws FileStorageException if something went wrong when trying to save file.
     * @throws IOException if saveFile failed to close outputStream.
     * @return null if file was successfully saved else return name of file.
     */
    private String storeFileInDirectory(MultipartFile file, String path) throws FileStorageException, IOException{
        String notAddedFile = null;
        if(file.getOriginalFilename() != null) {
            String fileType = getFileType(file.getOriginalFilename());
            System.out.println("File type is: " + fileType);
            switch (fileType) {
                case "IMA":
                    notAddedFile = saveFile(file, path + DICOM_PATH);
                    break;

                case "tiff":
                    notAddedFile = saveFile(file, path + TIFF_PATH);
                    break;

                case "jpg":
                case "png":
                case "gif":
                    notAddedFile = saveFile(file, path + IMAGE_PATH);
                    break;

                default:
                    notAddedFile = saveFile(file, path + DOCUMENT_PATH);
            }
        } else {
            System.out.println("File type was null.");
        }
        return notAddedFile;
    }

    /**
     * Save a file into the given path.
     * @param file to save into the given path.
     * @param path of where to save the given file.
     * @throws FileStorageException if something went wrong when trying to save file.
     * @throws IOException if outputStream failed to close.
     * @return null if file was successfully saved else return name of file.
     */
    private String saveFile(MultipartFile file, String path) throws FileStorageException, IOException {
        String notCreatedFile = null;
        SmbFile smbFile = null;
        SmbFileOutputStream outputStream = null;
        try {
            if(doesFileExist(file, path)) {
                notCreatedFile = getFileName(file);
                System.out.println("File already exist!");
            } else {
                smbFile = new SmbFile(url + "/" + path + "/" + getFileName(file), getContextWithCred());
                outputStream = new SmbFileOutputStream(smbFile);
                outputStream.write(file.getBytes());
            }
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        } finally {
            if(smbFile != null) {
                smbFile.close();
            }
            if(outputStream != null) {
                outputStream.close();
            }
        }
        return notCreatedFile;
    }

    /**
     * Checks if a file already exists in the folder.
     * @param file file to see if already exists.
     * @param path path to file including folder file is in.
     * @return true if file already exist
     */
    private boolean doesFileExist(MultipartFile file, String path) {
        SmbFile smbFile = null;
        try {
            smbFile = new SmbFile(url + "/" + path + "/", getContextWithCred());
            for (SmbFile existingFile:smbFile.listFiles()) {
                if(getFileName(file).equals(existingFile.getName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw  new ForbiddenException("Exception while trying to see if file exists: " + e.getMessage());
        } finally {
            if(smbFile != null) {
                smbFile.close();
            }
        }
        return false;
    }

    /**
     * Return file type.
     * @param fileName name of file with filetype.
     * @return filetype of file.
     */
    private String getFileType(String fileName) {
        StringBuilder sbFileName = new StringBuilder(fileName);
        fileName = sbFileName.reverse().toString();
        fileName = fileName.split("\\.")[0];
        return new StringBuilder(fileName).reverse().toString();
    }

    /**
     * Checks if the fileName contain the file type.
     * @param fileName name of file.
     * @return true if file name contains "." and is not empty after.
     */
    public boolean doesFileNameContainType(String fileName) {
        if(fileName.contains(".")) {
            System.out.println("File type: " + getFileType(fileName).trim());
            return !getFileType(fileName).trim().isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Creates the directory name of the project as a path.
     * @param project to make directory to.
     * @return String of directory name.
     */
    private String dateNameToPath(Project project) {
        LocalDate projectDate = convertToLocalDateViaSqlDate(project.getCreation());
        return "/" + projectDate.getYear() + "-" +
                projectDate.getMonthValue() + "-" +
                projectDate.getDayOfMonth() + "_" +
                project.getProjectName();
    }

    /**
     * Creates directories for a project if the project does not have them already.
     * @param project project to make directories for.
     * @throws DirectoryCreationException if creation of directories failed.
     */
    private void createProjectDirectories(Project project) throws DirectoryCreationException {
        List<String> directoriesToMake = createProjectDirList(project);

        for(String dirPath:directoriesToMake) {
            SmbFile smbFile = null;
            try {
                smbFile = new SmbFile(url + "/" + dirPath, getContextWithCred());
                if(!smbFile.exists()) {
                    smbFile.mkdir();
                }
            } catch (Exception e) {
                throw new DirectoryCreationException(e.getMessage());
            } finally {
                if(smbFile != null) {
                    smbFile.close();
                }
            }
        }
    }

    /**
     * Creates a list of all directories leading up to the project directory and folders inside.
     * @param project project to make directories for.
     * @return List of all directories leading up to the project directory and folders inside.
     */
    private List<String> createProjectDirList(Project project) {
        String directoryPath = fileStorageLocation + dateNameToPath(project);
        List<String> directoriesToMake = new ArrayList<>();
        //To create the project directory:
        directoriesToMake.add(fileStorageLocation);
        directoriesToMake.add(directoryPath);
        //Directories inside project directory:
        directoriesToMake.add(directoryPath + IMAGE_PATH);
        directoriesToMake.add(directoryPath + LOG_PATH);
        directoriesToMake.add(directoryPath + DOCUMENT_PATH);
        //Sub directories of directories inside project directory:
        directoriesToMake.add(directoryPath + DICOM_PATH);
        directoriesToMake.add(directoryPath + TIFF_PATH);
        return directoriesToMake;
    }

    /**
     * Convert a database date to LocalDate.
     * @param dateToConvert date to convert into LocalDate.
     * @return LocalDate of date.
     */
    private LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

    /**
     * Creates a context with credentials.
     * @return CIFSContext with context and credentials.
     * @throws CIFSException if creation of context failed.
     */
    private CIFSContext getContextWithCred() throws CIFSException {
        Properties prop = new Properties();
        prop.put( "jcifs.smb.client.enableSMB2", "true");
        prop.put( "jcifs.smb.client.disableSMB1", "false");
        prop.put( "jcifs.traceResources", "true" );
        Configuration config = new PropertyConfiguration(prop);
        CIFSContext baseContext = new BaseContext(config);
        return baseContext.withCredentials(new Kerb5Authenticator(new Subject(), domain, userName, password));
    }
}