package no.ntnu.ctscanarkivsystemserver.xmlhandling;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;


/*
This class is heavily inspired by the tutorial from https://www.baeldung.com/java-stax

This class should
 */
public class StAXReader {

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {

        String path = "..\\ExampleFiles\\FORMAS\\BMM.CT.THORAX_FORMAS_SOLID3_(ADULT).2.414.2017.11.08.12.26.03.232500.27001744.xml";

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(path));

        //Attributes you want the program to read
        String[] attributesToRead = new String[] {
                "Manufacturer",
                "ManufacturerModelName",
                "StudyDate",
                "InstanceNumber"
        };

            String attributeValue = getAttributeValue(attributesToRead[1], reader);
            System.out.println("Type " + attributesToRead[1]);
            System.out.println("Value: " + attributeValue);

    }

    private static String getAttributeValue(String attributeToRead, XMLEventReader reader) throws XMLStreamException {
        //Begin reading of given xml-file
        String desiredValue = "Not found!";
        boolean getNextValue = false;



        while (reader.hasNext()) {
            //Checks if the next element in the xml-file is a start element (<DicomAttribute>)
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();

                    switch (startElement.getName().getLocalPart()) {
                        case "DicomAttribute":
                            String attributeValue = startElement.getAttributeByName(new QName("keyword")).getValue();
                            if (attributeValue.equals(attributeToRead)) {
                                getNextValue = true;
                            } else {
                                getNextValue = false;
                            }
                            break;
                        case "Value":
                            if (getNextValue) {
                                desiredValue = reader.getElementText();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

        return desiredValue;
    }




    /*
    Uses an iterator to go through the attributes of the DicomAttribute element.
    Stops when list has no more attributes or if the keyword attribute is found.
    */
    private static String getAttribute(StartElement startElement, String wantedAttributeValue) {
        String attributeValue = "No attribute value found!";
        Iterator<Attribute> attributeIterator = startElement.getAttributes();

        boolean attributeValueFound = false;
        while (attributeIterator.hasNext() || !attributeValueFound) {
            Attribute myAttribute = attributeIterator.next();
            //Prints the value of the keyword
            if (myAttribute.getName().toString().equals(wantedAttributeValue)) {
                attributeValueFound = true;
                attributeValue = myAttribute.getValue();
            }
        }
        return attributeValue;
    }

}