package com.ideafactory.client.business.draw.review;

import android.media.MediaPlayer;

import java.io.IOException;

public class BackGroundMusic {

	private static MediaPlayer mediaPlay ;
	
	public BackGroundMusic() {
		if (mediaPlay == null) {
			mediaPlay = new MediaPlayer();

		} else {
			boolean isPlaying = mediaPlay.isPlaying();
			if (isPlaying) {
				stopMedia();
			}
		}
	}

	public void initMediaPlay (String musicPath, Boolean isLooping) {
		try {
			boolean isPlaying = mediaPlay.isPlaying();
			if (isPlaying) {
				stopMedia();
			}
			mediaPlay.reset();
			mediaPlay.setLooping(true);
			mediaPlay.setDataSource(musicPath);
			mediaPlay.prepare();
		} catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * 有叫号时降低声音
     */
    public void reduceMediaVolume(){
        if (mediaPlay!= null && mediaPlay.isPlaying()) {
            mediaPlay.setVolume(0.2f, 0.2f);
        }
    }

    /**
     * 有叫号时降低声音
     */
    public void addMediaVolume(){
        if (mediaPlay!= null && mediaPlay.isPlaying()) {
            mediaPlay.setVolume(1.0f, 1.0f);
        }
    }

	public void playMedia () {
		if (mediaPlay!= null && !mediaPlay.isPlaying()) {
			mediaPlay.start();
		}
	}
	
	public void stopMedia () {
		if (mediaPlay!= null && mediaPlay.isPlaying()) {
			mediaPlay.stop();
		}
	}

    public void pause () {
        if (mediaPlay!= null && mediaPlay.isPlaying()) {
            mediaPlay.pause();
        }
    }

    public boolean isMusicPlay(){
		return mediaPlay != null && mediaPlay.isPlaying();
	}
}
