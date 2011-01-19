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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.DialogInterface;
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

import android.media.*;


public class KnotsListView extends Activity {

	ImageDownloader mImageDownloader;
	ListView list;
	KnotsListAdapter mAdapter;
	Stack<String> mPaths = new Stack<String>();
	String mCurrentPath;
	private Knots mApplication;
	private long mLastPress = -1;
	private static final long BACK_THRESHOLD = DateUtils.SECOND_IN_MILLIS / 2;
	KnotsListDownload mTask;

	@Override
	public void onDestroy() {		
		if(mTask != null && mTask.getStatus()==AsyncTask.Status.RUNNING)
			mTask.cancel(true);

		mTask = null;
		super.onDestroy();
	}

	public KnotsListAdapter getAdapter() {
		return mAdapter;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		mImageDownloader = new ImageDownloader();

		super.onCreate(savedInstanceState);
		mApplication = (Knots) getApplication();

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

	private void cancelCurrentLoad() {
		try {
			if( mTask != null && mTask.getStatus()==AsyncTask.Status.RUNNING ) {			
				mTask.cancel(true);
				mTask.wait();
			}
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadDirectory(String currentPath) {

		// if we are asked to load an item while still loading current one, 
		// cancel it and kick off new download.
		cancelCurrentLoad();

		// create new task

		mTask=new KnotsListDownload();
		DownloadTaskArgs args = new DownloadTaskArgs(currentPath, this);
		mTask.setArgs(args);
		Void executeArgs = null;
		mTask.execute(executeArgs);	

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
						+ mApplication.getSessionId() 
						+ "&filter=" + newPath
						+ "&groupBy=virtualPath"
						+ "&fields=path.fileName,date,thumbnailId,totalAccessCount,width,height,lastPlayPosition,title"
						+ "&sortBy=path.fileName");

			} else if ( intent.getStringExtra(Knots.ACTIONID).equals("play")) {


				String mediaFile = intent.getStringExtra(Knots.MEDIAID);
				startPlayer( mediaFile );							        
			}

		} else if( Intent.ACTION_MAIN.equals( action ) ) {

			login();

			browseRootPath("http://api.orb.com/orb/xml/media.search?sid=" 
					+ mApplication.getSessionId() 
					+ "&q=mediaType%3Dvideo"
					+ "&groupBy=virtualPath"
					+ "&fields=path.fileName,date,path.fileName,date,thumbnailId,totalAccessCount,width,height,lastPlayPosition,title"
					+ "&sortBy=path.fileName");
		}

	}

	private void onSetConnectionType() {

		final CharSequence[] theConnections = {"Home WLan", "Remote WLan", "Mobile"};  

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);  

		alt_bld.setIcon(R.drawable.knots_item_video);  

		alt_bld.setTitle("Select a connection profile");  

		alt_bld.setSingleChoiceItems(theConnections, 0, new DialogInterface.OnClickListener() {  

			public void onClick(DialogInterface dialog, int item) {  

				switch( item ) {
				case 0:
					mApplication.setHeight("480");
					mApplication.setWidth("800");
					mApplication.setSpeed("1200");
					break;
				case 1:
					mApplication.setHeight("360");
					mApplication.setWidth("600");
					mApplication.setSpeed("450");

					break;
				case 2:
					mApplication.setHeight("300");
					mApplication.setWidth("500");
					mApplication.setSpeed("300");
					break;
				}
				dialog.dismiss();	
			}  
		});  

		AlertDialog alert = alt_bld.create();  

