package no.ntnu.ctscanarkivsystemserver.xmlTestClasses;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/*
This class is from http://blog.palominolabs.com/2013/03/06/parsing-xml-with-java-and-staxmate/index.html
Should print a list of DicomAttributes' keywords and the DicomAttributes value.
For now only prints to the log, but will probably in the future use the information gathered for something more useful.
 */
public class StaxMateReader {

    /**
     * Creates a root cursor and starts a while loop that continues as long as there are
     * elements to iterate over.
     *
     * @param path the path to the file you want to read from.
     * @return stringList a list which should contain DicomAttributes' keywords and their values.
     * @throws XMLStreamException If something goes wrong with the iterating over the document.
     * @throws FileNotFoundException If the file from path is not found.
     */
    public static ArrayList<DicomAttributeObject> parse(String path) throws XMLStreamException, FileNotFoundException  {
        SMInputFactory factory = new SMInputFactory(XMLInputFactory.newFactory());
        ArrayList<DicomAttributeObject> DicomAttributeObjectList = new ArrayList<>();

        SMHierarchicCursor rootC = factory.rootElementCursor(new FileInputStream(path));

        try {
            rootC.advance();

            SMInputCursor rootChildCursor = rootC.childElementCursor();

            while (rootChildCursor.getNext() != null) {
                handleRootChildElement(DicomAttributeObjectList, rootChildCursor);
            }
        } finally {
            rootC.getStreamReader().closeCompletely();
        }
        
        return DicomAttributeObjectList;
    }

    /**
     * rootChildCursor points at the <dicomAttribute> element
     * rootChildCursor.childElementCursor() points at <Value> elements
     * Will check if the cursor is pointing at a valid element (In this case, <DicomAttribute> or <Item> *Item not implemented)
     * If the cursor is pointing at a valid element it will go into the corresponding case in the switch
     * and get the information we want.
     *
     * @param DicomAttributeObjectList The list which we want to put our DicomAttributeObjects in, so that we can access it further up
     * @param rootChildCursor A cursor that points at the DicomAttribute elements.
     * @throws XMLStreamException If something goes wrong with the iterating over the document.
     */
    private static void handleRootChildElement(ArrayList<DicomAttributeObject> DicomAttributeObjectList, SMInputCursor rootChildCursor) throws XMLStreamException {
        switch (rootChildCursor.getLocalName()) {
            case "DicomAttribute":
                DicomAttributeObject dicomAttributeObject = new DicomAttributeObject(); // Create a new DicomAttributeObject
                dicomAttributeObject.setKeyword(rootChildCursor.getAttrValue("keyword")); // Set the keyword variable in DicomAttributeObject
                handleValueElement(dicomAttributeObject, rootChildCursor.childElementCursor()); // Get all the value elements of the DicomAttribute the cursor is pointing at
                DicomAttributeObjectList.add(dicomAttributeObject); // Add the dicomAttributeObject to the list
                break;
                //TODO: Figure out if this needs implementing
            case "Item":
                System.out.println("Inside Item. Shouldn't be here yet :)");
                break;
            default:
                break;
        }
    }

    /**
     * Loops as long as there is a child element of the parent cursor. Gets the element string value
     * and adds it to the list of information.
     *
     * @param DicomAttributeObject List which will hold the information we want.
     * @param valueCursor Cursor which will iterate over the child elements of a DicomAttribute. This means mainly <Value>.
     * @throws XMLStreamException If something goes wrong with the iterating over the document.
     */
    private static void handleValueElement(DicomAttributeObject DicomAttributeObject, SMInputCursor valueCursor) throws XMLStreamException {
        while (valueCursor.getNext() != null) {
            ValueXmlObject valueXmlObject = new ValueXmlObject();
            valueXmlObject.setNumber(Integer.parseInt(valueCursor.getAttrValue("number")));
            valueXmlObject.setElementContent(valueCursor.getElemStringValue());
            DicomAttributeObject.valueList.add(valueXmlObject);
        }
    }
}
