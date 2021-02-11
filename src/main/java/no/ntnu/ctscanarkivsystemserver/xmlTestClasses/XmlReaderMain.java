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
        ArrayList<String> listOfXmlFiles = new FileFinder().findFilesInFolder("..\\ExampleFiles\\FORMAS\\");

        for (int listIterator = 0; listIterator < listOfXmlFiles.size(); listIterator++) {
            ArrayList<String> usefulInfoList;
            usefulInfoList = new StaxMateReader().parse("..\\ExampleFiles\\FORMAS\\" + listOfXmlFiles.get(listIterator));
            for (int i = 0; i < usefulInfoList.size(); i++) {
                System.out.println("List item " + i + ": " + usefulInfoList.get(i));
            }
        }
    }
}
