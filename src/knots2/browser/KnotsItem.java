/**
 * 
 */
package knots2.browser;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

	
	public static final int CATEGORY = 0;
	public static final int TAG = 1;
	public static final int VALUE = 2;
	public static final int ITEM = 3;
	public static final int DIR = 4;
	public static final int VIRTUAL = 5;
	public static final int SERVER = 6;
	public static final int BUTTON = 7;
	
	
	/**
	 * 
	 */
	private String mid;
	private String id;
	private String directoryId;
	private String text;
	private int type;
	private Drawable itemImage;
	private Hashtable<String,String> fields;
	private String mediaType;
	private int intId;
	private Context mContext;
	
	
	public KnotsItem(Context context) {
		mContext = context; 
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
	
	
	public synchronized int getType() {
		
		String[] types = {"category", "tag", "value", "name", "dirname", "virtual", "server", "button"};
		for (int i = 0; i < types.length; i++)
		{
			if (fields.keySet().contains(types[types.length - 1 - i]))
			{
				text = (String)fields.get(types[types.length - 1 - i]);
				type = types.length - 1 - i;
				break;
			}
		}
		return type;
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
					String url = new String(  "http://192.168.0.28:1978/root/resource_file?type=screenshot&mid=" + mid + "&mediatype=0" );
					InputStream is = (InputStream) new URL(url).getContent();
					itemImage = Drawable.createFromStream(is, "src name");		    
				} else {
					
					if( type == DIR ) {
						itemImage = mContext.getResources().getDrawable(R.drawable.knots_dir);
					} else if ( type == SERVER ) {
						itemImage = mContext.getResources().getDrawable(R.drawable.knots_item_server);
					} else if (type == ITEM) {
						if( mediaType == null || mediaType == "1" )
							itemImage =  mContext.getResources().getDrawable(R.drawable.knots_item_video);
						else
							itemImage =  mContext.getResources().getDrawable(R.drawable.knots_item_music);								
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
		int local_type = getType();
		setItemImage();
	}

	public Intent itemSelected(Knots application)
	{
		Intent nextIntent = new Intent();
		switch( type ) {
		case SERVER:
			/*
			 * knots.get_connection().select_server(get_item_id());
			 * knots.connect_server();
			 * */
			break;
		case DIR:
			nextIntent.setAction(Intent.ACTION_VIEW);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("dir"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case VIRTUAL:
			nextIntent.setAction(Intent.ACTION_SEARCH);			
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("search"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case ITEM:								
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_MEDIA, getId());				
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_PLAY);
			nextIntent.setClass(application, KnotsPlayer.class);	
			break;				
		}
		return nextIntent;

	}
}