package knots2.browser;

import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class Knots extends Activity {

	private Vector<Profile> profiles;
	private Vector<KnotsItem> items;
	private int currentProfile = 6;
	private static final int PROFILES_GROUP = 1;	
	private static Knots instance;
    ImageLoader imageLoader;
    ListView list;
    LazyAdapter adapter;

    public Knots() {
        instance = this;	        
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
    
	public static Knots getKnots() {
		return instance;
	}
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageLoader =new ImageLoader(getApplicationContext());
        
        /* now try to get available profiles */
		profiles = loadProfiles();
		
		/* Get root Directory */
		items = loadDirectory( "" );
		
	    list=(ListView)findViewById(R.id.list);
	    adapter=new LazyAdapter(this, items);
	    list.setAdapter(adapter);	
	    list.setOnItemClickListener(listener);

	}
	
	public OnItemClickListener listener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {

			LazyAdapter.ViewHolder vh = (LazyAdapter.ViewHolder)view.getTag();
			vh.item.itemSelected();
			
		}

    };
    
    
	public void browseByPath( String pathToBrowse )
	{
		items = loadDirectory(pathToBrowse);		
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
	
	private Vector<KnotsItem> loadDirectory( String pathToLoad ) {

		Vector<KnotsItem> items = null;
		
		try {
			
			// this is the base path to pull items from
			String externalPath = Knots.getContext().getString( R.string.server ) + "/external/browse?format=xml";
			
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
			xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */

			/* Our KnotsListHandler now provides the parsed data to us. */
			items = myExampleHandler.getParsedData();

		} 
		
		catch (Exception e) {
			/* Display any Error to the GUI. */						
		}	
		
		return items;
		
	}

	private Vector<Profile> loadProfiles() {

		Vector<Profile> profiles = null;
		
		try {
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL(Knots.getContext().getString( R.string.server ) + "/external/transcoding_profiles");

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
