package knots2.browser;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayerActivity extends Activity implements OnCompletionListener,
		OnPreparedListener, OnErrorListener, OnBufferingUpdateListener {

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

	Knots mApplication;

	MyVideoView mVideoView;
	MediaController mc;

	private ProgressDialog dialog;

	public void onCompletion(final MediaPlayer mp) {
		this.finish();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.mApplication = (Knots) this.getApplication();

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		this.setContentView(R.layout.videoview);

		this.mVideoView = new MyVideoView(this);
		this.mVideoView.setOnCompletionListener(this);
		this.mVideoView.setOnPreparedListener(this);
		this.mVideoView.setOnErrorListener(this);

		final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		this.mVideoView.setLayoutParams(params);
		this.mVideoView.setMediaController(new MediaController(this));
		this.setContentView(this.mVideoView);

		// Handle incoming intents as possible searches or links
		this.onNewIntent(this.getIntent());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public boolean onError(final MediaPlayer mp, final int what, final int extra) {
		this.finish();
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

		if (Intent.ACTION_VIEW.equals(action)) {

			final Uri mediaFile = intent.getData();

			if (this.mVideoView != null) {
				this.mc = new MediaController(this);
				this.mVideoView.setMediaController(this.mc);
				this.mVideoView.setVideoURI(mediaFile);
				this.mVideoView.requestFocus();
				showDialog("Loading....");
			}
		}

	}

	public void showDialog( String reason ) {
		if( dialog == null )
			dialog = ProgressDialog.show(PlayerActivity.this, "", reason, true);
	}
	
	public void hideDialog() {
		dialog.dismiss();
		dialog=null;		
	}
	
	public void onPrepared(final MediaPlayer arg0) {
		hideDialog();
			
		arg0.setOnBufferingUpdateListener(this);
		arg0.start();
	}

	public void onBufferingUpdate(MediaPlayer mp, int percent) {
	}	
}
