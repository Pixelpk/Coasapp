package com.coasapp.coas.utils;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coasapp.coas.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class VoiceRecording {

    Activity activity;
    VoiceRecordListener voiceRecordListener;
    MediaRecorder mRecorder;
    String audioFile;

    BottomSheetDialog bottomSheetDialog;

    public VoiceRecording(Activity activity, VoiceRecordListener voiceRecordListener) {
        this.activity = activity;
        this.voiceRecordListener = voiceRecordListener;
    }

    public VoiceRecording(Activity activity) {
        this.activity = activity;
    }

    public void startRecording() {
        voiceRecordListener.recordingStart();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(44100 * 16);
        /**In the lines below, we create a directory VoiceRecorderSimplifiedCoding/Audios in the phone storage
         * and the audios are being stored in the Audios folder **/
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/JobApp");
        if (!file.exists()) {
            file.mkdirs();
        }

        audioFile = root.getAbsolutePath() + "/COASAPP/" +
                DataFormats.dateFormatFile.format(new Date()) + ".aac";
        Log.d("filename", audioFile);
        mRecorder.setOutputFile(audioFile);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {

        voiceRecordListener.recordingStop(audioFile);

        try {
            //textViewRecord.setText("Recorded");

            mRecorder.stop();
            mRecorder.release();


            Toast.makeText(activity, "Recording Saved", Toast.LENGTH_SHORT).show();

            //startPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;
    }

    public void dismissBottomSheet() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    public void showAudioAlert(Activity activity, final String type, final String file) {
        dismissBottomSheet();
        Log.i("DownloadAudioPlay",file);
        final AudioManager audioManager = ((AudioManager) activity.getSystemService(Context.AUDIO_SERVICE));
        final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

            }
        };

        try {


            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_play_audio, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(view);
            bottomSheetDialog = new BottomSheetDialog(activity);
            bottomSheetDialog.setContentView(view);

            final CheckBox checkBoxPlay = view.findViewById(R.id.checkBoxPlayPause);
            final SeekBar seekBarAudio = view.findViewById(R.id.seekBarAudio);
            final TextView textViewAudio = view.findViewById(R.id.textViewAudioTime);
            final AlertDialog alertDialog = builder.create();
            //alertDialog.show();
            //bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.show();


            final MediaPlayer mediaPlayer = new MediaPlayer();

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) checkBoxPlay.setChecked(true);
                    else checkBoxPlay.setChecked(false);
                    int total = mediaPlayer.getDuration();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    Log.i("Audio", "" + currentPosition);
                    seekBarAudio.setProgress(currentPosition);
                    textViewAudio.setText("" + APPHelper.getTimeString(currentPosition));

                    handler.postDelayed(this, 1000);
                }
            };

            MediaPlayer.OnPreparedListener onPreparedListener =
                    new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            if (type.equalsIgnoreCase("url")) {
                                mp.start();
                                seekBarAudio.setMax(APPHelper.getDurationInMilliseconds(file));
                                mp.setLooping(false);
                                handler.post(runnable);
                            }
                        }
                    };

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (type.equalsIgnoreCase("local")) {
                    seekBarAudio.setMax(mediaPlayer.getDuration());
                    mediaPlayer.setDataSource(activity, Uri.fromFile(new File(file)));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setLooping(false);
                    handler.post(runnable);
                } else {
                    mediaPlayer.setDataSource(file);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(onPreparedListener);
                }
                // Start playback

            }


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    bottomSheetDialog.dismiss();
                    handler.removeCallbacks(runnable);

                }

            });
            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }
            });
            checkBoxPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        if (isChecked) {
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }
                    }
                }
            });

            bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);

                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    handler.removeCallbacks(runnable);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordDialog(Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_record_audio, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

}
