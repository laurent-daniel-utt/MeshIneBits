package meshIneBits.util.supportExportFile;

import meshIneBits.util.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.lang.annotation.Inherited;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class XMLDocument<T> {

    private Document document;
    private Path filePath;

    public XMLDocument(Path filePath) {
        try {
            this.document =DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            this.filePath=filePath;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Element createElement(String tag, String value) {
        Element element = document.createElement(tag);
        element.appendChild(document.createTextNode(value));
        return element;
    }

    public void appendChild(Element element) {
        document.appendChild(element);
    }

    public Element createElement(String tag) {
        return document.createElement(tag);
    }
    public void appendTextNode(Element e,String s){
        e.appendChild(document.createTextNode(s));
    }

    public void writeDocumentToXML(Path filePath) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(filePath.toString()));

            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public Path getFilePath() {
        return filePath;
    }
    public String getNameFromFileLocation() {
        return filePath.getFileName().toString().split("[.]")[0];
    }


    public void changeFilePathToXML() {
        String fileName = getFilePath().getFileName().toString();
        if (fileName.split("[.]").length >= 2) {
            fileName = fileName.split("[.]")[0];
        }
        fileName = fileName + "." + "xml";
        filePath = Paths.get(filePath.getParent().toString() + File.separator + fileName);
    }
    public void writeObjectToXML(T obj){
        try {

            if (obj == null) {
                throw new NullPointerException(obj.getClass().getName()+" can't be null");
            }
            Element meshElement = buildElementResult(obj);
            appendChild(meshElement);
            writeDocumentToXML(filePath);
            Logger.message("The XML file has been generated and saved in " + getFilePath());
        } catch (Exception e) {
            Logger.error("The XML file has not been generated, Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    abstract Element buildElementResult(T obj);
}
