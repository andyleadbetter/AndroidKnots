package knots2.browser;

import java.io.CharArrayWriter;
import java.io.ObjectInputStream.GetField;

import knots2.browser.KnotsListView.KnotsListDownload;
import knots2.browser.KnotsListView.KnotsListHandlerObserver;
import knots2.browser.KnotsListView.KnotsListHandlerUpdate;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;




public class KnotsListHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================
	private KnotsItem mCurrentItem;	
	private Activity mCurrentActivity;
	private final String TAG = "KnotsListHandler";

	// Buffer for collecting data from
    // the "characters" SAX event.
    private CharArrayWriter contents = new CharArrayWriter();
	
    enum CurrentState    {
    	Idle,
    	ParsingRoot,
    	ParsingItems,
    	ParsingItem,
    	ParsingGroup,
    	ParsingPage,
    	Finished
    };
    
    private CurrentState status;
	private String mCurrentElement;
	private KnotsListHandlerObserver mParserObserver;
	private int mTotalEntries = 0;
	private int mCount = 0;
    
	public KnotsListHandler( KnotsListHandlerObserver parserObserver) {
		
		mParserObserver = parserObserver;
		
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		status = CurrentState.Idle;
		mCount = 0;
		mTotalEntries = 0;
		
	}

	@Override
	public void endDocument() throws SAXException {
			
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		
		contents.reset();
		mCurrentElement = "";
		
		
		
		if( localName.equals("group") )
		{
			mCurrentItem = new KnotsItem();			
			status = CurrentState.ParsingGroup;			
    		mCurrentItem.setType( KnotsItem.VIRTUAL );
		}
		
		if( localName.equals("item")) {
			mCurrentItem = new KnotsItem();
			mCurrentItem.setType( KnotsItem.ITEM );		
			status = CurrentState.ParsingItem;
		}
		
		if( localName.equals("searchResult")) {
			/*
			 * <searchResult searchId="0" groupCount="3" itemCount="0">
			 */
			mTotalEntries = Integer.parseInt(atts.getValue("groupCount")) + Integer.parseInt(atts.getValue("itemCount"));
			
		}
		
		if( status == CurrentState.ParsingGroup )
		{
			int count = atts.getLength() - 1;			
			while( count >= 0 ) {				
					String value = atts.getValue(count);
					String name = atts.getQName(count);
					Log.d(TAG, name + " " + value );
					mCurrentItem.getFields().put(name,value);
					count = count - 1;

			}
		} else if( status == CurrentState.ParsingItem ) 		{
			if( localName.equals("field")){
				mCurrentElement = atts.getValue("name");
			} else if( localName.equals("item")) {
				String value = atts.getValue(0);
				String name = atts.getQName(0);
			
				mCurrentItem.getFields().put( name, value );
			}
		}
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		if( status == CurrentState.ParsingItem ) {
			
		
			if( localName.equals("field"))
			{
				mCurrentItem.getFields().put(mCurrentElement, contents.toString());
			}		
			else if( localName.equals("item") ) {
				// At end of an item if there was a thumbnailId then
				// set the image tag.
				if( mCurrentItem.getFields().containsKey("thumbnailId") ){
					String imageUrl = "&mediumId=" + mCurrentItem.getFields().get("thumbnailId") + "&maxWidth=128&maxHeight=128";				
					mCurrentItem.setItemImage(imageUrl);
				}
				sendItemUpdate(mCurrentItem);
				
			} 					
		} else if ( status == CurrentState.ParsingGroup ) {

			if( localName.equals("group") ) {
				sendItemUpdate(mCurrentItem);
			}
		}				
		
	}
	
	public void sendItemUpdate( KnotsItem item ) {
		KnotsListHandlerUpdate newItem = new KnotsListHandlerUpdate();
		newItem.setItem(mCurrentItem);
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