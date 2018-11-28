package com.ideafactory.client.business.menuInfo.fragment;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.common.power.PowerOffTool;

public class OnOffFragment extends Fragment {
    private CheckBox onoffCheckBox;
    private EditText offEditText, onEditText;
    private TextView onoffHintsTextView;

    public OnOffFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_on_off, container, false);
        onoffCheckBox = (CheckBox) rootView.findViewById(R.id.cb_onoff_timer);
        offEditText = (EditText) rootView.findViewById(R.id.et_onoff_OffTime);
        onEditText = (EditText) rootView.findViewById(R.id.et_onoff_OnTime);
        onoffHintsTextView = (TextView) rootView.findViewById(R.id.tv_onoff_hints);

        setView();
        return rootView;
    }

    private void setView() {
        onoffHintsTextView.setText(R.string.hint_power);
        // 设置定时开关机
        String off = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.getPowerOffTool().POWER_OFF);
        String on = PowerOffTool.getPowerOffTool().getPowerParam(PowerOffTool.getPowerOffTool().POWER_ON);
        if (off.isEmpty()) {
            onoffCheckBox.setChecked(false);
            offEditText.setText("--:--");
            onEditText.setText("--:--");
        } else {
            offEditText.setText(off.split(";")[1]);
            onEditText.setText(on.split(";")[1]);
        }
        //选择按钮
        onoffCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onoffCheckBox.isChecked()) {
                    offEditText.setText("22:00");
                    onEditText.setText("08:00");
                } else {
                    PowerOffTool.getPowerOffTool().setLocalRuntime(null, null);
                    offEditText.setText("--:--");
                    onEditText.setText("--:--");
                }
            }
        });
        offEditText.setOnClickListener(listener);
        onEditText.setOnClickListener(listener);
    }

    /**
     * EditText 输入对话框
     */
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (!onoffCheckBox.isChecked()) {
                return;
            }
            EditText editText = (EditText) view;
            String[] number = editText.getText().toString().split(":");
            TimePickerDialog timePickerDialog = new TimePickerDialog(OnOffFragment.this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                    ((EditText) view).setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    setTime_onoff();
                }
            }, Integer.valueOf(number[0]), Integer.valueOf(number[1]), true);
            timePickerDialog.show();
        }
    };

    private void setTime_onoff() {
        // 定时开关机
        if (onoffCheckBox.isChecked()) {
            String offTimer = offEditText.getText().toString();
            if (!offTimer.matches("(([0-1][0-9])|(2[0-3])):[0-5]\\d")) {
                Toast.makeText(OnOffFragment.this.getActivity(), R.string.off_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            String onTimer = onEditText.getText().toString();
            if (!onTimer.matches("(([0-1][0-9])|(2[0-3])):[0-5]\\d")) {
                Toast.makeText(OnOffFragment.this.getActivity(), R.string.on_fail, Toast.LENGTH_SHORT).show();
                return;
            }
            PowerOffTool.getPowerOffTool().setLocalRuntime(onTimer, offTimer);
        } else {
            PowerOffTool.getPowerOffTool().setLocalRuntime(null, null);
        }
    }

}
