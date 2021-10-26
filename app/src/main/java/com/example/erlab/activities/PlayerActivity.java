package com.example.erlab.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.CaptioningManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.erlab.R;
import com.example.erlab.player.DashRendererBuilder;
import com.example.erlab.player.ExoPlayerWrapper;
import com.example.erlab.player.ExtractorRendererBuilder;
import com.example.erlab.player.HlsRendererBuilder;
import com.example.erlab.player.MediaController;
import com.example.erlab.player.SmoothStreamingRendererBuilder;
import com.example.erlab.player.SmoothStreamingTestMediaDrmCallback;
import com.example.erlab.player.WideVineTestMediaDrmCallback;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Locale;

/**
 * @Class PlayerActivity The base Video Player activity
 * @Author Malik Dawar
 * @Date 09 OCT 2020
 */

public class PlayerActivity extends Activity implements SurfaceHolder.Callback,
        ExoPlayerWrapper.Listener, ExoPlayerWrapper.CaptionListener, ExoPlayerWrapper.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener {

    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String VIDEO_URL_EXTRA = "video_url_extra";
    public static final String TITLE_TEXT_EXTRA = "title_text_extra";
    public static final String PLAY_BUTTON_EXTRA = "play_button_extra";

    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;

    private static final String EXT_DASH = ".mpd";
    private static final String EXT_SS = ".ism";
    private static final String EXT_HLS = ".m3u8";

    private static final String TAG = "PlayerActivity";
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private MediaController mediaController;
    private View shutterView;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private SubtitleLayout subtitleLayout;

    private ExoPlayerWrapper player;
    private boolean playerNeedsPrepare;


    private long playerPosition;

    private Uri contentUri;
    private int contentType;
    private String contentId;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private ViewGroup controllerView;

    private ViewGroup root;
    private TextView tvChannelTitle;
    private ProgressBar progressBar;
    private ImageView playButton;
    AudioManager audioManager;

    public static Intent getVideoPlayerIntent(Context context, @NonNull final String videoUrl, @NonNull final String title,
                                              @DrawableRes final int playButtonRes) {
        return new Intent(context, PlayerActivity.class).putExtra(VIDEO_URL_EXTRA, videoUrl)
                .putExtra(TITLE_TEXT_EXTRA, title).putExtra(PLAY_BUTTON_EXTRA, playButtonRes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player_activity);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        root = (ViewGroup) findViewById(R.id.root);
        root.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControlsVisibility();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return true;
        });

        root.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                    || keyCode == KeyEvent.KEYCODE_MENU) {
                return false;
            }
            return mediaController.dispatchKeyEvent(event);
        });

        shutterView = findViewById(R.id.shutter);
        tvChannelTitle = findViewById(R.id.tvChannelTitle);
        videoFrame = findViewById(R.id.video_frame);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        subtitleLayout = findViewById(R.id.subtitles);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);

        controllerView = findViewById(R.id.controller_view);
        controllerView.addView(mediaController);
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
        audioCapabilitiesReceiver.register();

        progressBar = findViewById(R.id.progress_bar);

        playButton = findViewById(R.id.play_button_icon);
        final int playButtonIconDrawableId = getIntent().getIntExtra(PLAY_BUTTON_EXTRA, 0);

        if (playButtonIconDrawableId != 0) {
            playButton.setImageDrawable(ContextCompat.getDrawable(this, playButtonIconDrawableId));
            playButton.setOnClickListener(v -> preparePlayer(true));
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        playerPosition = 0;
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        final String videoUrl = intent.getStringExtra(VIDEO_URL_EXTRA);
        contentUri = Uri.parse(videoUrl);
        contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, inferContentType(contentUri, videoUrl));
        tvChannelTitle.setText(intent.getStringExtra(TITLE_TEXT_EXTRA));

        configureSubtitleView();
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
        shutterView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }

    private ExoPlayerWrapper.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        switch (contentType) {
            case TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case TYPE_DASH:
                return new DashRendererBuilder(this, userAgent, contentUri.toString(),
                        new WideVineTestMediaDrmCallback(contentId));
            case TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri.toString());
            case TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer(boolean playWhenReady) {

        if (player == null) {
            player = new ExoPlayerWrapper();
            player.setRendererBuilder(getRendererBuilder());
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
            rewind(false);
        }
        final boolean showProgress = playbackState == ExoPlayer.STATE_BUFFERING || playbackState == ExoPlayer.STATE_PREPARING;
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);

        final boolean showPlayButton = !showProgress && !player.isPlaying();
        animatePlayButton(showPlayButton);
    }

    private void rewind(boolean playWhenReady) {
        if (player != null) {
            playerPosition = 0L;
            player.seekTo(playerPosition);
            preparePlayer(playWhenReady);
        }
    }

    private void animatePlayButton(boolean visible) {
        if (visible) {
            playButton.animate()
                    .alpha(1.f)
                    .setDuration(ANIMATION_DURATION_FAST)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            playButton.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        } else {
            playButton.animate()
                    .alpha(0.f)
                    .setDuration(ANIMATION_DURATION_FAST)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            playButton.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
        }
        Toast.makeText(getApplicationContext(), "Can't Play!", Toast.LENGTH_LONG).show();
        playerNeedsPrepare = true;
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                   float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        videoFrame.setAspectRatio(height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    private void configurePopupWithTracks(PopupMenu popup, final OnMenuItemClickListener customActionClickListener, final int trackType) {
        if (player == null) {
            return;
        }
        int trackCount = player.getTrackCount(trackType);
        if (trackCount == 0) {
            return;
        }
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return (customActionClickListener != null
                        && customActionClickListener.onMenuItemClick(item))
                        || onTrackItemClick(item, trackType);
            }
        });
        Menu menu = popup.getMenu();
        // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0)
        menu.add(MENU_GROUP_TRACKS, ExoPlayerWrapper.TRACK_DISABLED + ID_OFFSET, Menu.NONE, R.string.off);
        for (int i = 0; i < trackCount; i++) {
            menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
                    buildTrackName(player.getTrackFormat(trackType, i)));
        }
        menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
        menu.findItem(player.getSelectedTrack(trackType) + ID_OFFSET).setChecked(true);
    }

    private static String buildTrackName(MediaFormat format) {
        if (format.adaptive) {
            return "auto";
        }
        String trackName;
        if (MimeTypes.isVideo(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        } else if (MimeTypes.isAudio(format.mimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildAudioPropertyString(format)), buildBitrateString(format)),
                    buildTrackIdString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }

    private static String buildResolutionString(MediaFormat format) {
        return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildAudioPropertyString(MediaFormat format) {
        return format.channelCount == MediaFormat.NO_VALUE || format.sampleRate == MediaFormat.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private static String buildLanguageString(MediaFormat format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    private static String buildBitrateString(MediaFormat format) {
        return format.bitrate == MediaFormat.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private static String buildTrackIdString(MediaFormat format) {
        return format.trackId;
    }

    private boolean onTrackItemClick(MenuItem item, int type) {
        if (player == null || item.getGroupId() != MENU_GROUP_TRACKS) {
            return false;
        }
        player.setSelectedTrack(type, item.getItemId() - ID_OFFSET);
        return true;
    }

    private static final long ANIMATION_DURATION = 400;
    private static final long ANIMATION_DURATION_FAST = 100;
    private boolean mElementsHidden;

    private void toggleControlsVisibility() {
        if (mElementsHidden) {
            showControls();
        } else {
            controllerView.animate().translationY(controllerView.getHeight()).setDuration(ANIMATION_DURATION).start();
        }
    }

    private void showControls() {

        controllerView.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
    }


    @Override
    public void onCues(List<Cue> cues) {
        subtitleLayout.setCues(cues);
    }

    @Override
    public void onId3Metadata(List<Id3Frame> metadata) {
        for (Id3Frame frame : metadata) {
            if (frame instanceof TxxxFrame) {
                TxxxFrame txxxMetadata = (TxxxFrame) frame;
                Log.i(TAG, String.format("ID3 TimedMetadata Txxx: description=%s, value=%s",
                        txxxMetadata.description, txxxMetadata.value));
            } else if (frame instanceof PrivFrame) {
                PrivFrame privMetadata = (PrivFrame) frame;
                Log.i(TAG, String.format("ID3 TimedMetadata Priv: owner=%s",
                        privMetadata.owner));
            } else if (frame instanceof GeobFrame) {
                final GeobFrame geobMetadata = (GeobFrame) frame;
                Log.i(TAG, String.format("ID3 TimedMetadata Geob: mimeType=%s, filename=%s, description=%s",
                        geobMetadata.mimeType, geobMetadata.filename,
                        geobMetadata.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", frame));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    private void configureSubtitleView() {
        CaptionStyleCompat style;
        float fontScale;
        if (Util.SDK_INT >= 19) {
            style = getUserCaptionStyleV19();
            fontScale = getUserCaptionFontScaleV19();
        } else {
            style = CaptionStyleCompat.DEFAULT;
            fontScale = 1.0f;
        }
        subtitleLayout.setStyle(style);
        subtitleLayout.setFractionalTextSize(SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION * fontScale);
    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager.getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    private static int inferContentType(Uri uri, String fileExtension) {
        String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
                : uri.getLastPathSegment();
        if (lastPathSegment == null) {
            return TYPE_OTHER;
        } else if (lastPathSegment.endsWith(EXT_DASH)) {
            return TYPE_DASH;
        } else if (lastPathSegment.endsWith(EXT_SS)) {
            return TYPE_SS;
        } else if (lastPathSegment.endsWith(EXT_HLS)) {
            return TYPE_HLS;
        } else {
            return TYPE_OTHER;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}