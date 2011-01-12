/**
 * 
 */
package knots2.browser;

import java.util.Hashtable;

import android.content.Intent;

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
	private String text;
	private int type;
	private String itemImage;
	private Hashtable<String,String> mFields;
	
	public KnotsItem() {
		mFields = new Hashtable<String, String>();		
	}
	
	public synchronized String getId() {
		return id;
	}
	public synchronized void setId(String id) {
		this.id = id;
	}
	public synchronized String getText() {
		return mFields.get("title");
	}	
	

	public synchronized void setType( int newType ){
		type = newType;
	}
	
	public synchronized int getType() {
		return type;
	}
	
	
	public synchronized String getItemImage() {
		return itemImage;
	}
	
	public synchronized Hashtable<String, String> getFields() {
		return mFields;
	}
	public synchronized void setFields(Hashtable<String, String> fields) {
		this.mFields= fields;
	}
	
	public Intent itemSelected()
	{
		Intent nextIntent = new Intent();
		nextIntent.setAction(Intent.ACTION_VIEW);
		


		switch( type ) {
		case SERVER:
				/*
				 * knots.get_connection().select_server(get_item_id());
				 * knots.connect_server();
				 * */
			break;
		case DIR:
			nextIntent.putExtra(Knots.PATHID, mFields.get("dir"));				
			nextIntent.putExtra(Knots.ACTIONID, "browse");
			break;

		case VIRTUAL:
				nextIntent.putExtra(Knots.SEARCHID, mFields.get("searchId"));
				nextIntent.putExtra(Knots.ACTIONID, "browseVirtual");
				break;

		case ITEM:								
				nextIntent.putExtra(Knots.MEDIAID, mFields.get("orbMediumId"));				
				nextIntent.putExtra(Knots.ACTIONID, "play");
				break;				
		}
		return nextIntent;		
	}	
	
	/**
	 * @param itemImage the itemImage to set
	 */
	public void setItemImage(String itemImage) {
		this.itemImage = itemImage;
	}
}