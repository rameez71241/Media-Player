package com.rameez.mediaplayer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rameez.mediaplayer.R;
import com.rameez.mediaplayer.util.FileUtils;

import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_URI = "extra_media_uri";
    public static final String EXTRA_TITLE = "extra_title";

    private MediaPlayer mediaPlayer;
    private SeekBar playbackSeekBar;
    private SeekBar volumeSeekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private Button playPauseButton;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean userSeeking;
    private int lastPosition;
    private boolean shouldResume;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !userSeeking) {
                int current = mediaPlayer.getCurrentPosition();
                playbackSeekBar.setProgress(current);
                currentTimeText.setText(FileUtils.formatDuration(current));
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        playbackSeekBar = findViewById(R.id.seek_playback);
        volumeSeekBar = findViewById(R.id.seek_volume);
        currentTimeText = findViewById(R.id.text_current_time);
        totalTimeText = findViewById(R.id.text_total_time);
        playPauseButton = findViewById(R.id.button_play_pause);
        Button stopButton = findViewById(R.id.button_stop);
        ImageView albumArt = findViewById(R.id.image_album_art);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }

        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getInt("state_position", 0);
            shouldResume = savedInstanceState.getBoolean("state_playing", false);
        }

        String uriString = getIntent().getStringExtra(EXTRA_MEDIA_URI);
        if (uriString == null) {
            Toast.makeText(this, R.string.playback_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = Uri.parse(uriString);
        setupAudio(uri);
        setupArtwork(uri, albumArt);
        setupPlaybackControls();
        setupVolumeControl();

        playPauseButton.setOnClickListener(v -> togglePlayPause());
        stopButton.setOnClickListener(v -> stopPlayback());
    }

    private void setupAudio(Uri uri) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                playbackSeekBar.setMax(Math.max(duration, 0));
                totalTimeText.setText(FileUtils.formatDuration(duration));
                mp.seekTo(lastPosition);
                if (shouldResume || lastPosition == 0) {
                    mp.start();
                    playPauseButton.setText(R.string.pause);
                }
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                playPauseButton.setText(R.string.play);
                playbackSeekBar.setProgress(playbackSeekBar.getMax());
            });
            mediaPlayer.prepareAsync();
        } catch (IOException exception) {
            Toast.makeText(this, R.string.playback_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupArtwork(Uri uri, ImageView albumArt) {
        albumArt.setImageResource(R.drawable.ic_audio_placeholder);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);
            byte[] artBytes = retriever.getEmbeddedPicture();
            if (artBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                albumArt.setImageBitmap(bitmap);
            }
            retriever.release();
        } catch (Exception ignored) {
            albumArt.setImageResource(R.drawable.ic_audio_placeholder);
        }
    }

    private void setupPlaybackControls() {
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTimeText.setText(FileUtils.formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSeeking = false;
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void setupVolumeControl() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setText(R.string.play);
        } else {
            mediaPlayer.start();
            playPauseButton.setText(R.string.pause);
        }
    }

    private void stopPlayback() {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.pause();
        mediaPlayer.seekTo(0);
        playbackSeekBar.setProgress(0);
        currentTimeText.setText(FileUtils.formatDuration(0));
        playPauseButton.setText(R.string.play);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(progressRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            shouldResume = mediaPlayer.isPlaying();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setText(R.string.play);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("state_position", mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0);
        outState.putBoolean("state_playing", mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