		alert.show();  

	}

	private void startPlayer(String mediaFile) {

		try {

			// Treat as internal link only if valid Uri and host matches
			String mediaUri = "http://api.orb.com/orb/xml/stream?sid=" 
				+ mApplication.getSessionId() 
				+ "&mediumId=" + mediaFile
				+ "&streamFormat=wmv"
				+ "&type=pda" 
				+ "&width=" + mApplication.getWidth()
				+ "&height=" + mApplication.getHeight()
				+ "&speed=" + mApplication.getBitrate();


			URL playerStreamSetupUrl = new URL( mediaUri );

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsStreamHandler streamHandler = new KnotsStreamHandler();

			xr.setContentHandler(streamHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(playerStreamSetupUrl .openStream()));

			Intent i = new Intent(Intent.ACTION_VIEW );
			i.setClass(getApplicationContext(), knots2.browser.PlayerActivity.class );
			Uri u = Uri.parse(streamHandler.getUri());
			i.setType("video/x-ms-wmv");
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
			nextIntent.setClass(mApplication.getApplicationContext(), KnotsListView.class);
			startActivity(nextIntent);
		}
	};

	public void login()
	{

		try {
			URL login = new URL( "http://api.orb.com/orb/xml/session.login?apiKey=" 
					+ mApplication.getApiKey() 
					+ "&l=" + mApplication.getUsername()
					+ "&password=" + mApplication.getPassword() );

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsLoginHandler loginHandler = new KnotsLoginHandler();

			xr.setContentHandler(loginHandler);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(login.openStream()));

			mApplication.setSessionId(loginHandler.getSessionId());
			mApplication.setOrbVersion(loginHandler.getOrbVersion());
			mApplication.setMaxInactiveTime(loginHandler.getMaxInactiveTime());

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
			case R.id.itemSetConnectionType:
				onSetConnectionType();
				break;
			}			
		}
		return true;
	}

	public abstract interface KnotsListHandlerObserver {
		abstract public void onNewItem( KnotsListHandlerUpdate newItem );
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
	
	static public class KnotsListHandlerUpdate {
		private KnotsItem mItem;
		private int mTotalItems;
		private int mCurrentItem;
		
		/**
		 * @return the mItem
		 */
		public KnotsItem getItem() {
			return mItem;
		}
		/**
		 * @param mItem the mItem to set
		 */
		public void setItem(KnotsItem item) {
			this.mItem = item;
		}
		/**
		 * @return the mTotalItems
		 */
		public int getTotalItems() {
			return mTotalItems;
		}
		/**
		 * @param mTotalItems the mTotalItems to set
		 */
		public void setTotalItems(int totalItems) {
			this.mTotalItems = totalItems;
		}
		/**
		 * @return the mCurrentItem
		 */
		public int getCurrentItem() {
			return mCurrentItem;
		}
		/**
		 * @param mCurrentItem the mCurrentItem to set
		 */
		public void setCurrentItem(int currentItem) {
			this.mCurrentItem = currentItem;
		}
	}

	public class KnotsListDownload extends AsyncTask<Void, KnotsListHandlerUpdate, Void > implements KnotsListHandlerObserver {

		DownloadTaskArgs mArgs;
		
		ProgressDialog dlg;

		private ProgressDialog progressDialog;

		public void setArgs( DownloadTaskArgs args ) {
			mArgs = args;
		}

		private void loadDirectory( String pathToLoad ) {

			try {

				/* Create a URL we want to load some xml-data from. */
				URL url = new URL( pathToLoad );

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

		public void onNewItem(KnotsListHandlerUpdate newItem) {
			publishProgress(newItem);			
		}

		protected void onProgressUpdate(KnotsListHandlerUpdate... progress) {     

			progressDialog.setMax(progress[0].getTotalItems());
			progressDialog.setProgress(progress[0].getCurrentItem());
			mArgs.getList().addItem(progress[0].getItem());     
		}

		@Override
		protected Void doInBackground(Void... params) {
			loadDirectory(mArgs.getPath());
			return null;
		}

		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
		}
		
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(KnotsListView.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);			
			progressDialog.setMessage("Loading ... ");
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(true);
			progressDialog.setMax(0);
			progressDialog.setProgress(0);
			progressDialog.show();
			
			mArgs.getList().clear();
		}
	}
}
