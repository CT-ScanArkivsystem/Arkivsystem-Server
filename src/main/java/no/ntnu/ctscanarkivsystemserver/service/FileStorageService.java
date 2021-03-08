package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.config.FileStorageProperties;
import no.ntnu.ctscanarkivsystemserver.exception.FileStorageException;
import no.ntnu.ctscanarkivsystemserver.exception.MyFileNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class has the job of handling files and creating project directories.
 * @author trymv
 * @source https://www.callicoder.com/spring-boot-file-upload-download-rest-api-example/
 */
@Service
public class FileStorageService {

    private final Path DOCUMENT_PATH;
    private final Path IMAGE_PATH;
    private final Path LOG_PATH;
    private final Path IMAGE_DICOM_PATH;
    private final Path IMAGE_TIFF_PATH;
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        this.DOCUMENT_PATH = Paths.get(fileStorageProperties.getDocumentDir())
                .toAbsolutePath().normalize();
        this.IMAGE_PATH = Paths.get(fileStorageProperties.getImageDir())
                .toAbsolutePath().normalize();
        this.LOG_PATH = Paths.get(fileStorageProperties.getLogDir())
                .toAbsolutePath().normalize();
        this.IMAGE_DICOM_PATH = Paths.get(fileStorageProperties.getDicomDir())
                .toAbsolutePath().normalize();
        this.IMAGE_TIFF_PATH = Paths.get(fileStorageProperties.getTiffDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public void storeFile(MultipartFile file, Project project) throws FileStorageException {
        String fileName;
        // Normalize file name
        if(file != null && file.getOriginalFilename() != null) {
            fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try {
                // Check if the file's name contains invalid characters
                if(fileName.contains("..")) {
                    throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
                }
                if(!createProjectDirectories(this.fileStorageLocation.toString() + dateNameToPath(project))) {
                    throw new FileStorageException("Something went wrong when trying to create project directories!");
                }
                storeFileInDirectory(file, fileStorageLocation.toString() + dateNameToPath(project), fileName);
            } catch (IOException ex) {
                throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }
    }

    /**
     * Save the file into the correct directory depending on the file type.
     * @param file to save.
     * @param path to project folder.
     * @param fileName name of file.
     * @throws IOException if something went wrong when trying to save file.
     */
    private void storeFileInDirectory(MultipartFile file, String path, String fileName) throws IOException{
        if(file.getOriginalFilename() != null) {
            String fileType = getFileType(file.getOriginalFilename());
            System.out.println("File type is: " + fileType);
            switch (fileType) {
                case "IMA":
                    System.out.println("DICOM file!");
                    saveFile(file, path + IMAGE_DICOM_PATH, fileName);
                    break;

                case "tiff":
                    System.out.println("Tiff file!");
                    saveFile(file, path + IMAGE_TIFF_PATH, fileName);
                    break;

                default:
                    System.out.println("Default!");
                    saveFile(file, path + DOCUMENT_PATH, fileName);
            }
        } else {
            System.out.println("File type was null.");
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    /**
     * Save a file into the given path.
     * @param file to save into the given path.
     * @param path of where to save the given file.
     * @param fileName name of file.
     * @throws IOException if something went wrong when trying to save file.
     */
    private void saveFile(MultipartFile file, String path, String fileName) throws IOException {
        Path projectPath = Paths.get(path)
                .toAbsolutePath().normalize();
        // Copy file to the target location (Replacing existing file with the same name)
        Path targetLocation = projectPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
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
     * Makes directories for a project if the project does not have them already.
     * @param directoryPath path to directories including project directory.
     * @return true if directories was made or already exists.
     */
    private boolean createProjectDirectories(String directoryPath) {
        List<String> directoriesToMake = new ArrayList<>();
        //To create the project directory:
        directoriesToMake.add(directoryPath);
        //Directories inside project directory:
        directoriesToMake.add(directoryPath + IMAGE_PATH);
        directoriesToMake.add(directoryPath + LOG_PATH);
        directoriesToMake.add(directoryPath + DOCUMENT_PATH);
        //Sub directories of directories inside project directory:
        directoriesToMake.add(directoryPath + IMAGE_DICOM_PATH);
        directoriesToMake.add(directoryPath + IMAGE_TIFF_PATH);

        for(String dirPath:directoriesToMake) {
            File dirPathFile = new File(dirPath);
            if (!dirPathFile.exists()) {
                try {
                    dirPathFile.mkdir();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Convert a database date to LocalDate.
     * @param dateToConvert date to convert into LocalDate.
     * @return LocalDate of date.
     */
    private LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }
}