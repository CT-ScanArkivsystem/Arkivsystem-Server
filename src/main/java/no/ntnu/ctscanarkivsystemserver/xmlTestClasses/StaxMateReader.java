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
    public static ArrayList<String> parse(String path) throws XMLStreamException, FileNotFoundException  {
        SMInputFactory factory = new SMInputFactory(XMLInputFactory.newFactory());
        ArrayList<String> stringList = new ArrayList<>();

        SMHierarchicCursor rootC = factory.rootElementCursor(new FileInputStream(path));

        try {
            rootC.advance();

            SMInputCursor rootChildCursor = rootC.childElementCursor();

            while (rootChildCursor.getNext() != null) {
                handleRootChildElement(stringList, rootChildCursor);
            }
        } finally {
            rootC.getStreamReader().closeCompletely();
        }
        
        return stringList;
    }

    /**
     * rootChildCursor points at the <dicomAttribute> element
     * rootChildCursor.childElementCursor() points at <Value> elements
     * Will check if the cursor is pointing at a valid element (In this case, <DicomAttribute> or <Item>)
     * If the cursor is pointing at a valid element it will go into the corresponding case in the switch
     * and get the information we want.
     *
     * @param stringList A list of the information we want saved as strings.
     * @param rootChildCursor A cursor that points at the DicomAttribute elements.
     * @throws XMLStreamException If something goes wrong with the iterating over the document.
     */
    private static void handleRootChildElement(ArrayList<String> stringList, SMInputCursor rootChildCursor) throws XMLStreamException {
        switch (rootChildCursor.getLocalName()) {
            case "DicomAttribute":
                //Gets the attribute value of the attribute keyword in a DicomAttribute element.
                stringList.add(rootChildCursor.getAttrValue("keyword"));
                //Gets the element value of the child elements (usually <Value>)
                handleValueElement(stringList, rootChildCursor.childElementCursor());
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
     * @param stringList List which will hold the information we want.
     * @param valueCursor Cursor which will iterate over the child elements of a DicomAttribute. This means mainly <Value>.
     * @throws XMLStreamException If something goes wrong with the iterating over the document.
     */
    private static void handleValueElement(ArrayList<String> stringList, SMInputCursor valueCursor) throws XMLStreamException {
        while (valueCursor.getNext() != null) {
            String v = valueCursor.getElemStringValue();
            stringList.add(v);
        }
    }
}
