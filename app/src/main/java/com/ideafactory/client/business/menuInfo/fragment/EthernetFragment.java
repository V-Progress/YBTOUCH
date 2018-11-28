package com.ideafactory.client.business.menuInfo.fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;

public class EthernetFragment extends Fragment {
    private ImageView bgImageView;
    private TextView hintTextView;
    private Button hint2Button;

    public EthernetFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ethernet, container, false);
        bgImageView = (ImageView) rootView.findViewById(R.id.iv_ethernet_bg);
        hintTextView = (TextView) rootView.findViewById(R.id.tv_ethernet_hint);
        hint2Button = (Button) rootView.findViewById(R.id.btn_ethernet_hint2);

        init();
        return rootView;
    }

    private void init() {
        ConnectivityManager connectivityManager = (ConnectivityManager) EthernetFragment.this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethernetNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        hint2Button.setText(R.string.click_to_net);
        if (!ethernetNetInfo.isConnected()) {
            bgImageView.setBackgroundResource(R.mipmap.ethernet_bg);
            hintTextView.setText(R.string.net_is_fail);
        } else {
            bgImageView.setBackgroundResource(R.mipmap.etherner_bg_con);
            hintTextView.setText(R.string.net_is_ok);
        }
        hint2Button.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(intent);
        }
    };
}
