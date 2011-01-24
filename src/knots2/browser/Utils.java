package knots2.browser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

public class Utils {
	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	public static String readUrlResponse( URL query ) {
		InputStream in = null;
		String queryResult = "";
		try {

			HttpURLConnection httpConn = (HttpURLConnection) query.openConnection();
			httpConn.setAllowUserInteraction(false);
			httpConn.connect();
			in = httpConn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(in);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int read = 0;
			int bufSize = 512;
			byte[] buffer = new byte[bufSize];
			while(true){
				read = bis.read(buffer);
				if(read==-1){
					break;
				}
				baf.append(buffer, 0, read);
			}
			queryResult = new String(baf.toByteArray());

		} catch (MalformedURLException e) {
			// DEBUG
			Log.e("DEBUG: ", e.toString());
		} catch (IOException e) {
			// DEBUG
			Log.e("DEBUG: ", e.toString());
		}
		return queryResult;
	}
}