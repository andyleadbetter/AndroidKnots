package knots2.browser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.ContentHandler;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.google.android.imageloader.BitmapContentHandler;
import com.google.android.imageloader.ImageLoader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.AndroidCharacter;
import android.util.Base64;

public class Knots extends Application {

	public static final String PREFS_NAME = "MyPrefsFile";    
	
	public static final String KNOTS_INTENT_EXTRA_MEDIA = "knots2.browser.extras.media" ;
	public static final String KNOTS_INTENT_EXTRA_PATH = "knots2.browser.extras.path" ;
	public static final String KNOTS_INTENT_EXTRA_TAG = "knots2.browser.extras.tag" ;
	public static final String KNOTS_INTENT_EXTRA_CATEGORY = "knots2.browser.extras.category" ;
	
	public static final String KNOTS_INTENT_ACTION_PLAY = "knots2.browser.action.play";
	public static final String KNOTS_INTENT_ACTION_VIRTUAL = "knots2.browser.search.virtual";
	public static final String KNOTS_INTENT_ACTION_CATEGORY = "knots2.browser.search.category";
	public static final String KNOTS_INTENT_ACTION_TAG = "knots2.browser.search.tag";
	public static final String KNOTS_INTENT_ACTION_VALUE = "knots2.browser.search.value";

	private String playerId;    
	private String mMediaPassword;
	private String media;
	private String host;
	private int mCurrentProfile = 6;
	private ImageLoader mImageDownloadCache;
	private Vector<Profile> mProfiles;
	private String mUserName;
	private String mUserPassword;
	SharedPreferences settings;   
	TrustManager[] trustAllCerts;

	private int mListLimit;
	public Vector<Profile> getProfiles() {
		return mProfiles;
	}
	private void trustEveryone() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{new X509TrustManager(){
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {}
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {}
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}}}, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(
					context.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}


	public void onCreate() {
		super.onCreate();
		mImageDownloadCache = Knots.createImageLoader(this);
		trustEveryone();
		//		Restore preferences       
		settings = getSharedPreferences(PREFS_NAME, 0);    
		host = settings.getString("", "http://192.168.0.28:1978");
		mUserName = settings.getString("username", "andy");
		mUserPassword = settings.getString("password", "andy");
		mCurrentProfile = settings.getInt("profile", 6);
		mListLimit = settings.getInt("listLimit", 100);

		Authenticator.setDefault( new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUserName(),getUserPassword().toCharArray());     }}); 
		mProfiles = loadProfiles();

	}
	
	void authorizeConnection(HttpURLConnection connection)  {
		try {
			String base64EncodedCredentials = "";
			base64EncodedCredentials = Base64.encodeToString(
			    (getUserName() + ":" + getUserPassword()).getBytes(), Base64.NO_WRAP);
			
			connection.addRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	private static ImageLoader createImageLoader(Context context) {
		// Install the file cache (if it is not already installed)
		FileCache.install(context);

		// Just use the default URLStreamHandlerFactory because
		// it supports all of the required URI schemes (http).
		URLStreamHandlerFactory streamFactory = null;
		
		URL.setURLStreamHandlerFactory(streamFactory); 


		// Load images using a BitmapContentHandler
		// and cache the image data in the file cache.
		ContentHandler bitmapHandler = FileCache.capture(new BitmapContentHandler(), null);

		// For pre-fetching, use a "sink" content handler so that the
		// the binary image data is captured by the cache without actually
		// parsing and loading the image data into memory. After pre-fetching,
		// the image data can be loaded quickly on-demand from the local cache.
		ContentHandler prefetchHandler = FileCache.capture(FileCache.sink(), null);

		// Perform callbacks on the main thread
		Handler handler = null;

		return new ImageLoader(streamFactory, bitmapHandler, prefetchHandler, handler);
	}



	public Knots() {

	}

	private Vector<Profile> loadProfiles() {

		Vector<Profile> profiles = null;
		HttpURLConnection urlConnection;
		try {
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL(getHost() + "/external/transcoding_profiles");

			
			urlConnection = (HttpURLConnection) url.openConnection();
			
			authorizeConnection( urlConnection );

			urlConnection.setUseCaches(true);
			urlConnection.connect();
			
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsProfilesHandler myExampleHandler = new KnotsProfilesHandler();
			xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
			final InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			
			xr.parse(new InputSource(in));
			/* Parsing has finished. */

			profiles = myExampleHandler.getParsedData();
			boolean profileFound = false;
			for (Profile profile : profiles) {

				if( profile.getIntegerId()==mCurrentProfile) {
					profileFound = true;
					break;				
				}
			} 
			if( !profileFound )
				mCurrentProfile = profiles.get(0).getIntegerId();
		}

		catch (Exception e) {
			e.printStackTrace();			
		}

		return profiles;

	}


	
	public ImageLoader getImageDownloadCache() {
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
	public String getMediaPassword() {
		return mMediaPassword;
	}
	/**
	 * @param password the password to set
	 */
	public void setMediaPassword(String password) {
		this.mMediaPassword = password;
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
		SharedPreferences.Editor editor = settings.edit();      
		editor.putString("host", host);           
		editor.commit();
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	public int getCurrentProfile() {
		return mCurrentProfile;
	}

	public void setCurrentProfile(int i) {
		// TODO Auto-generated method stub
		mCurrentProfile = i;
		SharedPreferences.Editor editor = settings.edit();      
		editor.putInt("profile", i);      
		editor.commit();
	}

	public void setUser(String user) {
		mUserName = user;
		SharedPreferences.Editor editor = settings.edit();      
		editor.putString("username", user);      
		editor.commit();
	}

	public void setUserPassword( String pass ) {
		mUserPassword = pass; 
		SharedPreferences.Editor editor = settings.edit();      
		editor.putString("password", pass);      
		editor.commit();
	}


	public String getUserName() {
		return mUserName;
	}

	public String getUserPassword() {
		return mUserPassword;
	}
	public void setListLimit(int limit) {
		mListLimit = limit;		
		SharedPreferences.Editor editor = settings.edit();      
		editor.putInt("listLimit", mListLimit);      
		editor.commit();
	}

	public int getListLimit( ) {
		return mListLimit;
	}
}
