package knots2.browser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import knots2.browser.KnotsListView.KnotsListHandlerObserver;
import knots2.browser.KnotsListView.KnotsListHandlerUpdate;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.widget.Toast;


public class KnotsListDownload extends
AsyncTask<Void, KnotsListHandlerUpdate, Void> implements
KnotsListHandlerObserver {

	final class DownloadTaskArgs {
		private final String mPath;
		final KnotsListView mView;
		

		public DownloadTaskArgs(final String path, final KnotsListView view) {
			mView = view;
			mPath = path;
		}

		public synchronized String getPath() {
			return mPath;
		}

		public String getLimit() {
			// TODO Auto-generated method stub
			return "&limit=" + Integer.toString(((Knots)mView.getApplication()).getListLimit());
		}

	}

	DownloadTaskArgs mArgs;
	final private int itemsPerPage = 32;		
	private int mCurrentItem = 0;
	private int mCurrentPage = 0;
	private int mTotalPages = 0;
	private int mTotalItems = 0;
	private ProgressDialog progressDialog;
	private int mDialogStyle;
	static final int SHOW_DIALOG_THRESHOLD = 32;
	
	private Exception mCaughtExpection = null;
	
	@Override
	protected Void doInBackground(final Void... params) {
		// stop tracing    

		loadDirectory(mArgs);

		return null;
	}

	public Knots getApplication() {
		return (Knots) mArgs.mView.getApplication();
	}

	private void loadDirectory(final DownloadTaskArgs args) {
		HttpURLConnection urlConnection = null;
		try {

			/* Create a URL we want to load some xml-data from. */
			String pages = "";

			boolean morePages = false;
			//Debug.startMethodTracing("fetch");
			do {
				final URL url = new URL(args.getPath() + pages + args.getLimit());

				urlConnection = (HttpURLConnection) url.openConnection();			
				urlConnection.connect();

				/* Get a SAXParser from the SAXPArserFactory. */
				final SAXParserFactory spf = SAXParserFactory.newInstance();
				final SAXParser sp = spf.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				final XMLReader xr = sp.getXMLReader();
				/*
				 * Create a new ContentHandler and apply it to the
				 * XML-Reader
				 */
				final KnotsListHandler folderHandler = new KnotsListHandler(
						this);

				xr.setContentHandler(folderHandler);

				/* Parse the xml-data from our URL. */
				final InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());

				xr.parse(new InputSource(in));
				in.close();
				urlConnection.disconnect();

				/*
				 * Parsing has finished. any more pages ?
				 */
				morePages = mCurrentPage < mTotalPages;

				if (morePages) {
					pages = "&page=" + Integer.toString(mCurrentPage + 1);
				}

			} while (morePages && !isCancelled());
			//Debug.stopMethodTracing();


		}

		catch (final Exception e) {
			// we are an execution thread, so hold the exception,
			mCaughtExpection = e;
			
		} finally {
			urlConnection.disconnect();
		}

	}

	public void onNewItem(final KnotsListHandlerUpdate newItem) {
		publishProgress(newItem);
	}

	public void onPageUpdate(final int currentPage, final int totalPages, int totalItems) {

		mTotalPages = totalPages;
		mCurrentPage = currentPage;
		mTotalItems += totalItems;
	}

	@Override
	protected void onPostExecute(final Void result) {
		progressDialog.dismiss();
		mArgs.mView.setProgressBarVisibility(false);
		
		// did we catch an exception.
		if( mCaughtExpection != null ) {
			Toast errorPrompt = Toast.makeText(mArgs.mView, R.string.directory_error , 10);
			errorPrompt.show();
		}
	}

	@Override
	protected void onPreExecute() {

		mCurrentItem = 0;
		mCurrentPage = 0;
		mTotalPages = 0;
		mTotalItems = 0;
		mDialogStyle = ProgressDialog.STYLE_SPINNER;


		progressDialog = new ProgressDialog(mArgs.mView);
		progressDialog.setProgressStyle(mDialogStyle);
		progressDialog.setMessage("Loading ... ");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);		
		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(this));					
		mArgs.mView.mAdapter.clear();
		mArgs.mView.setProgressBarVisibility(true);


	}

	private class CancelTaskOnCancelListener implements OnCancelListener {
		private AsyncTask<?, ?, ?> task;
		public CancelTaskOnCancelListener(AsyncTask<?, ?, ?> task) {
			this.task = task;
		}
		public void onCancel(DialogInterface dialog) {
			if (task!=null) {
				task.cancel(true);
				mArgs.mView.setProgressBarVisibility(false);

			}
		}
	}

	@Override
	protected void onProgressUpdate(
			final KnotsListHandlerUpdate... progress) {

		mCurrentItem++;
		progressDialog.setMax(mTotalItems);			
		if( mTotalItems > SHOW_DIALOG_THRESHOLD && !isCancelled()) {	
			if( mDialogStyle == ProgressDialog.STYLE_SPINNER ){
				progressDialog.setIndeterminate(false);
				mDialogStyle = ProgressDialog.STYLE_HORIZONTAL;					

				progressDialog.setProgressStyle(mDialogStyle);
				progressDialog.show();
			}

			progressDialog.setProgress(mCurrentItem);				
		}

		int titleProgress = (int) (10000.0 * ( 1.0 -  (( (float) mTotalItems - (float)mCurrentItem ) / (float)mTotalItems)));
		mArgs.mView.setProgress(titleProgress);
		mArgs.mView.mAdapter.addItem(progress[0].getItem());
	}

	public void setArgs(final DownloadTaskArgs args) {
		mArgs = args;
	}
}