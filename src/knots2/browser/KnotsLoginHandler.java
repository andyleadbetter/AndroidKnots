package knots2.browser;

import java.io.CharArrayWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/** 
 * Sax Parser for ORB XML API.
 */


public class KnotsLoginHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================


	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the maxInactiveTime
	 */
	public String getMaxInactiveTime() {
		return maxInactiveTime;
	}

	/**
	 * @return the orbVersion
	 */
	public String getOrbVersion() {
		return orbVersion;
	}

	private String status;
	private String maxInactiveTime;
	private String orbVersion;
	private String sessionId;
	
	// Buffer for collecting data from
	// the "characters" SAX event.
	private CharArrayWriter contents = new CharArrayWriter();






	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		contents.reset();

	}

	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
	throws SAXException {

		if (localName.equals("status")) {
			status = contents.toString();
		}else if (localName.equals("orbSessionId")) {
			sessionId=contents.toString();
		}else if (localName.equals("maxInactiveInterval")) {
			maxInactiveTime = contents.toString();
		}else if (localName.equals("orbVersion")) {
			orbVersion = contents.toString();
		}
	}

	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
	public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
	}

}