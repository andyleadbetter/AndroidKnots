/**
 * 
 */
package knots2.browser;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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
	private static final String[] types = {"category", "tag", "value", "name", "dirname", "virtual", "server", "button"};

	/**
	 * 
	 */
	private int type;
	private Hashtable<String,String> fields;
	private Knots mApplication;
	private ImageView imageView;


	public KnotsItem(Knots application) {
		mApplication = application; 
		fields = new Hashtable<String, String>();		
	}

	
	public synchronized String getMediaType() {
		return fields.get("mediaType");
	}
	public synchronized String getMid() {
		return fields.get("mid");
	}
	public synchronized String getId() {
		return fields.get("id");
	}
	public synchronized String getDirectoryId() {
		return fields.get("directoryId");
	}
	public synchronized String getText() {
		return fields.get(types[type]);
	}

	public synchronized int getType() {

		return type;
	}
	public void dataFinished()
	{
		for (int i = 0; i < types.length; i++)
		{
			if (fields.keySet().contains(types[types.length - 1 - i]))
			{				
				type = types.length - 1 - i;
				break;
			}
		}
	}


	public synchronized void getItemImage(BaseAdapter adapter, ImageView imagePlaceHolder, Knots application) {

		try
		{
			String mid = fields.get("mid");
			
			if ( fields.keySet().contains("mid") && !mid.equals("" )) {
				String url = new String(  application.getHost() + "/root/resource_file?type=screenshot&mid=" + fields.get("mid") + "&mediatype=0" );									
				mApplication.getImageDownloadCache().bind(adapter, imagePlaceHolder, url);					
			} else if( type == DIR ) {
				imagePlaceHolder.setImageResource(R.drawable.knots_dir);
			} else if ( type == SERVER ) {
				imagePlaceHolder.setImageResource(R.drawable.knots_item_server);
			} else if (type == ITEM) {
				String mediaType = fields.get("mediatype");
				if( mediaType.equals(null) ||  mediaType.equals("1" ))
					imagePlaceHolder.setImageResource(R.drawable.knots_item_video);
				else
					imagePlaceHolder.setImageResource(R.drawable.knots_item_music);								
			} else {
				imagePlaceHolder.setImageResource(R.drawable.knots_dir);
			}

		}catch (Exception e) {
			System.out.println("Exc="+e);	    
		}		
	}

	public synchronized Hashtable<String, String> getFields() {
		return fields;
	}
	public synchronized void setFields(Hashtable<String, String> fields) {
		this.fields = fields;
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