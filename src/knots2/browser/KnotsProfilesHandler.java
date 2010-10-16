package knots2.browser;


import java.io.CharArrayWriter;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KnotsProfilesHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================
	
	private Profile currentItem;
	
	private Vector<Profile> itemList = new Vector<Profile>();

	// Buffer for collecting data from
    // the "characters" SAX event.
    private CharArrayWriter contents = new CharArrayWriter();
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Vector<Profile> getParsedData() {
		return this.itemList;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		itemList = new Vector<Profile>();
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
		
		if( localName.equals("item")) {
			currentItem = new Profile();
			itemList.add(currentItem);		
		}
		
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		if (localName.equals("id")) {
			currentItem.setId( contents.toString());
		} else if( localName.equals("video_format")) {
			currentItem.setCodec(contents.toString());
		} else if( localName.equals("video_bitrate")) {
			currentItem.setBitrate(contents.toString());
		} else if( localName.equals("name")) {
			currentItem.setName(contents.toString());			
		}
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
    	}
    }