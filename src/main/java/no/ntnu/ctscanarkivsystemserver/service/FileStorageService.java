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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.Subject;
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
    private final String IMAGE_DICOM_PATH;
    private final String IMAGE_TIFF_PATH;
    private final String fileStorageLocation;

    private final String userName;
    private final String password;
    private final String domain;
    private final String url;
    private SmbFileOutputStream out;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = fileStorageProperties.getUploadDir();
        this.DOCUMENT_PATH = fileStorageProperties.getDocumentDir();
        this.IMAGE_PATH = fileStorageProperties.getImageDir();
        this.LOG_PATH = fileStorageProperties.getLogDir();
        this.IMAGE_DICOM_PATH = fileStorageProperties.getDicomDir();
        this.IMAGE_TIFF_PATH = fileStorageProperties.getTiffDir();
        this.userName = fileStorageProperties.getUser();
        this.password = fileStorageProperties.getPass();
        this.domain = fileStorageProperties.getDomain();
        this.url = fileStorageProperties.getUrl();
    }

    public void test(MultipartFile files) {
        System.out.println("Test");
        try {
            Configuration config = new PropertyConfiguration(new Properties());
            CIFSContext context = new BaseContext(config);
            context = context.withCredentials(new NtlmPasswordAuthentication(null, domain, userName, password));

            //SmbFileInputStream in = new SmbFileInputStream(url + "/Test.txt", context);
            System.out.println("Test1");
            out = new SmbFileOutputStream(new SmbFile(url + "/" + getFileName(files), context));
            out.write(files.getBytes());
            System.out.println("Test2");
            /*byte[] b = new byte[8192];
            int n;
            byte[] bytes = "in.read()".getBytes();
            String test = Base64.getEncoder().encodeToString(bytes);
            System.out.println("Test5: " + test);
            while(( n = in.read( b )) > 0 ) {
                System.out.println("Test6");
                System.out.write( b, 0, n );
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Return the name of file including file type.
     * @param file file to get name from.
     * @return name of file incudling file type.
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
     */
    public void storeFile(MultipartFile[] files, Project project) throws FileStorageException, DirectoryCreationException {
        createProjectDirectories(project);
        for(MultipartFile file:files) {
            if (file != null && file.getOriginalFilename() != null) {
                try {
                    // Check if the file's name contains invalid characters
                    if (getFileName(file).contains("..")) {
                        throw new FileStorageException("Sorry! Filename contains invalid path sequence " + getFileName(file));
                    }
                    storeFileInDirectory(file, fileStorageLocation + dateNameToPath(project));
                } catch (Exception ex) {
                    throw new FileStorageException("Could not store file " + getFileName(file) + ". Please try again!\nMessage: "
                            + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Save the file into the correct directory depending on the file type.
     * @param file to save.
     * @param path to project folder.
     * @throws FileStorageException if something went wrong when trying to save file.
     */
    private void storeFileInDirectory(MultipartFile file, String path) throws FileStorageException {
        if(file.getOriginalFilename() != null) {
            String fileType = getFileType(file.getOriginalFilename());
            System.out.println("File type is: " + fileType);
            switch (fileType) {
                case "IMA":
                    System.out.println("DICOM file!");
                    saveFile(file, path + IMAGE_DICOM_PATH);
                    break;

                case "tiff":
                    System.out.println("Tiff file!");
                    saveFile(file, path + IMAGE_TIFF_PATH);
                    break;

                default:
                    System.out.println("Default!");
                    saveFile(file, path + DOCUMENT_PATH);
            }
        } else {
            System.out.println("File type was null.");
        }
    }

    /*public Resource loadFileAsResource(String fileName) {
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
    }*/

    /**
     * Save a file into the given path.
     * @param file to save into the given path.
     * @param path of where to save the given file.
     * @throws FileStorageException if something went wrong when trying to save file.
     */
    private void saveFile(MultipartFile file, String path) throws FileStorageException {
        System.out.println("Test");
        try {
            System.out.println("Test1");
            out = new SmbFileOutputStream(new SmbFile(url + "/" + path + "/" + getFileName(file), getContextWithCred()));
            out.write(file.getBytes());
            System.out.println("Test2");
        } catch (SmbException e) {
            throw new FileStorageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
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
        directoriesToMake.add(directoryPath + IMAGE_DICOM_PATH);
        directoriesToMake.add(directoryPath + IMAGE_TIFF_PATH);
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