package knots2.browser;

import java.io.CharArrayWriter;

import knots2.browser.KnotsListView.KnotsListHandlerObserver;
import knots2.browser.KnotsListView.KnotsListHandlerUpdate;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class KnotsListHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================
	
	private KnotsItem currentItem;
	private KnotsPage currentPage;
	private KnotsAdapter listAdapter;
	
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
    
    private CurrentState status;
	private String mCurrentElement;
	private KnotsListHandlerObserver mParserObserver;
	private int mTotalEntries = 0;
	private int mCount = 0;
	private ImageDownloader mAsyncLoader;
    
	public KnotsListHandler( KnotsListHandlerObserver parserObserver) {
		
		mParserObserver = parserObserver;
	}
    



	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		status = CurrentState.Idle;
		
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		status = CurrentState.Finished;
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
			currentItem = new KnotsItem(mParserObserver.getApplication());
			status = CurrentState.ParsingItem;
		} else if( localName.equals("pages")) {
			currentPage = new KnotsPage();		
			status = CurrentState.ParsingPage;
		} else if( localName.equals("items")) {
			status = CurrentState.ParsingItems;
		}
		
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		if( status == CurrentState.ParsingItem ) {
			
				// While parsing the item element, add each field to the hash table, its used later when fetching media from server.
			if(!localName.equals("item")) {
				// Store this data to the item hash fields.
				currentItem.getFields().put(localName, contents.toString());	
			}
			
			//if this is the end of the item element, then call retrieveData to pull info about this item
			if( localName.equals("item") ) {
				currentItem.dataFinished();
				sendItemUpdate(currentItem);				
			}
			
						
		} else if( status == CurrentState.ParsingPage ) {
			if (localName.equals("current")) {
					currentPage.setCurrentPage(Integer.parseInt(contents.toString().trim()));
			}else if (localName.equals("total")) {
					currentPage.setTotalPages(Integer.parseInt(contents.toString().trim()));
			}
		}
	}
		
	public void sendItemUpdate( KnotsItem item ) {
		KnotsListHandlerUpdate newItem = new KnotsListHandlerUpdate();
		newItem.setItem(item);
		newItem.setCurrentItem(mCount++);
		newItem.setTotalItems(mTotalEntries);
		mParserObserver.onNewItem(newItem);		
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
    	}

    }