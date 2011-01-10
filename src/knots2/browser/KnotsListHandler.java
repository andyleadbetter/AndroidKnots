package knots2.browser;

import java.io.CharArrayWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;

public class KnotsListHandler extends DefaultHandler{

	// ===========================================================
	// Fields
	// ===========================================================
	private KnotsItem mCurrentItem;
	private KnotsListAdapter mListAdapter;
	private Activity mCurrentActivity;
	
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
    
	public KnotsListHandler(Activity parentActivity, KnotsListAdapter listAdapter ) {
		
		mCurrentActivity = parentActivity;
		mListAdapter = listAdapter;
		
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
			
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		
		contents.reset();
		
		
		
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

		if( status == CurrentState.ParsingGroup || status == CurrentState.ParsingItem ){
			int count = atts.getLength() - 1;			
			while( count >= 0 )
			{				
				String value = atts.getValue(count);
				String name = atts.getQName(count);
				mCurrentItem.getFields().put(name,value);
				count = count - 1;

			}
		}
	}
	
	/** Gets be called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		
		if( status == CurrentState.ParsingItem ) {

			//if this is the end of the item element, then call retrieveData to pull info about this item
			if( localName.equals("item") ) {
				if( mCurrentItem.getFields().containsKey("thumbnailId") ){
					String imageUrl = "http://api.orb.com/orb/data/image?sid=" + ((Knots)mCurrentActivity.getApplication()).getSessionId() + "&mediumId=" + mCurrentItem.getFields().get("thumbnailId") + "&maxWidth=128&maxHeight=128";				
					mCurrentItem.setItemImage(imageUrl);
				}				
				mListAdapter.addItem(mCurrentItem);
			} 					
		} else if ( status == CurrentState.ParsingGroup ) {

			if( localName.equals("group") ) {
				mListAdapter.addItem(mCurrentItem);
			}			
		}
	}
	
	/** Gets be called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {		
		contents.write(ch, start, length);
    	}

    }