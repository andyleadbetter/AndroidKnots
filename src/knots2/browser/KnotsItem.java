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
	private String mid;
	private String id;
	private String directoryId;
	private String directoryNameId;
	private ItemType type;
	private Drawable itemImage;
	private Hashtable<String,String> fields;
	
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
	}
	public synchronized String getDirectoryId() {
		return directoryId;
	}
	public synchronized void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	public synchronized String getDirectoryNameId() {
		return directoryNameId;
	}
	public synchronized void setDirectoryNameId(String directoryNameId) {
		this.directoryNameId = directoryNameId;
	}
	public synchronized ItemType getType() {
		return type;
	}
	public synchronized void setType(ItemType type) {
		this.type = type;
	}
	public synchronized Drawable getItemImage() {
		return itemImage;
	}
	public synchronized void setItemImage(Drawable itemImage) {
		this.itemImage = itemImage;
	}
	public synchronized Hashtable<String, String> getFields() {
		return fields;
	}
	public synchronized void setFields(Hashtable<String, String> fields) {
		this.fields = fields;
	}		
}
