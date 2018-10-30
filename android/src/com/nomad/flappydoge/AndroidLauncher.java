package com.nomad.flappydoge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import Interfaces.Services;

public class AndroidLauncher extends AndroidApplication implements Services {

    private static final String adUnitId = "ca-app-pub-3660065570474827/7182835425";
    private static final String storeLink = "https://play.google.com/store/apps/details?id=com.nomad.flappydoge";
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);

        View gameView = initializeForView(new FlappyDoge(this), config);
        //initialize(new FlappyDoge(), config);
        RelativeLayout.LayoutParams gameViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        gameViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        gameViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        gameView.setLayoutParams(gameViewParams);
        layout.addView(gameView);

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnitId);

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        adView.loadAd(adRequestBuilder.build());

        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        topParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        topParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layout.addView(adView, topParams);
        adView.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    public void Share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Play Flappy Doge! " + Uri.parse(storeLink));
        startActivity(Intent.createChooser(shareIntent, "Share File Using!"));
    }
}
