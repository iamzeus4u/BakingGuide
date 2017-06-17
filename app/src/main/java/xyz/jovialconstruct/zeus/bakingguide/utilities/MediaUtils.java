package xyz.jovialconstruct.zeus.bakingguide.utilities;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import xyz.jovialconstruct.zeus.bakingguide.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by zeus on 13/06/2017.
 */

public class MediaUtils implements ExoPlayer.EventListener {
    private static MediaSessionCompat mMediaSession;
    private static String TAG;
    private PlaybackStateCompat.Builder mStateBuilder;
    private SimpleExoPlayer mExoPlayer;
    private Context context;
    private android.support.v4.media.session.MediaSessionCompat.Callback mCallBack;
    private String mPlayingTitle;
    private String mRecipeName;

    public MediaUtils(Context context, MediaSessionCompat.Callback callback) {
        this.context = context;
        this.mCallBack = callback;
        TAG = context.getClass().getSimpleName();
    }

    public void initializeMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(context, Application.class.getSimpleName());
        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(mCallBack);
        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    private void showNotification() {
        PlaybackStateCompat state = mStateBuilder.build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        int icon;
        String play_pause;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = context.getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = context.getString(R.string.play);
        }
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));
        NotificationCompat.Action restartAction = new android.support.v4.app.NotificationCompat
                .Action(R.drawable.exo_controls_previous, context.getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        NotificationCompat.Action nextAction = new android.support.v4.app.NotificationCompat
                .Action(R.drawable.exo_controls_next, context.getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, 0, new Intent(context, context.getClass()), 0);
        builder.setContentTitle(mRecipeName)
                .setContentText(mPlayingTitle)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.exo_controls_play)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .addAction(nextAction)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    // ExoPlayer Event Listeners
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    /**
     * Method that is called when the ExoPlayer state changes. Used to update the MediaSession
     * PlayBackState to keep in sync, and post the media notification.
     *
     * @param playWhenReady true if ExoPlayer is playing, false if it's paused.
     * @param playbackState int describing the state of ExoPlayer. Can be STATE_READY, STATE_IDLE,
     *                      STATE_BUFFERING, or STATE_ENDED.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
            Log.d(TAG, "onPlayerStateChanged: PLAYING");
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
            Log.d(TAG, "onPlayerStateChanged: PAUSED");
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }

    @Override
    public void onPositionDiscontinuity() {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    public SimpleExoPlayer getmExoPlayer() {
        return mExoPlayer;
    }

    public void setExtras(String mPlayingTitle, SimpleExoPlayer mExoPlayer) {
        this.mPlayingTitle = mPlayingTitle;
        this.mExoPlayer = mExoPlayer;
    }

    public void setRecipeName(String recipeName) {
        this.mRecipeName = recipeName;
    }

    public void release() {
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
    }

    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

}
