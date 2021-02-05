package no.ntnu.ctscanarkivsystemserver;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class DicomTestClass {


    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {


        String[] command = new String[] {
                "dcm4che-5.22.6-bin\\dcm4che-5.22.6\\bin\\dcm2xml.bat",
                "TIIN_KOND.CT.SPECIALS_TIINS80S_(ADULT).1.1.2017.12.04.10.12.25.734375.30992199.IMA"
        };

        // https://stackoverflow.com/questions/8496494/running-command-line-in-java
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        final Process process = builder.start();

        // Watch the process
        String result = watch(process);


        // https://stackoverflow.com/questions/9125241/remove-the-last-chars-of-the-java-string-variable
        String xmlFileName = StringUtils.removeEnd(command[command.length-1], ".IMA");
        stringToDom(result, xmlFileName);

    }

    public static String watch(Process process) {

        StringBuilder strBuilder = new StringBuilder();

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        try {
            while ((line = input.readLine()) != null) {
                strBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strBuilder.toString();
    }



    // https://stackoverflow.com/questions/17853541/java-how-to-convert-a-xml-string-into-an-xml-file
    public static void stringToDom(String xmlSource, String fileName)
            throws SAXException, ParserConfigurationException, IOException {

        // Parse the given input
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

        // Write the parsed document to an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }


        String[] attributesIWantToKeep = new String[] {
                "Manufacturer",
                "ManufacturerModelName"
        };

        deleteAttributesThatAreNot(doc, attributesIWantToKeep);

        DOMSource source = new DOMSource(doc);

        StreamResult result =  new StreamResult(new File("" + fileName + ".xml"));

        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAttributesThatAreNot(Document doc, String[] attributeKeywordList) {

        NodeList allDicomAttributeNodes = doc.getElementsByTagName("DicomAttribute");


        for (int i = allDicomAttributeNodes.getLength() - 1; i >= 0; i--) {

            Element e = (Element)allDicomAttributeNodes.item(i);


            List<String> wordList = Arrays.asList(attributeKeywordList);

            // If the list of attributes we want to keep DOES NOT contains the one we are currently at in the for loop
            // delete it
            if (!wordList.contains(e.getAttributes().getNamedItem("keyword").getNodeValue())) {
                e.getParentNode().removeChild(e);
                System.out.println("Delete");
            } else {
                System.out.println("Keep");
            }

        }
    }
}
