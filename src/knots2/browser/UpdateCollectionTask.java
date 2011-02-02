package knots2.browser;
import java.net.MalformedURLException;
import java.net.URL;



import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;


public class UpdateCollectionTask extends AsyncTask<Knots, Void, Void> {


	private ProgressDialog progressDialog;
	private KnotsListView knotsListView;

	public UpdateCollectionTask(KnotsListView knotsListView) {
		this.knotsListView = knotsListView;
	}

	@Override
	protected Void doInBackground(Knots... params) {

		URL seekRequest;
		try {
			seekRequest = new URL(params[0].getHost() + "/root/update_database");

			final String txtResult = Utils.readUrlResponse(seekRequest);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(final Void result) {
		progressDialog.dismiss();
		
	}

	@Override
	protected void onPreExecute() {

		progressDialog = new ProgressDialog(knotsListView);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Updating... ");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);		
		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(this));			
		progressDialog.show();
	}

	private class CancelTaskOnCancelListener implements OnCancelListener {
		private AsyncTask<?, ?, ?> task;
		public CancelTaskOnCancelListener(AsyncTask<?, ?, ?> task) {
			this.task = task;
		}
		public void onCancel(DialogInterface dialog) {
			if (task!=null) {
				task.cancel(true);
			}
		}
	}
}
