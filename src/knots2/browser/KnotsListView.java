package knots2.browser;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Iterator;

import knots2.browser.KnotsListDownload.*;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class KnotsListView extends Activity {

	class DialogWrapper implements SeekBar.OnSeekBarChangeListener {
		View base = null;
		EditText passwordField = null;
		EditText serverField = null;
		EditText userField = null;
		TextView limitField = null;
		SeekBar  limitSeeker = null;

		DialogWrapper(final View base, String server, String username, String password, int limit) {
			this.base = base;
			serverField = (EditText) base.findViewById(R.id.EditText_server);
			userField = (EditText) base.findViewById(R.id.EditText_user);
			passwordField = (EditText) base.findViewById(R.id.EditText_pw);
			limitSeeker = ( SeekBar ) base.findViewById(R.id.seekBarLimit);
			limitField = (TextView) base.findViewById(R.id.textView_counter);
			limitSeeker.setOnSeekBarChangeListener(this);
			limitSeeker.setMax(500);	
			limitSeeker.setProgress(limit);
			limitField.setText(Integer.toString(limit));
			serverField.setText(server);
			userField.setText(username);
			passwordField.setText(password);
			
			
		}

		String getPassword() {
			return (getPasswordField().getText().toString());
		}

		private EditText getPasswordField() {
			if (passwordField == null) {
				passwordField = (EditText) base.findViewById(R.id.EditText_pw);
			}
			return (passwordField);
		}

		String getServer() {
			return (getServerField().getText().toString());
		}

		private EditText getServerField() {
			if (serverField == null) {
				serverField = (EditText) base
				.findViewById(R.id.EditText_server);
			}
			return (serverField);
		}

		String getUser() {
			return (getUserField().getText().toString());
		}

		private EditText getUserField() {
			if (userField == null) {
				userField = (EditText) base.findViewById(R.id.EditText_user);
			}
			return (userField);
		}
		
		int getLimit() {
			return (getSeekField().getProgress());
		}

		private SeekBar getSeekField() {
			if ( limitSeeker == null) {
				limitSeeker = (SeekBar) base.findViewById(R.id.seekBarLimit);
			}
			return (limitSeeker);
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if( fromUser )
				limitField.setText(Integer.toString(progress));
			
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
	}

	public abstract interface KnotsListHandlerObserver {
		abstract public Knots getApplication();

		abstract public void onNewItem(KnotsListHandlerUpdate newItem);

		abstract public void onPageUpdate(int currentPage, int totalPages, int mTotalItems);
	}

	static public class KnotsListHandlerUpdate {
		private KnotsItem mItem;

		/**
		 * @return the mItem
		 */
		public KnotsItem getItem() {
			return mItem;
		}

		/**
		 * @param mItem
		 *            the mItem to set
		 */
		public void setItem(final KnotsItem item) {
			mItem = item;
		}
	}

	private static final int PROFILES_GROUP = 1;
	private final int currentProfile = 9;
	public OnItemClickListener listener = new OnItemClickListener() {

		public void onItemClick(final AdapterView<?> parent, final View view,
				final int position, final long id) {

			final KnotsAdapter.ViewHolder vh = (KnotsAdapter.ViewHolder) view
			.getTag();
			final Intent nextIntent = vh.item.itemSelected(mApplication);
			if (nextIntent.getAction().equals(Knots.KNOTS_INTENT_ACTION_TAG)) {
				nextIntent.putExtra(Knots.KNOTS_INTENT_EXTRA_CATEGORY,
						mCurrentCategory);
			}
			startActivity(nextIntent);
		}
	};

	KnotsAdapter mAdapter;

	private Knots mApplication;

	private String mCurrentCategory = "";
	ListView mList;
	private KnotsListDownload mTask;


	private void addProfiles(final Menu mainMenu) {

		final SubMenu profilesMenu = mainMenu
		.addSubMenu(R.string.profiles_menu);
		profilesMenu
		.setGroupCheckable(KnotsListView.PROFILES_GROUP, true, true);
		profilesMenu.setIcon(R.drawable.knots_button_player);

		final Iterator<Profile> itr = mApplication.getProfiles().iterator();
		while (itr.hasNext()) {
			final Profile profile = itr.next();
			final MenuItem newItem = profilesMenu.add(
					KnotsListView.PROFILES_GROUP, profile.getIntegerId(),
					Menu.NONE, profile.getName());
			// If this item is the current profile tag it as checked.
			newItem.setChecked(newItem.getItemId() == currentProfile);
		}
	}

	private void browseByPath(final String path) {
		// this is the base path to pull items from
		String externalPath = mApplication.getHost()
		+ "/external/browse?format=xml";
		// Add the new sub tree to the URL, the root path is fetched with empty
		// Path
		if (path != "") {
			externalPath += "&path=" + path;
		}
		loadDirectory(externalPath);
	}

	private void browseCategory(final String category, final String path, final String value) {
		// this is the base path to pull items from
		String externalPath = mApplication.getHost()
		+ "/external/browse?format=xml";

		// Add the new sub tree to the URL, the root path is fetched with empty
		// Path
		if (category != "") {
			externalPath += "&category=" + category;
		}

		if (path != "") {
			externalPath += "&tag=" + path;
		}

		if( value != "" ) {
			externalPath += "&value=" + value;
		}

		loadDirectory(externalPath);
	}

	private void browseVirtual(final String virtualPath) {

		// this is the base path to pull items from
		String externalPath = mApplication.getHost()
		+ "/external/browse?virtual=";
		// Add the new sub tree to the URL, the root path is fetched with empty
		// Path
		if (virtualPath != "") {
			externalPath += URLEncoder.encode(virtualPath);
		}

		loadDirectory(externalPath);
	}

	private void cancelCurrentLoad() {

		if ((mTask != null) && (mTask.getStatus() == AsyncTask.Status.RUNNING)) {
			mTask.cancel(true);
		}

	}

	private void loadDirectory(final String currentPath) {
		// start tracing to "/sdcard/calc.trace"  

		// if we are asked to load an item while still loading current one,
		// cancel it and kick off new download.
		cancelCurrentLoad();

		// create new task
		mTask = new KnotsListDownload();
		DownloadTaskArgs args = mTask.new DownloadTaskArgs(currentPath, this);
		mTask.setArgs(args);
		final Void executeArgs = null;
		mTask.execute(executeArgs);

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);


		requestWindowFeature(Window.FEATURE_PROGRESS);

		mApplication = (Knots) getApplication();

		setContentView(R.layout.main);

		mList = (ListView) findViewById(R.id.list);

		mAdapter = new KnotsAdapter(this);   

		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(listener);

		// Handle incoming intents as possible searches or links
		onNewIntent(getIntent());
	}



	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		addProfiles(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy() {

		cancelCurrentLoad();
		super.onDestroy();
	}

	/**
	 * Intercept the back-key to try walking backwards along our word history
	 * stack. If we don't have any remaining history, the key behaves normally
	 * and closes this activity.
	 */
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {

		// Otherwise fall through to parent
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Because we're singleTop, we handle our own new intents. These usually
	 * come from the {@link SearchManager} when a search is requested, or from
	 * internal links the user clicks on.
	 */
	@Override
	public void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {

			final String newPath = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_PATH);
			if (newPath != null) {
				browseByPath(newPath);
			}
		} else if (Knots.KNOTS_INTENT_ACTION_CATEGORY.equals(action)) {
			mCurrentCategory = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_CATEGORY);
			browseCategory(mCurrentCategory, "", "");
		} else if (Knots.KNOTS_INTENT_ACTION_VIRTUAL.equals(action)) {
			// if we are viewing then theres a new path
			final String newPath = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_PATH);
			if (newPath != null) {
				browseVirtual(newPath);
			}
		} else if (Knots.KNOTS_INTENT_ACTION_TAG.equals(action)) {
			// if we are viewing then theres a new path
			mCurrentCategory = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_CATEGORY);
			final String newPath = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_PATH);
			browseCategory(mCurrentCategory, newPath, "");
		} else if (Knots.KNOTS_INTENT_ACTION_VALUE.equals(action)) {
			// if we are viewing then theres a new path
			mCurrentCategory = intent
			.getStringExtra(Knots.KNOTS_INTENT_EXTRA_CATEGORY);
			final String newPath = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_TAG);
			final String newValue = intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_PATH);
			browseCategory(mCurrentCategory, newPath, newValue);

		} else if (Intent.ACTION_MAIN.equals(action)) {
			// going to root path
			browseByPath("");
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getGroupId()) {
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
			case R.id.update:
				onUpdateCollection();
				break;
			}

		}
		return true;
	}

	private void onUpdateCollection() {
		UpdateCollectionTask updater = new UpdateCollectionTask(this);
		updater.execute(mApplication);		
	}

	private void onShowLoginDialog() {
		final LayoutInflater inflater = LayoutInflater.from(this);
		final View addView = inflater.inflate(R.layout.serveroptions, null);
		final DialogWrapper wrapper = new DialogWrapper(addView, mApplication.getHost(), mApplication.getUserName(), mApplication.getUserPassword(), mApplication.getListLimit());

		new AlertDialog.Builder(this)
		.setTitle(R.string.loginOptions)
		.setView(addView)
		.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog,
					final int whichButton) {
				mApplication.setHost(wrapper.getServer());
				mApplication.setUser(wrapper.getUser());
				mApplication.setMediaPassword(wrapper.getPassword());
				mApplication.setListLimit(wrapper.getLimit());
			}
		})
		.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog,
					final int whichButton) {
				// ignore, just dismiss
			}
		}).show();
	}

	private void setCurrentProfile(final MenuItem item) {
		mApplication.setCurrentProfile(item.getItemId());
	}
}
