package no.ntnu.ctscanarkivsystemserver.xmlTestClasses;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class helps find the names of xml files in a given directory.
 */
public class FileFinder {
    ArrayList<String> listOfWantedFileNames = new ArrayList<>();

    /**
     * Method will go through the directory given by the path parameter and find all files
     * that end with .xml/.XML It will then return a string list with the names of the files.
     * NOTE: This method only returns the name of the file, not the path!!!
     *
     * @param path the path to the file directory.
     * @return listOfWantedFileNames list of xml files in the directory given in path.
     */
    public ArrayList<String> findFilesInFolder(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            String filename = listOfFiles[i].getName();
            if (filename.endsWith(".xml") || filename.endsWith(".XML")) {
                System.out.println("File ending with xml found: " + filename);
                listOfWantedFileNames.add(listOfFiles[i].getName());
            }
        }
        return listOfWantedFileNames;
    }
}
