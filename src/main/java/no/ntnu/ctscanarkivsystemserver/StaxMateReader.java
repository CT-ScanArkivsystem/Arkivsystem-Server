package no.ntnu.ctscanarkivsystemserver;

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
Should print a list of
 */
public class StaxMateReader {

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        SMInputFactory factory = new SMInputFactory(XMLInputFactory.newFactory());
        String path = "..\\ExampleFiles\\FORMAS\\BMM.CT.THORAX_FORMAS_SOLID3_(ADULT).2.414.2017.11.08.12.26.03.232500.27001744.xml";
        ArrayList<String> strings = new ArrayList<>();
        strings.add("test");
        strings = parse(path, factory);
        for (int i = 0; i < strings.size(); i++) {
            System.out.println("List item " + i + ": " + strings.get(i));
        }
    }

    public static ArrayList<String> parse(String path, SMInputFactory factory) throws XMLStreamException, FileNotFoundException {
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
    /*
    rootChildCursor points at the <dicomAttribute> element
    rootChildCursor.childElementCursor() points at <Value> elements
    */
    private static void handleRootChildElement(ArrayList<String> stringList, SMInputCursor rootChildCursor) throws XMLStreamException {
        switch (rootChildCursor.getLocalName()) {
            case "DicomAttribute":
                stringList.add(rootChildCursor.getAttrValue("keyword"));
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

    private static void handleValueElement(ArrayList<String> stringList, SMInputCursor valueCursor) throws XMLStreamException {
        while (valueCursor.getNext() != null) {
            String v = valueCursor.getElemStringValue();
            stringList.add(v);
        }
    }
}
