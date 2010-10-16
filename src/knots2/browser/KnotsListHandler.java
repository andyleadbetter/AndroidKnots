package knots2.browser;

import java.io.CharArrayWriter;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KnotsListHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================
	
	private KnotsItem currentItem;
	private KnotsPage currentPage;
	
	private Vector<KnotsItem> itemList = new Vector<KnotsItem>();

	// Buffer for collecting data from
    // the "characters" SAX event.
    private CharArrayWriter contents = new CharArrayWriter();
	
    enum CurrentState    {
    	Idle,
    	ParsingRoot,
    	ParsingItems,
    	ParsingItem,
    	ParsingPage,
    	Finished
    };
    
    
    

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Vector<KnotsItem> getParsedData() {
		return this.itemList;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		itemList = new Vector<KnotsItem>();
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
			currentItem = new KnotsItem();
			itemList.add(currentItem);		
		} else if( localName.equals("pages")) {
			currentPage = new KnotsPage();		
		}
		
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		if (localName.equals("dirname")) {
			currentItem.setDirectoryNameId(contents.toString());
		}else if (localName.equals("dir")) {
			currentItem.setDirectoryId(contents.toString());
		}else if (localName.equals("id")) {
			currentItem.setId(contents.toString());
		}else if (localName.equals("current")) {
			currentPage.setCurrentPage(Integer.parseInt(contents.toString().trim()));
		}else if (localName.equals("total")) {
			currentPage.setTotalPages(Integer.parseInt(contents.toString().trim()));
		}
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
    	}
    }