package knots2.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

	private Vector<Profile> profiles;	
	private int currentProfile = 6;
	private static final int PROFILES_GROUP = 1;	
	private static KnotsListView instance;
	ImageLoader imageLoader;
	ListView list;
	KnotsAdapter adapter;
	Stack<String> paths;
	String currentPath;
	private Knots application;
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
		application = (Knots) getApplication();
		
		setContentView(R.layout.main);

		imageLoader =new ImageLoader(application.getApplicationContext());

		paths = new Stack<String>();

		/* now try to get available profiles */
		profiles = loadProfiles();

		list=(ListView)findViewById(R.id.list);
		adapter=new KnotsAdapter(this);
		list.setAdapter(adapter);	
		list.setOnItemClickListener(listener);

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
        // Handle back key as long we have a history stack
        if (keyCode == KeyEvent.KEYCODE_BACK && !paths.empty()) {

            // Compare against last pressed time, and if user hit multiple times
            // in quick succession, we should consider bailing out early.
            long currentPress = SystemClock.uptimeMillis();
            if (currentPress - mLastPress < BACK_THRESHOLD) {
                return super.onKeyDown(keyCode, event);
            }
            mLastPress = currentPress;

            // Pop last entry off stack and start loading
            String lastEntry = paths.pop();
            loadDirectory(lastEntry);

            return true;
        }

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
            // Treat as internal link only if valid Uri and host matches
            String newPath = intent.getStringExtra( "knots2.browser.path" );
            if( newPath != null )
              {
            	loadDirectory(newPath);
              }
        } else if( Intent.ACTION_MAIN.equals( action ) ) {
        	browseByPath("");
        }
        	
    }
	
	public OnItemClickListener listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {

			KnotsAdapter.ViewHolder vh = (KnotsAdapter.ViewHolder)view.getTag();
			Intent nextIntent = vh.item.itemSelected();
			
			if( nextIntent.getStringExtra("knots2.browser.action")=="browse")
			{
				nextIntent.setClass(application.getApplicationContext(), KnotsListView.class);				
				startActivity(nextIntent);	
			} else if( nextIntent.getStringExtra("knots2.browser.action")=="play") {				
				nextIntent.setClass(application.getApplicationContext(), KnotsPlayer.class);
				startActivity(nextIntent);
			}
				
			
		}
	};

	public void browseByPath( String pathToBrowse )
	{		
		paths.push(currentPath);
		currentPath = pathToBrowse;
		loadDirectory(pathToBrowse);

	}


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

	public OnClickListener btnListener=new OnClickListener(){
		public void onClick(View arg0) {
			try {
				currentPath = paths.pop();
			}
			catch(EmptyStackException e)
			{
				currentPath="";
			}

			loadDirectory(currentPath);
		}
	};

	private void loadDirectory( String pathToLoad ) {

		adapter.clear();

		try {

			// this is the base path to pull items from
			String externalPath =application.getApplicationContext().getString( R.string.server ) + "/external/browse?format=xml";

			// Add the new sub tree to the URL, the root path is fetched with empty Path
			if( pathToLoad != "" ) {
				externalPath += "&path=" + pathToLoad;
			}

			/* Create a URL we want to load some xml-data from. */
			URL url = new URL( externalPath );

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsListHandler myExampleHandler = new KnotsListHandler();
			myExampleHandler.setListAdapter( adapter );
			xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */

		} 

		catch (Exception e) {
			/* Display any Error to the GUI. */						
		}	

	}

	private Vector<Profile> loadProfiles() {

		Vector<Profile> profiles = null;

		try {
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL(application.getApplicationContext().getString( R.string.server ) + "/external/transcoding_profiles");

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

		Iterator<Profile> itr = profiles.iterator();		
		while(itr.hasNext())
		{
			Profile profile = itr.next();
			MenuItem newItem = profilesMenu.add( PROFILES_GROUP, profile.getIntegerId(), Menu.NONE, profile.getName());
			// If this item is the current profile tag it as checked.
			newItem.setChecked( newItem.getItemId() == currentProfile );				
		}
	}

	private void setCurrentProfile( MenuItem item ) {	
		currentProfile = item.getItemId();				
	}

}
