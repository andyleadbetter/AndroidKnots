package knots2.browser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import knots2.browser.KnotsListView.KnotsListDownload;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;


public class KnotsListView extends Activity {

	private int currentProfile = 9;

	private static final int PROFILES_GROUP = 1;	


	ListView mList;
	KnotsAdapter mApdapter;		
	private Knots mApplication;
	private long mLastPress = -1;

	private KnotsListDownload mTask;

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
			case R.id.login:
				onShowLoginDialog();
				break;
			}

		}
		return true;
	}

	private void onShowLoginDialog() {
		LayoutInflater inflater=LayoutInflater.from(this);
		View addView=inflater.inflate(R.layout.serveroptions, null);
		final DialogWrapper wrapper=new DialogWrapper(addView);

		new AlertDialog.Builder(this)
		.setTitle(R.string.loginOptions)
		.setView(addView)
		.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				mApplication.setHost(wrapper.getServer());
				mApplication.setUser(wrapper.getUser());
				mApplication.setMediaPassword(wrapper.getPassword());
			}
		})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				// ignore, just dismiss
			}
		})
		.show();
	}

	class DialogWrapper {
		EditText serverField=null;
		EditText userField=null;
		EditText passwordField=null;
		View base=null;

		DialogWrapper(View base) {
			this.base=base;
			serverField=(EditText)base.findViewById(R.id.EditText_server);
			userField=(EditText)base.findViewById(R.id.EditText_server);
			passwordField=(EditText)base.findViewById(R.id.EditText_pw);
		}

		String getServer() {
			return(getServerField().getText().toString());
		}

		String getUser() {
			return(getUserField().getText().toString());
		}

		String getPassword() {
			return(getPasswordField().getText().toString());
		}

		private EditText getUserField() {
			if (userField==null) {
				userField=(EditText)base.findViewById(R.id.EditText_user);
			}				
			return(userField);
		}

		private EditText getPasswordField() {
			if (passwordField==null) {
				passwordField=(EditText)base.findViewById(R.id.EditText_pw);
			}				
			return(passwordField);
		}



		private EditText getServerField() {
			if (serverField==null) {
				serverField=(EditText)base.findViewById(R.id.EditText_server);
			}				
			return(serverField);
		}
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




private void addProfiles( Menu mainMenu ) {

	SubMenu profilesMenu = mainMenu.addSubMenu(R.string.profiles_menu);
	profilesMenu.setGroupCheckable( PROFILES_GROUP, true, true);
	profilesMenu.setIcon(R.drawable.knots_button_player);

	Iterator<Profile> itr = mApplication.getProfiles().iterator();		
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
public abstract interface KnotsListHandlerObserver {
	abstract public void onNewItem( KnotsListHandlerUpdate newItem );
	abstract public Knots getApplication();
}

private class DownloadTaskArgs {
	private String mPath;
	private KnotsListView mView;		

	public DownloadTaskArgs( String path, KnotsListView view) {
		mView = view;
		mPath = path;
	}

	public synchronized String getPath() {
		return mPath;
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

	public void setArgs( DownloadTaskArgs args ) {
		mArgs = args;
	}
	public Knots getApplication() {
		return (Knots)mArgs.mView.getApplication();
	}

	private void loadDirectory( DownloadTaskArgs args ) {
		HttpURLConnection urlConnection = null;
		try {
			 
			/* Create a URL we want to load some xml-data from. */
			URL url = new URL( args.getPath() );
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setUseCaches(true);
			urlConnection.connect();

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			KnotsListHandler folderHandler = new KnotsListHandler((KnotsListHandlerObserver)this);

			xr.setContentHandler(folderHandler);

			/* Parse the xml-data from our URL. */
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());

			xr.parse(new InputSource(in));
			/* Parsing has finished. */

		} 

		catch (Exception e) {
			/* Display any Error to the GUI. */						
		}	
		finally {     
			urlConnection.disconnect();   
		}

	}

	public void onNewItem(KnotsListHandlerUpdate newItem) {
		publishProgress(newItem);			
	}

	protected void onProgressUpdate(KnotsListHandlerUpdate... progress) {     

		mArgs.mView.mApdapter.addItem(progress[0].getItem());     
	}

	@Override
	protected Void doInBackground(Void... params) {
		loadDirectory(mArgs);
		return null;
	}

	protected void onPostExecute(Void result) {
	}

	protected void onPreExecute() {		
		mArgs.mView.mApdapter.clear();
	}
}
}
