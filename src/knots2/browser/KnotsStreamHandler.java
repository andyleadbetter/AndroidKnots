package knots2.browser;

import java.io.CharArrayWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/** 
 * Sax Parser for ORB XML API.
 */


public class KnotsStreamHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================

	String mUri = "";
	
	/**
	 * @return the status
	 */
	
	public String getUri() {
		// TODO Auto-generated method stub
		return mUri;
	}

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
		if (localName.equals("item")) {
			mUri = atts.getValue(0);
		}

	}

	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
	throws SAXException {

	}

	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
	public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
	}



}