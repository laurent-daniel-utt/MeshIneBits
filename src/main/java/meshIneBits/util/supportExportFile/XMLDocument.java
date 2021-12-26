/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLAIRIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.util.supportExportFile;

import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.util.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    protected void writeObjectToXML(T obj){
        try {

            if (obj == null) {
                throw new NullPointerException(obj.getClass().getName()+" can't be null");
            }
            if (obj.getClass()== Mesh.class){
                // try to get the number of batch.
                AScheduler scheduler = ((Mesh) obj).getScheduler();
                List<Bit3D> listAllBit3D = AScheduler.getSetBit3DsSortedFrom(scheduler.getSortedBits());
                int nbBatch = (listAllBit3D.size()/CraftConfig.nbBitesBatch)+1;
                // Generate the xml for each Batch.
                for (int i =0; i<nbBatch;i++){
                    this.document =DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element meshElement = buildElementResult(obj,i);
                    appendChild(meshElement);
                    filePath = Paths.get(getFilePath() + File.separator );
                    writeDocumentToXML(Paths.get(getFilePath() + File.separator +"Batch "+ i+ "." + "xml"));

                    Logger.message("The XML file has been generated and saved in " + getFilePath());
                }
            }

            //Logger.message("The XML file has been generated and saved in " + getFilePath());
        } catch (Exception e) {
            Logger.error("The XML file has not been generated, Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    abstract Element buildElementResult(T obj, int nbBatch);
}
