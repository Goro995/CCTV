package com.trycorecoach.corecoachtv;

import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.VideoView;

import com.trycorecoach.corecoachtv.lists.Exercise;
import com.trycorecoach.corecoachtv.manager.DataManager;
import com.trycorecoach.corecoachtv.manager.VideoControllerView;


/**
 * PlaybackOverlayActivity for video playback that loads PlaybackOverlayFragment
 */
public class PlaybackOverlayActivity extends FragmentActivity implements
        PlaybackOverlayFragment.OnPlayPauseClickedListener {
    private static final String TAG = "PlaybackOverlayActivity";

    public VideoView mVideoView;
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
    private MediaSession mSession;

    private SurfaceView videoSurface;
    private MediaPlayer player;
    private VideoControllerView controller;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback_controls);
        loadViews();
        setupCallbacks();
        mSession = new MediaSession(this, "LeanbackSampleApp");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);

//        playbackOverlayFragment.togglePlayback(true);
//        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
//        playbackOverlayFragment.startProgressAutomation();
    }


    public void startPlaying() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        Log.d(TAG, "mVideoView: " + mVideoView);
        mVideoView.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoView.suspend();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (mPlaybackState != LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(true);
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(false);
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                playbackOverlayFragment.stop();
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                playbackOverlayFragment.fastForwardAction();
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                playbackOverlayFragment.rewindAction();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playbackOverlayFragment.prev();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playbackOverlayFragment.next();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(false);
                } else {
                    playbackOverlayFragment.togglePlayback(true);
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void videoSeeker() {
        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        mVideoView.seekTo(playbackOverlayFragment.getCurrentTime());
    }

    public void videoStop() {
        mVideoView.seekTo(0);
    }

    public PlaybackOverlayFragment getFragment() {
        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        return playbackOverlayFragment;
    }

    /**
     * Implementation of OnPlayPauseClickedListener
     */
    public void onFragmentPlayPause(Exercise exercise, int position, Boolean playPause) {

        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        Log.d(TAG, "mPlaybackState: " + mPlaybackState);
        Log.d(TAG, "playPause: " + playPause);

        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
            setupCallbacks();
            mPlaybackState = LeanbackPlaybackState.IDLE;
        }
        if (playPause && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (position >= 0) {
                mVideoView.seekTo(position);
                mVideoView.start();
                playbackOverlayFragment.startProgressAutomation();
                Log.d(TAG, "PLAYING");
            }
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            mVideoView.pause();
            playbackOverlayFragment.stopProgressAutomation();
            Log.d(TAG, "PAUSED");

        }
        updatePlaybackState(position);
        updateMetadata(exercise);
    }

    private void updatePlaybackState(int position) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, position, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH;

        if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
            actions |= PlaybackState.ACTION_PAUSE;
        }

        return actions;
    }

    private void updateMetadata(final Exercise exercise) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        String title = exercise.getTitle().replace("_", " -");

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
//        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
//                exercise.getDescription());
//        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
//                exercise.getCardImageUrl());

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, exercise.getTitle());

//        Glide.with(this)
//                .load(Uri.parse(exercise.getCardImageUrl()))
//                .asBitmap()
//                .into(new SimpleTarget<Bitmap>(500, 500) {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
//                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
//                        mSession.setMetadata(metadataBuilder.build());
//                    }
//                });
    }

    private void loadViews() {
        mVideoView = (VideoView) findViewById(R.id.videoView);
        Exercise mSelectedExercise = (Exercise) this
                .getIntent().getSerializableExtra(DetailsActivity.EXERCISE);
        String pathToVideo = DataManager.buildURLforContent(mSelectedExercise.getVideoURL());
        mVideoView.setVideoPath(pathToVideo);
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
    }

    private void setupCallbacks() {

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String msg = "";
                if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = getString(R.string.video_error_media_load_timeout);
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = getString(R.string.video_error_server_inaccessible);
                } else {
                    msg = getString(R.string.video_error_unknown_error);
                }
                mVideoView.stopPlayback();
                mPlaybackState = LeanbackPlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {

                    mVideoView.start();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlaybackState = LeanbackPlaybackState.IDLE;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mSession.setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                stopPlayback();
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSession.release();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
    }

    private void stopPlayback() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    /*
     * List of various states that we can be in
     */
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, IDLE
    }

    private class MediaSessionCallback extends MediaSession.Callback {
    }
}
