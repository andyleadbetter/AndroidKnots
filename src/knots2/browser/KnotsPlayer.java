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
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.VideoView;

public class KnotsPlayer extends Activity implements OnCompletionListener,
OnPreparedListener, OnErrorListener {

	Knots mApplication;

	private PlayerProperties playerProperties;
	private VideoView mVideoView;
	private MediaController mc;
	private ProgressDialog dialog;





	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStop() {
		stopPlayer();
		super.onStop();
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

			startPlayer(intent.getStringExtra("knots2.browser.media"));
		
			if (this.mVideoView != null) {
				mVideoView.setVideoURI(Uri.parse("rtsp://192.168.0.28:8080/stream.sdp"));
				mVideoView.start();
				showDialog("Loading....");
			}
		}

	}


	public void getPlayerProperties()
	{
		String path = mApplication.getApplicationContext().getString( R.string.server ) + "/external/player_properties?player_id=" + mApplication.getPlayerId();


		/* Create a URL we want to load some xml-data from. */
		URL url;

		try {
			url = new URL(path);

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();

			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			playerProperties = new PlayerProperties();
			xr.setContentHandler(playerProperties);

			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */


		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	void startPlayer(final String mediaId){

		try{
			HttpGet method = new HttpGet(mApplication.getString( R.string.server ) + "/external/play?profile=" + Integer.toString(9) +"&id=" + mediaId );
			HttpClient client = new DefaultHttpClient();
			String txtResult = new String();

			HttpResponse response = client.execute(method);
			txtResult = HttpHelper.request(response);     
			mApplication.setPlayerId(txtResult.split(":")[0]);
			mApplication.setPassword(txtResult.split(":")[1]);

		}
		catch (Exception e) {
			/* Display any Error to the GUI. */			
		}
	}


	public void stopPlayer( )	{
		/* Create a URL we want to load some xml-data from. */
		
//		if( mApplication.getPlayerId() != null ) {
//			/* Create a URL we want to load some xml-data from. */		
//			HttpClient client = new DefaultHttpClient();
//			HttpGet method = new HttpGet(mApplication.getString( R.string.server ) + "/root/stop?id=" + mApplication.getPlayerId() );
//			try{
//				HttpResponse response = client.execute(method);
//				HttpHelper.request(response);				
//			}catch(Exception ex){				
//			}
//		}
	}




	public class MyVideoView extends VideoView {
		public MyVideoView(final Context context) {
			super(context);
		}

		@Override
		protected void onMeasure(final int widthMeasureSpec,
				final int heightMeasureSpec) {
			this.setMeasuredDimension(800, 480);
		}
	}

	



	public void onCompletion(final MediaPlayer mp) {
		this.finish();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.mApplication = (Knots) this.getApplication();

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
		this.mVideoView = new MyVideoView(this);
		this.mVideoView.setOnCompletionListener(this);
		this.mVideoView.setOnPreparedListener(this);
		this.mVideoView.setOnErrorListener(this);

		final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,  LayoutParams.FILL_PARENT);
		this.mVideoView.setLayoutParams(params);
		this.mVideoView.setMediaController(new MediaController(this));
		this.setContentView(this.mVideoView);

		// Handle incoming intents as possible searches or links
		this.onNewIntent(this.getIntent());

	}


	public boolean onError(final MediaPlayer mp, final int what, final int extra) {
		
		return false;
	}


	public void showDialog( String reason ) {
	
//		if( dialog == null )
//			dialog = ProgressDialog.show(KnotsPlayer.this, "", reason, true);
	}

	public void hideDialog() {
//		dialog.dismiss();
//		dialog=null;		
	}

	public void onPrepared(final MediaPlayer arg0) {
		hideDialog();
		arg0.start();
	}
}