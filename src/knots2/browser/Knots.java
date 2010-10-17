package knots2.browser;

import java.net.URL;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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


public class Knots extends Activity {

	private Vector<Profile> profiles;	
	private int currentProfile = 6;
	private static final int PROFILES_GROUP = 1;	
	private static Knots instance;
    ImageLoader imageLoader;
    ListView list;
    LazyAdapter adapter;
    Stack<String> paths;
    String currentPath;
    String playerId;    
    
    public Knots() {
        instance = this;	        
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
    
	public static Knots getKnots() {
		return instance;
	}

	@Override
	public void onDestroy() {		
		stopVideo();
		super.onDestroy();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageLoader =new ImageLoader(getApplicationContext());
        
        paths = new Stack<String>();
                
        /* now try to get available profiles */
		profiles = loadProfiles();
		
	    list=(ListView)findViewById(R.id.list);
	    adapter=new LazyAdapter(this);
	    list.setAdapter(adapter);	
	    list.setOnItemClickListener(listener);
		
	    browseByPath("");
	    
	    Button b=(Button)findViewById(R.id.goback);
	    b.setOnClickListener(btnListener);
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
        @Override
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

	public void playVideo(String mid) {
			
		HttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(Knots.getContext().getString( R.string.server ) + "/external/play?profile=" + Integer.toString(currentProfile) +"&id=" + mid);
		String txtResult = new String();
        try{
            HttpResponse response = client.execute(method);
            txtResult = HttpHelper.request(response);     
            playerId = txtResult.split(":")[0];
        
            setContentView(R.layout.videoview);
            MediaController mc = (MediaController) findViewById(R.id.mediacontrollerid);
            VideoView videoView = (VideoView) findViewById(R.id.videoviewid);
            
            mc.setAnchorView(videoView);
            Uri video = Uri.parse("rtsp://192.168.0.28:8080/stream.sdp");
            videoView.setMediaController(mc);
            videoView.setVideoURI(video);
            videoView.start();
	            
        }catch(Exception ex){
            txtResult = "Failed!";
        }
        
	}
	

	public void stopVideo( )
	    {
		/* Create a URL we want to load some xml-data from. */
		if( playerId != null ) {
			/* Create a URL we want to load some xml-data from. */		
			HttpClient client = new DefaultHttpClient();
			HttpGet method = new HttpGet(Knots.getContext().getString( R.string.server ) + "/root/stop?id=" + playerId );
			String txtResult = new String();
			try{
				HttpResponse response = client.execute(method);
				txtResult = HttpHelper.request(response);
	            setContentView(R.layout.main);
			}catch(Exception ex){
				txtResult = "Failed!";
			}
		}
	}	
}
