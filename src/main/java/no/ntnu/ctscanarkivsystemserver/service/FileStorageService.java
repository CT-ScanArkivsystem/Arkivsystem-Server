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
import no.ntnu.ctscanarkivsystemserver.model.database.Project;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class has the job of handling files and creating project directories.
 * <INFORMATION>
 *     To add support for new file types add file type in the switch case in both "getFileLocation" and "storeFileInDirectory".
 *     To add creation of new directory add directory path into list in "createProjectDirList".
 *     To change where file type is stored look at switch case in "storeFileInDirectory" and "getFileLocation".
 *     New directory paths is set in "application.properties", fetched by "FileStorageProperties.class" and set here as variables.
 * </INFORMATION>
 * @author trymv
 */
@Service
public class FileStorageService {

    private final ImageService imageService;

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
    public FileStorageService(FileStorageProperties fileStorageProperties, ImageService imageService) {
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
        this.imageService = imageService;
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
     * @param subFolder Project sub-folder to store files in.
     * @return list of all files which was not added.
     * @throws FileStorageException       if storing of files failed.
     * @throws DirectoryCreationException if creation of directories failed.
     */
    public List<String> storeFile(MultipartFile[] files, Project project, String subFolder) throws FileStorageException, DirectoryCreationException {
        List<String> notAddedFiles = new ArrayList<>();
        subFolder = backslashToStartOfString(subFolder);
        createProjectDirectories(project, subFolder);
        for (MultipartFile file : files) {
            if (file != null && file.getOriginalFilename() != null) {
                try {
                    // Check if the file's name contains invalid characters
                    if (isFilenameInvalid(getFileName(file))) {
                        throw new FileStorageException("Sorry! Filename contains invalid path sequence " + getFileName(file));
                    }
                    String notAddedFile = storeFileInDirectory(file, fileStorageLocation + dateNameToPath(project) + subFolder);
                    if (notAddedFile != null) {
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
     * @param subFolder Folder of sub-project to get file from.
     * @return file content as a byte array.
     * @throws IOException           if this method failed to close stream.
     * @throws FileStorageException  if this method failed to setup connection or get file.
     * @throws FileNotFoundException if file with fileName was not found.
     */
    public byte[] loadFileAsBytes(String fileName, Project project, String subFolder) throws IOException, FileStorageException, FileNotFoundException {
        SmbFile smbFile = null;
        SmbFileInputStream inputStream = null;
        subFolder = backslashToStartOfString(subFolder);
        byte[] bytes;
        try {
            smbFile = new SmbFile(url + "/" + getFileLocation(fileName, project, subFolder) + "/" + fileName, getContextWithCred());
            inputStream = new SmbFileInputStream(smbFile);
            bytes = IOUtils.toByteArray(inputStream);
        } catch (SmbException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bytes;
    }

    /**
     * Gets a image as a byte array.
     * @param imageName name of image file including file type.
     * @param project   Project image is associated with.
     * @param subFolder Folder of sub-project to get image from.
     * @param imgSize Size of returned image. If 0 image will be returned in original size.
     * @return image content as a byte array.
     * @throws IOException          if loadFileAsBytes method failed to close stream.
     * @throws FileStorageException if file with imageName was not found.
     */
    public byte[] getImageAsBytes(String imageName, Project project, String subFolder, int imgSize) throws IOException, FileStorageException {
        if (imageService.isFileAnImage(imageName)) {
            byte[] imageBytes = loadFileAsBytes(imageName, project, subFolder);
            if (imgSize != 0 && !getFileType(imageName).equals("gif")) {
                imageBytes = imageService.scaleImage(imageBytes, getFileType(imageName), imgSize);
            }
            return imageBytes;
        } else {
            throw new IllegalArgumentException("File is not a image or the system does not support it. File name is: " + imageName);
        }
    }

    /**
     * Get all file names in a directory.
     * Valid arguments is: documents, images, logs, dicom, tiff and all.
     * @param directory directory to get all file names from.
     * @param project   project associated with directory you want to get file names from.
     * @param subFolder Folder of sub-project to get file names from.
     * @return list of file names from directory.
     * @throws FileNotFoundException    if directory was not found.
     * @throws FileStorageException     if getting file names failed.
     * @throws BadRequestException      if directory is not a valid directory.
     * @throws IllegalArgumentException if project is null or directory is empty.
     */
    public List<String> getAllFileNames(String directory, Project project, String subFolder) throws FileNotFoundException, FileStorageException, BadRequestException, IllegalArgumentException {
        List<String> filesInDir = new ArrayList<>();
        if (directory.trim().isEmpty() || project == null || subFolder.trim().isEmpty()) {
            throw new IllegalArgumentException("Project is null, directory string is empty or subFolder is empty.");
        } else {
            //To make variable not case sensitive.
            directory = directory.toUpperCase();
        }
        subFolder = backslashToStartOfString(subFolder);
        String filePath = fileStorageLocation + dateNameToPath(project) + subFolder;
        switch (directory) {
            case "DOCUMENTS":
                filesInDir = getAllFileNamesInDirectory(filePath + DOCUMENT_PATH, true);
                break;

            case "IMAGES":
                filesInDir = getAllFileNamesInDirectory(filePath + IMAGE_PATH, true);
                break;

            case "LOGS":
                filesInDir = getAllFileNamesInDirectory(filePath + LOG_PATH, true);
                break;

            case "DICOM":
                filesInDir = getAllFileNamesInDirectory(filePath + DICOM_PATH, true);
                break;

            case "TIFF":
                filesInDir = getAllFileNamesInDirectory(filePath + TIFF_PATH, true);
                break;

            case "ALL":
                List<String> allDirs = createProjectDirList(project, subFolder);
                //Removing dir Archives.
                allDirs.remove(fileStorageLocation);
                for (String dir : allDirs) {
                    filesInDir.addAll(getAllFileNamesInDirectory(dir, true));
                }
                break;

            default:
                throw new BadRequestException(directory + " is not a valid directory");
        }
        return filesInDir;
    }

    /**
     * See if a file exists in a sub-project folder.
     * @param fileName name of file to see if exists.
     * @param project project associated with directory you want to see if file exists in.
     * @param subFolder Folder of sub-project to see if file exists in.
     * @return true if file was found in the file-server.
     * @throws FileNotFoundException if directory was not found.
     */
    public boolean doesFileExist(String fileName, Project project, String subFolder) throws FileNotFoundException {
        for (String file : getAllFileNames("all", project, subFolder)) {
            if (file.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return all folders of sub-projects in a project folder.
     * Note: This will also return all files in the project folder.
     * @param project project to get sub-project folders from.
     * @return List of all sub-project folders.S
     * @throws FileStorageException     if directory was not found.
     * @throws FileNotFoundException    if something went wrong when trying to get directory.
     * @throws IllegalArgumentException if project is null.
     */
    public List<String> getAllProjectSubFolders(Project project) throws FileStorageException, FileNotFoundException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null!");
        } else {
            return getAllFileNamesInDirectory(fileStorageLocation + dateNameToPath(project), false);
        }
    }

    /**
     * Return all files in a directory as a list.
     * @param directoryPath path to directory to list out all files in.
     * @param ignoreFolders if true this will not return any folders.
     * @return list with all files in a directory.
     * @throws FileNotFoundException if directory was not found.
     * @throws FileStorageException  if something went wrong when trying to get files in directory or directory.
     */
    private List<String> getAllFileNamesInDirectory(String directoryPath, boolean ignoreFolders) throws FileNotFoundException, FileStorageException {
        SmbFile smbFile = null;
        List<String> filesInDir = new ArrayList<>();
        try {
            smbFile = new SmbFile(url + "/" + directoryPath + "/", getContextWithCred());
            for (SmbFile fileInDir : smbFile.listFiles()) {
                if (fileInDir.getName().contains(".") || !ignoreFolders) {
                    filesInDir.add(fileInDir.getName().replace("/", ""));
                }
            }
        } catch (SmbException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        } finally {
            if (smbFile != null) {
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
     * @param subFolder Folder of sub-project to get file from.
     * @return Full path to where the file is located.
     */
    private String getFileLocation(String fileName, Project project, String subFolder) {
        String fileType = getFileType(fileName);
        String fileLocation = fileStorageLocation + dateNameToPath(project) + subFolder;
        switch (fileType) {
            case "IMA":
                fileLocation += DICOM_PATH;
                break;

            case "tiff":
                fileLocation += TIFF_PATH;
                break;

            case "xlsx":
            case "txt":
                fileLocation += LOG_PATH;
                break;

            case "jpg":
            case "png":
            case "PNG":
            case "gif":
            case "raw":
            case "eps":
            case "bmp":
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
     * @return null if file was successfully saved else return name of file.
     * @throws FileStorageException if something went wrong when trying to save file.
     * @throws IOException          if saveFile failed to close outputStream.
     */
    private String storeFileInDirectory(MultipartFile file, String path) throws FileStorageException, IOException {
        String notAddedFile = null;
        if (file.getOriginalFilename() != null) {
            String fileType = getFileType(file.getOriginalFilename());
            switch (fileType) {
                case "IMA":
                    notAddedFile = saveFile(file, path + DICOM_PATH);
                    break;

                case "tiff":
                    notAddedFile = saveFile(file, path + TIFF_PATH);
                    break;

                case "xlsx":
                case "txt":
                    notAddedFile = saveFile(file, path + LOG_PATH);
                    break;

                case "jpg":
                case "png":
                case "PNG":
                case "gif":
                case "raw":
                case "eps":
                case "bmp":
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
     * @return null if file was successfully saved else return name of file.
     * @throws FileStorageException if something went wrong when trying to save file.
     * @throws IOException          if outputStream failed to close.
     */
    private String saveFile(MultipartFile file, String path) throws FileStorageException, IOException {
        String notCreatedFile = null;
        SmbFile smbFile = null;
        SmbFileOutputStream outputStream = null;
        try {
            if (doesFileExist(file, path)) {
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
            if (smbFile != null) {
                smbFile.close();
            }
            if (outputStream != null) {
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
        try (SmbFile smbFile = new SmbFile(url + "/" + path + "/", getContextWithCred())) {
            for (SmbFile existingFile : smbFile.listFiles()) {
                if (getFileName(file).equals(existingFile.getName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new ForbiddenException("Exception while trying to see if file exists: " + e.getMessage());
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
        if (fileName.contains(".")) {
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
                String.format("%02d", projectDate.getMonthValue()) + "-" +
                String.format("%02d", projectDate.getDayOfMonth()) + "_" +
                project.getProjectName();
    }

    /**
     * Creates directories for a project if the project does not have them already.
     * @param project project to make directories for.
     * @throws DirectoryCreationException if creation of directories failed.
     */
    private void createProjectDirectories(Project project, String subFolder) throws DirectoryCreationException {
        List<String> directoriesToMake = createProjectDirList(project, subFolder);

        for (String dirPath : directoriesToMake) {
            try (SmbFile smbFile = new SmbFile(url + "/" + dirPath, getContextWithCred())) {
                if (!smbFile.exists()) {
                    smbFile.mkdir();
                }
            } catch (Exception e) {
                throw new DirectoryCreationException(e.getMessage());
            }
        }
    }

    /**
     * Creates a list of all directories leading up to the project directory and folders inside.
     * @param project project to make directories for.
     * @return List of all directories leading up to the project directory and folders inside.
     */
    private List<String> createProjectDirList(Project project, String subFolder) {
        String directoryPath = fileStorageLocation + dateNameToPath(project);
        List<String> directoriesToMake = new ArrayList<>();
        //To create the project directory:
        directoriesToMake.add(fileStorageLocation);
        directoriesToMake.add(directoryPath);
        directoriesToMake.add(directoryPath + subFolder);
        //Directories inside project directory:
        directoriesToMake.add(directoryPath + subFolder + IMAGE_PATH);
        directoriesToMake.add(directoryPath + subFolder + LOG_PATH);
        directoriesToMake.add(directoryPath + subFolder + DOCUMENT_PATH);
        //Sub directories of directories inside project directory:
        directoriesToMake.add(directoryPath + subFolder + DICOM_PATH);
        directoriesToMake.add(directoryPath + subFolder + TIFF_PATH);
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
        prop.put("jcifs.smb.client.enableSMB2", "true");
        prop.put("jcifs.smb.client.disableSMB1", "false");
        prop.put("jcifs.traceResources", "true");
        Configuration config = new PropertyConfiguration(prop);
        CIFSContext baseContext = new BaseContext(config);
        return baseContext.withCredentials(new Kerb5Authenticator(new Subject(), domain, userName, password));
    }

    /**
     * Adds a backslash to the beginning of a string if the string
     * does not have one already.
     * @param string string to add backslash to.
     * @return string with backslash.
     * @throws IllegalArgumentException If param is null or empty.
     */
    private String backslashToStartOfString(String string) throws IllegalArgumentException {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Cannot add back slash to empty or null string!");
        } else {
            if (!string.substring(1).equals("/")) {
                string = "/" + string;
            }
        }
        return string;
    }

    /**
     * Package all files in param into a zip and return them as a byte array.
     * Source: https://www.baeldung.com/java-compress-and-uncompress
     * @param filesToZip list of file names to download.
     * @param project    project to download files from.
     * @param subFolder  sub project folder files are in.
     * @return all files in a zip as a byte array.
     * @throws IOException if a file was not found.
     */
    public byte[] getFilesAsZip(List<String> filesToZip, Project project, String subFolder) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("multiCompressed.zip"); ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (String srcFile : filesToZip) {
                File fileToZip = new File(srcFile);
                byte[] fileBytes = loadFileAsBytes(fileToZip.getName(), project, subFolder);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                zipOut.write(fileBytes, 0, fileBytes.length);
            }
        }
        return FileUtils.readFileToByteArray(new File("multiCompressed.zip"));
    }

    /**
     * Checks if every file name in list contain file type.
     * @param fileNames file names to check.
     * @return true if all file names contain file type.
     */
    public boolean doesAllFileNamesContainType(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (!doesFileNameContainType(fileName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a folder contain any illegal characters.
     * @param name name of folder.
     * @return true if folder name is invalid.
     */
    public boolean isFolderNameInvalid(String name) {
        if (name.matches(".*[;,.=?*:|<>].*") || name.contains("\\") || name.contains("\"") || name.contains("//")) {
            System.out.println("Folder name ERROR: Illegal char in: " + name +
                    "\nIllegal characters is: ;,.=\\?*:|\"<> or multiple /");
            return true;
        }
        return false;
    }

    /**
     * Checks if a file contain any illegal characters.
     * @param filename name of file.
     * @return true if file name is invalid.
     */
    public boolean isFilenameInvalid(String filename) {
        if (filename.matches(".*[/;,=?*:|<>].*") || filename.contains("\\") || filename.contains("..") || filename.contains("\"")) {
            System.out.println("File name ERROR: Illegal char in: " + filename +
                    "\nIllegal characters is: /;,=\\?*:|\"<> or multiple .");
            return true;
        }
        return false;
    }
}
