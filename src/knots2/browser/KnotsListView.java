package knots2.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class KnotsListView extends Activity {

	ImageDownloader mImageDownloader;
	ListView list;
	KnotsListAdapter mAdapter;

	public KnotsListAdapter getAdapter() {
		return mAdapter;
	}

	Stack<String> mPaths = new Stack<String>();
	String mCurrentPath;
	private Knots application;
	private long mLastPress = -1;

	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	
	KnotsListDownload mTask;

	@Override
	public void onDestroy() {		

		super.onDestroy();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		mImageDownloader = new ImageDownloader();

		super.onCreate(savedInstanceState);
		application = (Knots) getApplication();

		setContentView(R.layout.main);

		list=(ListView)findViewById(R.id.list);
		mAdapter=new KnotsListAdapter(this, mImageDownloader);

		list.setAdapter(mAdapter);	
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
		// Otherwise fall through to parent
		// Handle back key as long we have a history stack
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// Compare against last pressed time, and if user hit multiple times
			// in quick succession, we should consider bailing out early.
			long currentPress = SystemClock.uptimeMillis();
			if ( (currentPress - mLastPress < BACK_THRESHOLD ) ) {
				return super.onKeyDown(keyCode, event);
			}
			mLastPress = currentPress;

			// Normal press of back then is there a previous path
			if( mPaths.isEmpty()){
				// Reached root so end activity
				this.finish();
			} else {
				// go up one level
				mCurrentPath = mPaths.pop();
				loadDirectory(mCurrentPath);

				return true;
			}
		}

		// Default do what ever super class decides.

		return super.onKeyDown(keyCode, event);
	}

	private void loadDirectory(String currentPath) {
		if(mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED ){
			mTask=new KnotsListDownload();
			DownloadTaskArgs args = new DownloadTaskArgs(currentPath, this);
			mTask.setArgs(args);
			Void executeArgs = null;
			mTask.execute(executeArgs);	
		}				
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

			if( intent.getStringExtra(Knots.ACTIONID).equals("browseVirtual")) {


				// Treat as internal link only if valid Uri and host matches
				String newPath = intent.getStringExtra( Knots.SEARCHID );
				browseByPath("http://api.orb.com/orb/xml/media.search?sid=" 
						+ application.getSessionId() 
						+ "&filter=" + newPath
						+ "&groupBy=virtualPath"
						+ "&fields=path.fileName,date,thumbnailId,totalAccessCount,width,height,lastPlayPosition,title"
						+ "&sortBy=path.fileName");

			} else if ( intent.getStringExtra(Knots.ACTIONID).equals("play")) {

				// Treat as internal link only if valid Uri and host matches
				String mediaFile = "http://api.orb.com/orb/xml/stream?sid=" 
					+ application.getSessionId() 
					+ "&mediumId=" + intent.getStringExtra(Knots.MEDIAID)
					+ "&streamFormat=asx"
					+ "&type=pc" 
					+ "&width=" + application.getWidth()
					+ "&height=" + application.getHeight()
					+ "&speed=" + application.getBitrate();

				startPlayer( mediaFile );							        
			}

		} else if( Intent.ACTION_MAIN.equals( action ) ) {

			login();

			browseRootPath("http://api.orb.com/orb/xml/media.search?sid=" 
					+ application.getSessionId() 
					+ "&q=mediaType%3Dvideo"
					+ "&groupBy=virtualPath"
					+ "&fields=path.fileName,date,path.fileName,date,thumbnailId,totalAccessCount,width,height,lastPlayPosition,title"
					+ "&sortBy=path.fileName");
		}

	}

	private void startPlayer(String mediaFile) {

		try {
			URL login = new URL( mediaFile );

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsStreamHandler streamHandler = new KnotsStreamHandler();

			xr.setContentHandler(streamHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(login.openStream()));

			Intent i = new Intent(Intent.ACTION_VIEW);
			Uri u = Uri.parse(streamHandler.getUri());
			i.setData(u);
			startActivity(i);

		} catch (MalformedURLException e) {
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

	public OnItemClickListener listener=new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {

			KnotsListAdapter.ViewHolder vh = (KnotsListAdapter.ViewHolder)view.getTag();
			Intent nextIntent = vh.item.itemSelected();
			nextIntent.setClass(application.getApplicationContext(), KnotsListView.class);
			startActivity(nextIntent);
		}
	};

	public void login()
	{

		try {
			URL login = new URL( "http://api.orb.com/orb/xml/session.login?apiKey=" 
					+ application.getApiKey() 
					+ "&l=" + application.getUsername()
					+ "&password=" + application.getPassword() );

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsLoginHandler loginHandler = new KnotsLoginHandler();

			xr.setContentHandler(loginHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(login.openStream()));

			application.setSessionId(loginHandler.getSessionId());
			application.setOrbVersion(loginHandler.getOrbVersion());
			application.setMaxInactiveTime(loginHandler.getMaxInactiveTime());

		} catch (MalformedURLException e) {
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

	public void browseByPath( String pathToBrowse )
	{		
		mPaths.push(mCurrentPath);
		mCurrentPath = pathToBrowse;
		loadDirectory(pathToBrowse);
	}

	public void browseRootPath( String pathToBrowse )
	{		
		mCurrentPath = pathToBrowse;
		loadDirectory(pathToBrowse);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		MenuInflater inflater = getMenuInflater();	    
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch( item.getGroupId() )
		{
		default:
			switch (item.getItemId()) {
			case R.id.quit:
				super.finish();
				break;
			}			
		}
		return true;
	}

	public abstract interface KnotsListHandlerObserver {
		abstract public void onNewItem( KnotsItem newItem );
	}
	
	 private class DownloadTaskArgs {
		 private String mPath;
		 private KnotsListView mView;
		 
		 public DownloadTaskArgs( String path, KnotsListView view ) {
			 mView = view;
			 mPath = path;
		 }
		 
		 public synchronized String getPath() {
			return mPath;
		 }
		

		
		public synchronized KnotsListAdapter getList(){
			return mView.getAdapter();
		}
	}

	public class KnotsListDownload extends AsyncTask<Void, KnotsItem, Void > implements KnotsListHandlerObserver {

		DownloadTaskArgs mArgs;
		
		public void setArgs( DownloadTaskArgs args ) {
			mArgs = args;
		}
		
		private void loadDirectory( String pathToLoad ) {
			
			try {

				// this is the base path to pull items from
				String externalPath = pathToLoad;

				/* Create a URL we want to load some xml-data from. */
				URL url = new URL( externalPath );

				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				XMLReader xr = sp.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader*/ 
				KnotsListHandler folderHandler = new KnotsListHandler((KnotsListHandlerObserver)this);

				xr.setContentHandler(folderHandler);

				/* Parse the xml-data from our URL. */
				xr.parse(new InputSource(url.openStream()));
				/* Parsing has finished. */

			} 

			catch (Exception e) {
				/* Display any Error to the GUI. */						
			}	
		}

	
		public void onNewItem(KnotsItem newItem) {
			publishProgress(newItem);			
		}
		
		  protected void onProgressUpdate(KnotsItem... progress) {         
			  mArgs.getList().addItem(progress[0]);     
		  }

		@Override
		protected Void doInBackground(Void... params) {
			loadDirectory(mArgs.getPath());
			return null;
		}
		
	     protected void onPostExecute(Void result) {
	    	 
	     }
	     protected void onPreExecute() {
	    	 mArgs.getList().clear();
			
		}
	}
}
