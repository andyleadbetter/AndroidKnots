package knots2.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

public class KnotsPlayer extends Activity {
	
	Knots application;
	
	private PlayerProperties playerProperties;
	private VideoView videoView;
	private MediaController mc;
	
	
	
	@Override
	public void onDestroy() {						
	}
	
	@Override
	public void onStop() {		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Intent args = getIntent();
		
		application = (Knots) getApplication();
		application.setMedia(args.getStringExtra("media"));
		
		setContentView(R.layout.videoview);
		
		mc = (MediaController) findViewById(R.id.mediacontrollerid);
		
		videoView = (VideoView) findViewById(R.id.videoviewid);
		
		startPlayer();
		
		
	}

	public void getPlayerProperties()
	{
		String path = application.getApplicationContext().getString( R.string.server ) + "/external/player_properties?player_id=" + application.getPlayerId();


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
	
	void startPlayer(){

		try{
			HttpGet method = new HttpGet(application.getApplicationContext().getString( R.string.server ) + "/external/play?profile=" + Integer.toString(4) +"&id=" + application.getMedia());
			HttpClient client = new DefaultHttpClient();
			String txtResult = new String();
			
			
			
		
			HttpResponse response = client.execute(method);
			txtResult = HttpHelper.request(response);
			txtResult = txtResult.substring(0, txtResult.length() - 12 );
			
			application.setPlayerId(txtResult.split(":")[0]);
			application.setPassword(txtResult.split(":")[1]);
			
			getPlayerProperties();
			
			Uri mediaStream = Uri.parse("rtsp://192.168.0.28:8080/stream.sdp");
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(mediaStream);
			startActivity(intent);


			
		}
		catch (Exception e) {
			/* Display any Error to the GUI. */			
		}
	}
	

	public void stopPlayer( )	{
		/* Create a URL we want to load some xml-data from. */
		if( application.getPlayerId() != null ) {
			/* Create a URL we want to load some xml-data from. */		
			HttpClient client = new DefaultHttpClient();
			HttpGet method = new HttpGet(application.getApplicationContext().getString( R.string.server ) + "/root/stop?id=" + application.getPlayerId() );
			try{
				HttpResponse response = client.execute(method);
				HttpHelper.request(response);
				finish();
			}catch(Exception ex){				
			}
		}
	}
}
