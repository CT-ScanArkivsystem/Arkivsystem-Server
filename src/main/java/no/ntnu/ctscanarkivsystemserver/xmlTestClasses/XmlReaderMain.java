package no.ntnu.ctscanarkivsystemserver.xmlTestClasses;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class XmlReaderMain {

    /**
     * Main class to run different code to see what works and what doesn't
     *
     * @param args
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        // Creates a list that holds all the xml files that are to be read from in the given directory
        ArrayList<String> listOfXmlFiles = new FileFinder().findFilesInFolder("..\\ExampleFiles\\FORMAS\\");
        ArrayList<DicomAttributeObject> dicomAttributeObjectList = new ArrayList<>();
        // Loop that reads elements from xml files.
        for (int j = 0; j < listOfXmlFiles.size(); j++) {
            dicomAttributeObjectList = new StaxMateReader().parse("..\\ExampleFiles\\FORMAS\\" + listOfXmlFiles.get(j));

            for (int i = 0; i < dicomAttributeObjectList.size(); i++) {
                System.out.println("Keyword: " + dicomAttributeObjectList.get(i).keyword + " Value: " + dicomAttributeObjectList.get(i).getAllValues());
            }
        }
    }
}
