package org.olerpler.iLocker.encryption;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Preferences {

	public boolean decryptAllOnOpen = false;
	public boolean encryptWhenMinToSysTray = false;

	public Preferences() {
		read();
	}

	public void read() {
		if(!EncryptionManager.PREFERENCES.exists()) {
			try {
				EncryptionManager.PREFERENCES.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {				
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(EncryptionManager.PREFERENCES);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("option");

			for(int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {							
					Element eElement = (Element) nNode;

					String attribute = eElement.getAttribute("id");
					
					if(attribute.equals("general")) {
					} else if(attribute.equals("security")) {
						decryptAllOnOpen        = getElementByTagName(eElement, "decrypt_all");
						encryptWhenMinToSysTray = getElementByTagName(eElement, "encrypt_min");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean getElementByTagName(Element eElement, String s) {
		return Boolean.parseBoolean(
				eElement.getElementsByTagName(s).
				item(0).getTextContent());
	}

	public void write() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("all_options");
			doc.appendChild(rootElement);

			Element options = doc.createElement("option");
			rootElement.appendChild(options);

			/** GENERAL **/
			options.setAttribute("id", "general");

			
			/** SECURITY **/
			options.setAttribute("id", "security");
			
			Element decryptAll = doc.createElement("decrypt_all");
			decryptAll.appendChild(doc.createTextNode(decryptAllOnOpen + ""));
			options.appendChild(decryptAll);

			Element sysTrayEnc = doc.createElement("encrypt_min");
			sysTrayEnc.appendChild(doc.createTextNode(encryptWhenMinToSysTray + ""));
			options.appendChild(sysTrayEnc);

			

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(EncryptionManager.PREFERENCES);

			transformer.transform(source, result);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}
