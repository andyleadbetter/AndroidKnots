package knots2.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;


public class KnotsListView extends Activity {

	private Vector<Profile> mProfiles;	
	
	private int currentProfile = 9;
	
	private static final int PROFILES_GROUP = 1;	
	
	ImageDownloader mImageLoader;
	ListView mList;
	KnotsAdapter mApdapter;		
	private Knots mApplication;
    private long mLastPress = -1;

    private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	
	@Override
	public void onDestroy() {		
		
		super.onDestroy();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mApplication = (Knots) getApplication();
		
		setContentView(R.layout.main);

		mImageLoader =new ImageDownloader();

		/* now try to get available profiles */
		mProfiles = loadProfiles();

		mList=(ListView)findViewById(R.id.list);
		mApdapter=new KnotsAdapter(this);
		mList.setAdapter(mApdapter);	
		mList.setOnItemClickListener(listener);

        // Handle incoming intents as possible searches or links
        onNewIntent(getIntent());
	}

	   /**
     * Intercept the back-key to try walking backwards along our word history
     * stack. If we don't have any remaining history, the key behaves normally
     * and closes this activity.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Otherwise fall through to parent
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Because we're singleTop, we handle our own new intents. These usually
     * come from the {@link SearchManager} when a search is requested, or from
     * internal links the user clicks on.
     */
    @Override
    public void onNewIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
  
        	String newPath = intent.getStringExtra( Knots.KNOTS_INTENT_EXTRA_PATH );
        	if( newPath != null )
        	{
        		browseByPath(newPath);
        	}
        }
        else if ( Intent.ACTION_SEARCH.equals(action)) {
        	// if we are viewing then theres a new path
        	String newPath = intent.getStringExtra( Knots.KNOTS_INTENT_EXTRA_PATH );
        	if( newPath != null )
        	{
        		browseVirtual(newPath);
        	}
        } else if( Intent.ACTION_MAIN.equals( action ) ) {
        	// going to root path
        	browseByPath("");
        }
        	
    }
	
	public OnItemClickListener listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {

			KnotsAdapter.ViewHolder vh = (KnotsAdapter.ViewHolder)view.getTag();
			Intent nextIntent = vh.item.itemSelected(mApplication);
			startActivity(nextIntent);				
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		

		addProfiles( menu );

		MenuInflater inflater = getMenuInflater();	    
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch( item.getGroupId() )
		{
		case PROFILES_GROUP:
			setCurrentProfile(item);
			break;

		default:
			switch (item.getItemId()) {
			case R.id.quit:
				super.finish();
				break;
			}			
		}
		return true;
	}

	private void browseVirtual( String virtualPath ) {

		// this is the base path to pull items from
		String externalPath = mApplication.getHost() + "/external/browse?virtual=";
		// Add the new sub tree to the URL, the root path is fetched with empty Path
		if( virtualPath != "" ) {
			externalPath += URLEncoder.encode(virtualPath);
		}

		loadDirectory(externalPath);
	}

	
	private void browseByPath( String path ) {
		// this is the base path to pull items from
		String externalPath = mApplication.getHost() + "/external/browse?format=xml";

		// Add the new sub tree to the URL, the root path is fetched with empty Path
		if( path != "" ) {
			externalPath += "&path=" + path;
		}
		
		loadDirectory(externalPath);
	}
	
	private void loadDirectory( String pathToLoad ) {

		mApdapter.clear();

			/* Create a URL we want to load some xml-data from. */
			URL url;
			try {
				url = new URL( pathToLoad );


				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				XMLReader xr = sp.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader*/ 
				KnotsListHandler myExampleHandler = new KnotsListHandler();
				myExampleHandler.setListAdapter( mApdapter );
				xr.setContentHandler(myExampleHandler);

				/* Parse the xml-data from our URL. */
				xr.parse(new InputSource(url.openStream()));
				/* Parsing has finished. */
			}
			catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



	}

	private Vector<Profile> loadProfiles() {

		Vector<Profile> profiles = null;

		try {
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL(mApplication.getHost() + "/external/transcoding_profiles");

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

	private void addProfiles( Menu mainMenu ) {

		SubMenu profilesMenu = mainMenu.addSubMenu(R.string.profiles_menu);
		profilesMenu.setGroupCheckable( PROFILES_GROUP, true, true);
		profilesMenu.setIcon(R.drawable.knots_button_player);

		Iterator<Profile> itr = mProfiles.iterator();		
		while(itr.hasNext())
		{
			Profile profile = itr.next();
			MenuItem newItem = profilesMenu.add( PROFILES_GROUP, profile.getIntegerId(), Menu.NONE, profile.getName());
			// If this item is the current profile tag it as checked.
			newItem.setChecked( newItem.getItemId() == currentProfile );				
		}
	}

	private void setCurrentProfile( MenuItem item ) {	
		mApplication.setCurrentProfile( item.getItemId() );				
	}

}
