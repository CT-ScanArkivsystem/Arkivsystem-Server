package dcm4che;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che3.io.DicomInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, TransformerException, SAXException, ParserConfigurationException {

        Dcm2Xml dcm2XmlObject = new Dcm2Xml();

        String inputFile =
                new String("TIIN_KOND.CT.SPECIALS_TIINS80S_(ADULT).1.1.2017.12.04.10.12.25.734375.30992199.IMA");

        DicomInputStream dis = new DicomInputStream(new File(inputFile));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            // https://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java
            PrintStream old = System.out;
            System.setOut(ps);

            dcm2XmlObject.parse(dis);

            System.out.flush();
            System.setOut(old);

            // https://stackoverflow.com/questions/7454330/how-to-remove-newlines-from-beginning-and-end-of-a-string
            String xmlString = baos.toString().replaceAll("[\n\r]", "");

            stringToDom(xmlString, StringUtils.removeEnd(inputFile, ".IMA"));

        } finally {
            dis.close();
        }
    }

    // https://stackoverflow.com/questions/17853541/java-how-to-convert-a-xml-string-into-an-xml-file
    private static void stringToDom(String xmlSource, String fileName)
            throws SAXException, ParserConfigurationException, IOException, TransformerException {

        // Parse the given input
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

        // Write the parsed document to an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        transformer = transformerFactory.newTransformer();

        String[] attributesToDelete = new String[] {
                "PixelData"
        };
        deleteAttributes(doc, attributesToDelete);

        DOMSource source = new DOMSource(doc);
        StreamResult result =  new StreamResult(new File("" + fileName + ".xml"));

        // https://www.novixys.com/blog/howto-pretty-print-xml-java/
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(source, result);

    }

    private static void deleteAttributes(Document doc, String[] attributeKeywordList) {
        NodeList allDicomAttributeNodes = doc.getElementsByTagName("DicomAttribute");
        for (int i = allDicomAttributeNodes.getLength() - 1; i >= 0; i--) {
            Element e = (Element)allDicomAttributeNodes.item(i);
            List<String> wordList = Arrays.asList(attributeKeywordList);

            // If the list of attributes we want to keep DOES NOT contains the one we are currently at in the for loop
            // delete it
            if (wordList.contains(e.getAttributes().getNamedItem("keyword").getNodeValue())) {
                e.getParentNode().removeChild(e);
            }
        }
    }

}
