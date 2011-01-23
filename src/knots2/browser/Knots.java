package knots2.browser;

import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Application;

public class Knots extends Application {

	public static final String KNOTS_INTENT_ACTION_PLAY = "knots2.browser.action.play";
	public static final String KNOTS_INTENT_EXTRA_MEDIA = "knots2.browser.media" ;
	public static final String KNOTS_INTENT_EXTRA_PATH = "knots2.browser.path" ;
	
	private String playerId;    
	private String password;
	private String media;
	private String host;
	private int mCurrentProfile = 6;
	private ImageDownloader mImageDownloadCache;
	private Vector<Profile> mProfiles;	

	public Vector<Profile> getProfiles() {
		return mProfiles;
	}
	
	
	public Knots() {
		mImageDownloadCache = new ImageDownloader();
		mProfiles = loadProfiles();
	}
	
	private Vector<Profile> loadProfiles() {

		Vector<Profile> profiles = null;

		try {
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL(getHost() + "/external/transcoding_profiles");

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsProfilesHandler myExampleHandler = new KnotsProfilesHandler();
			xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */

			profiles = myExampleHandler.getParsedData();
		}

		catch (Exception e) {
			/* Display any Error to the GUI. */			
		}

		return profiles;

	}
	
	public ImageDownloader getImageDownloadCache() {
		return mImageDownloadCache;
	}


	/**
	 * @return the playerId
	 */
	public String getPlayerId() {
		return playerId;
	}
	/**
	 * @param playerId the playerId to set
	 */
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the media
	 */
	public String getMedia() {
		return media;
	}
	/**
	 * @param media the media to set
	 */
	public void setMedia(String media) {
		this.media = media;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return "http://192.168.0.28:1978";
	}
	
	public int getCurrentProfile() {
		return mCurrentProfile;
	}
	
	public void setCurrentProfile(int i) {
		// TODO Auto-generated method stub
		mCurrentProfile = i;
	}	
}
