/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package knots2.browser;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

/**
 * Displays a video file. The KnotsVideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class KnotsVideoView extends SurfaceView implements MediaPlayerControl {
	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PAUSED = 4;

	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_RESUME = 7;
	private static final int STATE_SUSPEND = 6;
	private final MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
			mCurrentBufferPercentage = percent;
		}
	};
	// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private final MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(final MediaPlayer mp) {
			mCurrentState = KnotsVideoView.STATE_PLAYBACK_COMPLETED;
			mTargetState = KnotsVideoView.STATE_PLAYBACK_COMPLETED;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
		}
	};

	private int mCurrentBufferPercentage;
	// mCurrentState is a KnotsVideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the KnotsVideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = KnotsVideoView.STATE_IDLE;
	private int mDuration;
	private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(final MediaPlayer mp, final int framework_err,
				final int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = KnotsVideoView.STATE_ERROR;
			mTargetState = KnotsVideoView.STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}

			return true;
		}
	};
	private MediaController mMediaController;
	private MediaPlayer mMediaPlayer = null;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(final MediaPlayer mp) {
			mCurrentState = KnotsVideoView.STATE_PREPARED;

			mCanPause = mCanSeekBack = mCanSeekForward = true;

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			final int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared
															// may be
			// changed after seekTo()
			// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if ((mVideoWidth != 0) && (mVideoHeight != 0)) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if ((mSurfaceWidth == mVideoWidth)
						&& (mSurfaceHeight == mVideoHeight)) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == KnotsVideoView.STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying()
							&& ((seekToPosition != 0) || (getCurrentPosition() > 0))) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == KnotsVideoView.STATE_PLAYING) {
					start();
				}
			}
		}
	};
	private int mSeekWhenPrepared; // recording the seek position while
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(final SurfaceHolder holder,
				final int format, final int w, final int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			final boolean isValidState = (mTargetState == KnotsVideoView.STATE_PLAYING);
			final boolean hasValidSize = ((mVideoWidth == w) && (mVideoHeight == h));
			if ((mMediaPlayer != null) && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
				if (mMediaController != null) {
					mMediaController.show();
				}
			}
		}

		public void surfaceCreated(final SurfaceHolder holder) {
			mSurfaceHolder = holder;
			// resume() was called before surfaceCreated()
			if ((mMediaPlayer != null)
					&& (mCurrentState == KnotsVideoView.STATE_SUSPEND)
					&& (mTargetState == KnotsVideoView.STATE_RESUME)) {
				mMediaPlayer.setDisplay(mSurfaceHolder);
			} else {
				openVideo();
			}
		}

		public void surfaceDestroyed(final SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mCurrentState != KnotsVideoView.STATE_SUSPEND) {
				release(true);
			}
		}
	};
	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(final MediaPlayer mp, final int width,
				final int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if ((mVideoWidth != 0) && (mVideoHeight != 0)) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
		}
	};
	private int mSurfaceHeight;
	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private int mSurfaceWidth;

	private int mTargetState = KnotsVideoView.STATE_IDLE;

	// settable by the client
	private Uri mUri;

	private int mVideoHeight;

	private int mVideoWidth;

	private final String TAG = "KnotsVideoView";

	public KnotsVideoView(final Context context) {
		super(context);
		initKnotsVideoView();
	}

	public KnotsVideoView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
		initKnotsVideoView();
	}

	public KnotsVideoView(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		initKnotsVideoView();
	}

	private void attachMediaController() {
		if ((mMediaPlayer != null) && (mMediaController != null)) {
			final View anchorView = getParent() instanceof View ? (View) getParent()
					: this;
			mMediaController.setAnchorView(anchorView);
		}
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public Uri getVideoURI() {

		return mUri;
	}

	private void initKnotsVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = KnotsVideoView.STATE_IDLE;
		mTargetState = KnotsVideoView.STATE_IDLE;
	}

	private boolean isInPlaybackState() {
		return ((mMediaPlayer != null)
				&& (mCurrentState != KnotsVideoView.STATE_ERROR)
				&& (mCurrentState != KnotsVideoView.STATE_IDLE) && (mCurrentState != KnotsVideoView.STATE_PREPARING));
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		final boolean isKeyCodeSupported = (keyCode != KeyEvent.KEYCODE_BACK)
				&& (keyCode != KeyEvent.KEYCODE_VOLUME_UP)
				&& (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN)
				&& (keyCode != KeyEvent.KEYCODE_MENU)
				&& (keyCode != KeyEvent.KEYCODE_CALL)
				&& (keyCode != KeyEvent.KEYCODE_ENDCALL);
		if (isInPlaybackState() && isKeyCodeSupported
				&& (mMediaController != null)) {
			if ((keyCode == KeyEvent.KEYCODE_HEADSETHOOK)
					|| (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if ((keyCode == KeyEvent.KEYCODE_MEDIA_STOP)
					&& mMediaPlayer.isPlaying()) {
				pause();
				mMediaController.show();
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		setMeasuredDimension(800, 480);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (isInPlaybackState() && (mMediaController != null)) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent ev) {
		if (isInPlaybackState() && (mMediaController != null)) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	private void openVideo() {
		if ((mUri == null) || (mSurfaceHolder == null)) {
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		final Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		getContext().sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(getContext(), mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = KnotsVideoView.STATE_PREPARING;
			attachMediaController();
		} catch (final IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = KnotsVideoView.STATE_ERROR;
			mTargetState = KnotsVideoView.STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (final IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = KnotsVideoView.STATE_ERROR;
			mTargetState = KnotsVideoView.STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = KnotsVideoView.STATE_PAUSED;
			}
		}
		mTargetState = KnotsVideoView.STATE_PAUSED;
	}

	/*
	 * release the media player in any state
	 */
	private void release(final boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = KnotsVideoView.STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = KnotsVideoView.STATE_IDLE;
			}
		}
	}

	public int resolveAdjustedSize(final int desiredSize, final int measureSpec) {
		int result = desiredSize;
		final int specMode = MeasureSpec.getMode(measureSpec);
		final int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	public void seekTo(final int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public void setMediaController(final MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(final OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * KnotsVideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(final OnErrorListener l) {
	}

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(final MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	public void setVideoPath(final String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(final Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	public void setVideoURI(final Uri uri, final Map<String, String> headers) {
		mUri = uri;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void start() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = KnotsVideoView.STATE_PLAYING;
		}
		mTargetState = KnotsVideoView.STATE_PLAYING;
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = KnotsVideoView.STATE_IDLE;
			mTargetState = KnotsVideoView.STATE_IDLE;
		}
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}
}
