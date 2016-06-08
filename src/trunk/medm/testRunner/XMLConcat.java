package trunk.medm.testRunner;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLConcat {
    public static void main(String[] args) throws Throwable {
    	String varShortcut = "R12";
        File dir = new File("ensemble2014/prcp");
//        File[] rootFiles = dir.listFiles();
        
        File[] rootFiles = new File[2]; 
        
        //Ensemble
		for(int i=0; i<2; i++){
			String stepper = String.format("%02d", i);
			rootFiles[i] = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/R12/DFS_MEDM_STN_EPSG_PMOS_R12_M"+stepper+".201406160000.xml");
		}
        
        
        Writer outputWriter = new FileWriter("ensemble2014/prcp/mergedFile.xml");
        XMLOutputFactory xmlOutFactory = XMLOutputFactory.newFactory();
        XMLEventWriter xmlEventWriter = xmlOutFactory.createXMLEventWriter(outputWriter);
        XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();

        xmlEventWriter.add(xmlEventFactory.createStartDocument());
        xmlEventWriter.add(xmlEventFactory.createStartElement("", null, "rootSet"));

        XMLInputFactory xmlInFactory = XMLInputFactory.newFactory();
        for (File rootFile : rootFiles) {

        	//XPATH
        	XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
//            InputSource inputSource = new InputSource(new FileInputStream(rootFile));
            InputSource inputSource = new InputSource(new FileReader(rootFile));
            NodeList specifiedStation = (NodeList) xpath.evaluate("//regionGroup/stn[@stnNo='47108']", inputSource, XPathConstants.NODESET);
            System.out.println("specifiedStation.getLength(): "+specifiedStation.getLength());
            //XPATH
        	
        	
        	System.out.println(rootFile.getName());
            XMLEventReader xmlEventReader = xmlInFactory.createXMLEventReader(new StreamSource(rootFile));
            XMLEvent event = xmlEventReader.nextEvent();
            // Skip ahead in the input to the opening document element
            while (event.getEventType() != XMLEvent.START_ELEMENT) {
                event = xmlEventReader.nextEvent();
            }

            do {
                xmlEventWriter.add(event);
                event = xmlEventReader.nextEvent();
            } while (event.getEventType() != XMLEvent.END_DOCUMENT);
            xmlEventReader.close();
        }

        xmlEventWriter.add(xmlEventFactory.createEndElement("", null, "rootSet"));
        xmlEventWriter.add(xmlEventFactory.createEndDocument());

        xmlEventWriter.close();
        outputWriter.close();
        
        
        
        
    }
}