package trunk.medm.executioner.tester;


import java.io.FileReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ReturnANodelist {

  public static void main(String[] args) throws Exception {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xPath = factory.newXPath();

    NodeList shows = (NodeList) xPath.evaluate("//regionGroup", new InputSource(new FileReader(
        "e:/SFS_XMLOutput/StationInfoMap.xml")), XPathConstants.NODESET);
    for (int i = 0; i < shows.getLength(); i++) {
      Element show = (Element) shows.item(i);
//      String guestName = xPath.evaluate("guest/name", show);
//      String guestCredit = xPath.evaluate("guest/credit", show);

      System.out.println(show.getAttribute("stnCode") + ", " + show.getAttribute("stnName") + " - ");
//      System.out.println(guestName + " (" + guestCredit + ")");
    }

  }

}