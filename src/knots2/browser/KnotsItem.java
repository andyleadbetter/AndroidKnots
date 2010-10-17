/**
 * 
 */
package knots2.browser;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;

import android.graphics.drawable.Drawable;

/**
 * @author andy
 * @desc Object to hold details of a knots item
 *
 *  <item>
 *    <dirname><![CDATA[Magnum Force]]></dirname>
 *    <dir><![CDATA[1,1]]></dir>
 *    <id>0</id>
 *  </item>
 *
 */

public class KnotsItem {

	
	enum ItemType {
		UNINITIALIZED,
		CATEGORY,
		TAG,
		VALUE,
		ITEM,
		DIR,
		VIRTUAL,
		SERVER,
		BUTTON
	};
	
	
	/**
	 * 
	 */
	private String mid;
	private String id;
	private String directoryId;
	private String text;
	private ItemType type;
	private Drawable itemImage;
	private Hashtable<String,String> fields;
	private String mediaType;
	private int intId;
	
	public KnotsItem() {
		type = ItemType.UNINITIALIZED;
		fields = new Hashtable<String, String>();		
	}
	
	public synchronized int getIntId() {
		return intId;
	}

	public synchronized String getMediaType() {
		return mediaType;
	}
	public synchronized void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	public synchronized String getMid() {
		return mid;
	}
	public synchronized void setMid(String mid) {
		this.mid = mid;
	}
	public synchronized String getId() {
		return id;
	}
	public synchronized void setId(String id) {
		this.id = id;
		this.intId = Integer.valueOf(id);		
	}
	public synchronized String getDirectoryId() {
		return directoryId;
	}
	public synchronized void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	public synchronized String getText() {
		return text;
	}
	
	
	public synchronized ItemType getType() {
		
		if( type == ItemType.UNINITIALIZED ) {
							
			Set<String> keys = fields.keySet();
			
			for (String key : keys ) {
				
				if( key == "virtual" ) {
					type = ItemType.VIRTUAL;					
				}else if( key == "category" ) {
					type = ItemType.CATEGORY;
				}else if( key == "tag" ) {
					type = ItemType.TAG;
				}else if( key == "value" ) {
					type = ItemType.VALUE;
				}else if( key == "dirname") {
					type = ItemType.DIR;
				}else if( key == "server" ) {
					type = ItemType.SERVER;
				}else if( key == "button" ) {
					type = ItemType.BUTTON;
				}

				if( type != ItemType.UNINITIALIZED ) {
					text = fields.get(key);
					break;			
				}				
			}
			
			if( type==ItemType.UNINITIALIZED ) {
				type = ItemType.ITEM;
				text = fields.get("name");
			}				
		}
		
		return type;
	}
	
	private synchronized void setType() {
		ItemType calculatedType = getType();
	}
	public synchronized Drawable getItemImage() {
		return itemImage;
	}
	public synchronized void setItemImage() {
		if( itemImage == null )
		{		
			try
			{
				if( !( mid == null ) ){		
					String url = new String( Knots.getContext().getString(R.string.server) + "/root/resource_file?type=screenshot&mid=" + mid + "&mediatype=0" );
					InputStream is = (InputStream) new URL(url).getContent();
					itemImage = Drawable.createFromStream(is, "src name");		    
				}
				else {
					switch( type ) {
					case DIR:
						itemImage = Knots.getContext().getResources().getDrawable(R.drawable.knots_dir);
						break;
					case SERVER:
						itemImage = Knots.getContext().getResources().getDrawable(R.drawable.knots_item_server);
						break;
					case ITEM:
						if( mediaType == null || mediaType == "1" )
							itemImage =  Knots.getContext().getResources().getDrawable(R.drawable.knots_item_video);
						else
							itemImage =  Knots.getContext().getResources().getDrawable(R.drawable.knots_item_music);	
						break;
					}
				}

			}catch (Exception e) {
					System.out.println("Exc="+e);	    
			}		
		}
	}
	
	public synchronized Hashtable<String, String> getFields() {
		return fields;
	}
	public synchronized void setFields(Hashtable<String, String> fields) {
		this.fields = fields;
	}
	
	public void retrieveData() {
		setType();	
		setItemImage();
	}

	public void itemSelected()
	{
		switch( type ) {
		case SERVER:
				/*
				 * knots.get_connection().select_server(get_item_id());
				 * knots.connect_server();
				 * */
			break;
		case DIR:
				Knots.getKnots().browseByPath(fields.get("dir"));
				break;
		case VIRTUAL:
				//knots.get_browser().show_virtual_category(get_item_attribute("search"), 1);
				break;
		case ITEM:
				//knots.get_info().show_info(this);
				break;				
		}
	}
}