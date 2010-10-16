/**
 * 
 */
package knots2.browser;

import java.util.Hashtable;

import android.graphics.drawable.Drawable;
import android.provider.MediaStore.Images;

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
	public String mid;
	public String id;
	public String directoryId;
	public String directoryNameId;
	public ItemType type;
	public Drawable itemImage;
	public Hashtable<String,String> fields;
		

}
