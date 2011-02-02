/**
 * 
 */
package knots2.browser;

import java.util.Hashtable;

import android.content.Intent;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @author andy
 * @desc Object to hold details of a knots item
 * 
 *       <item> <dirname><![CDATA[Magnum Force]]></dirname>
 *       <dir><![CDATA[1,1]]></dir> <id>0</id> </item>
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
	
	private static final String[] types = { "category", "tag", "value", "name",
			"dirname", "virtual", "server", "button" };
	
	

	private Hashtable<String, String> fields;
	private final Knots mApplication;
	/**
	 * 
	 */
	private int type;

	public KnotsItem(final Knots application) {
		mApplication = application;
		fields = new Hashtable<String, String>();
	}

	public void dataFinished() {
		for (int i = 0; i < KnotsItem.types.length; i++) {
			if (fields.keySet().contains(
					KnotsItem.types[KnotsItem.types.length - 1 - i])) {
				type = KnotsItem.types.length - 1 - i;
				break;
			}
		}
	}

	public synchronized String getDirectoryId() {
		return fields.get("directoryId");
	}

	public synchronized Hashtable<String, String> getFields() {
		return fields;
	}

	public synchronized String getId() {
		return fields.get("id");
	}

	public synchronized void getItemImage(final BaseAdapter adapter,
			final ImageView imagePlaceHolder, final Knots application) {

		try {
			final String mid = fields.get("mid");

			if (fields.keySet().contains("mid") && !mid.equals("")) {
				final String url = new String(application.getHost()
						+ "/root/resource_file?type=screenshot&mid="
						+ fields.get("mid") + "&mediatype=0");
				mApplication.getImageDownloadCache().bind(adapter,
						imagePlaceHolder, url);
			} else if (type == KnotsItem.DIR) {
				imagePlaceHolder.setImageResource(R.drawable.knots_dir);
			} else if (type == KnotsItem.SERVER) {
				imagePlaceHolder.setImageResource(R.drawable.knots_item_server);
			} else if (type == KnotsItem.ITEM) {
				final String mediaType = fields.get("mediatype");
				if (mediaType.equals(null) || mediaType.equals("1")) {
					imagePlaceHolder
							.setImageResource(R.drawable.knots_item_video);
				} else {
					imagePlaceHolder
							.setImageResource(R.drawable.knots_item_music);
				}
			} else {
				imagePlaceHolder.setImageResource(R.drawable.knots_dir);
			}

		} catch (final Exception e) {
			System.out.println("Exc=" + e);
		}
	}

	public synchronized String getMediaType() {
		return fields.get("mediaType");
	}

	public synchronized String getMid() {
		return fields.get("mid");
	}

	public synchronized String getText() {
		return fields.get(KnotsItem.types[type]);
	}

	public synchronized int getType() {

		return type;
	}

	public Intent itemSelected(final Knots application) {
		final Intent nextIntent = new Intent();
		switch (type) {
		case CATEGORY:
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_CATEGORY);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_CATEGORY, fields.get("id"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case DIR:
			nextIntent.setAction(Intent.ACTION_VIEW);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("dir"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case VIRTUAL:
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_VIRTUAL);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("search"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case ITEM:
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_MEDIA, getId());
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_PLAY);
			nextIntent.setClass(application, KnotsPlayer.class);
			break;
		case TAG:
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_TAG);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("id"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		case VALUE:
			nextIntent.setAction(Knots.KNOTS_INTENT_ACTION_VALUE);
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_TAG, fields.get("tag_id"));
			nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_PATH, fields.get("value"));
			nextIntent.setClass(application, KnotsListView.class);
			break;
		}
		return nextIntent;

	}

	public synchronized void setFields(final Hashtable<String, String> fields) {
		this.fields = fields;
	}
}