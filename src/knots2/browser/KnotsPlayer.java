package knots2.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;

public class KnotsPlayer extends Activity implements
		OnCompletionListener, OnPreparedListener, OnErrorListener,
		OnBufferingUpdateListener, MediaController.MediaPlayerControl {

		
	
	
	

	private ProgressDialog mProgressDialog;
	Knots mApplication;
	private int mBufferPercent;
	private final Handler mHandler = new Handler();
	private MediaController mPlayerController;
	private PlayerProperties mPlayerProperties;
	private KnotsVideoView mVideoView;

	private void updateProperties() {

		final String path = mApplication.getHost()
		+ "/external/player_properties?player_id="
		+ mApplication.getPlayerId();

		/* Create a URL we want to load some xml-data from. */
		URL url;

		try {


			url = new URL(path);

			/* Get a SAXParser from the SAXPArserFactory. */
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			final XMLReader xr = sp.getXMLReader();

			/* Create a new ContentHandler and apply it to the XML-Reader */			
			xr.setContentHandler(mPlayerProperties);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));

		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

		
	private final Runnable mUpdatePropertiesTask = new Runnable() {
		public void run() {
			updateProperties();
			
			mHandler.postDelayed(mUpdatePropertiesTask, 5000);
		}
	};

	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return true;
	}

	public int getBufferPercentage() {

		return mBufferPercent;
	}

	public int getCurrentPosition() {
		float position = mPlayerProperties.get_position() * getDuration();
		
		return (int) position;

	}

	public int getDuration() {
		// TODO Auto-generated method stub
		return mPlayerProperties.get_duration();
	}

	public void hideDialog() {
		mProgressDialog.dismiss();
		mProgressDialog = null;
	}

	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return true;
	}

	public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
		mBufferPercent = percent;
		if (mProgressDialog != null) {
			mProgressDialog.setProgress(mBufferPercent);
		}
	}

	public void onCompletion(final MediaPlayer mp) {
		finish();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mApplication = (Knots) getApplication();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		mVideoView = new KnotsVideoView(this);
		mVideoView.setLayoutParams(params);
		mVideoView.setKeepScreenOn(true);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnErrorListener(this);
		

		mPlayerController = new MediaController(this);
		mPlayerController.setMediaPlayer(this);
		mVideoView.setMediaController(mPlayerController);

		mPlayerProperties = new PlayerProperties();

		this.setContentView(mVideoView);

	
		
		// Handle incoming intents as possible searches or links
		onNewIntent(getIntent());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public boolean onError(final MediaPlayer mp, final int what, final int extra) {
		finish();
		return false;
	}

	/**
	 * Because we're singleTop, we handle our own new intents. These usually
	 * come from the {@link SearchManager} when a search is requested, or from
	 * internal links the user clicks on.
	 */
	@Override
	public void onNewIntent(final Intent intent) {

		final String action = intent.getAction();

		if (Knots.KNOTS_INTENT_ACTION_PLAY.equals(action)) {

			startPlayer(intent.getStringExtra(Knots.KNOTS_INTENT_EXTRA_MEDIA));

		}

	}

	public void onPrepared(final MediaPlayer arg0) {
		hideDialog();
	}

	@Override
	public void onStop() {
		stopPlayer();
		super.onStop();
	}

	public void pause() {
		// TODO Auto-generated method stub

	}

	public void seekTo(final int pos) {

		Log.d("Player", "Seeking to " + String.valueOf(pos));

		try {
			final float percentage = (float) pos / (float) getDuration();

			final HttpGet method = new HttpGet(mApplication.getHost()
					+ "/external/seek?player_id=" + mApplication.getPlayerId()
					+ "&position=" + Float.toString(percentage));
			final HttpClient client = new DefaultHttpClient();
			final String txtResult = new String();
			final HttpResponse response = client.execute(method);
			HttpHelper.request(response);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void showDialog(final String reason) {

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(KnotsPlayer.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER );			
			mProgressDialog.setMessage("Loading ... ");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}
	}

	public void start() {

	}

	void startPlayer(final String mediaId) {

		try {
			final HttpGet method = new HttpGet(mApplication.getHost()
					+ "/external/play?profile="
					+ Integer.toString(mApplication.getCurrentProfile())
					+ "&id=" + mediaId);
			final HttpClient client = new DefaultHttpClient();
			String txtResult = new String();

			final HttpResponse response = client.execute(method);
			txtResult = HttpHelper.request(response);
			mApplication.setPlayerId(txtResult.split(":")[0]);
			mApplication.setPassword(txtResult.split(":")[1]);
			
			updateProperties();
			
			startPropertyUpdates();
			mVideoView.setVideoURI(mPlayerProperties.get_streamUrl());
			mVideoView.start();
			showDialog("Loading");
			
			// if the stream url changed the update the media player




		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void startPropertyUpdates() {
		mHandler.removeCallbacks(mUpdatePropertiesTask);
		mHandler.postDelayed(mUpdatePropertiesTask, 5000);
	}

	public void stopPlayer() {
		/* Create a URL we want to load some xml-data from. */

		if (mApplication.getPlayerId() != null) {
			stopPropertyUpdates();
			mVideoView.stopPlayback(); 
			/* Create a URL we want to load some xml-data from. */
			final HttpClient client = new DefaultHttpClient();
			final HttpGet method = new HttpGet(
					mApplication.getString(R.string.server) + "/root/stop?id="
							+ mApplication.getPlayerId());
			try {
				final HttpResponse response = client.execute(method);
				HttpHelper.request(response);
			} catch (final Exception ex) {
			}
		}
	}

	private void stopPropertyUpdates() {
		mHandler.removeCallbacks(mUpdatePropertiesTask);
	}
}
