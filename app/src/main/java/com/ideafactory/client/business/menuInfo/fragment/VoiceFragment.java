package com.ideafactory.client.business.menuInfo.fragment;

import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.review.SoundControl;
import com.ideafactory.client.heartbeat.HeartBeatClient;

public class VoiceFragment extends Fragment {
    private TextView voiTextView;
    private SeekBar voiSeekBar;

    public VoiceFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_voice, container, false);
        voiTextView = (TextView) rootView.findViewById(R.id.tv_show_voice_num);
        voiSeekBar = (SeekBar) rootView.findViewById(R.id.sb_show_voi);

        MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
        int max = mainActivity.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int cur = mainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int result = (cur * 100) / max;
        voiSeekBar.setProgress(result);
        voiTextView.setText(result + "%");
        voiSeekBar.setOnSeekBarChangeListener(new MyVoiSeekBar());

        return rootView;
    }

    class MyVoiSeekBar implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            voiTextView.setText(progress + "%");
            SoundControl.setMusicSound(progress * 0.01);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
