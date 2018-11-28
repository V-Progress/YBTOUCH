package com.ideafactory.client.business.menuInfo.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ideafactory.client.R;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.TimeSetUtil;
import com.ideafactory.client.util.xutil.MyXutils;

import java.io.IOException;
import java.util.Date;

public class DateFragment extends Fragment {
    private Button syncBtn;
    private EditText dateEt, timeEt;
    private CheckBox setCheckBox;
    private TextView curYearTextView, curHoursTextView, dateHintsTextView;

    public DateFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_date, container, false);
        syncBtn = (Button) rootView.findViewById(R.id.btn_date_syncTime);
        dateEt = (EditText) rootView.findViewById(R.id.et_date_setDate);
        timeEt = (EditText) rootView.findViewById(R.id.et_date_setTime);
        setCheckBox = (CheckBox) rootView.findViewById(R.id.cb_date_isset);
        curYearTextView = (TextView) rootView.findViewById(R.id.tv_date_year);
        curHoursTextView = (TextView) rootView.findViewById(R.id.tv_date_hours);
        dateHintsTextView = (TextView) rootView.findViewById(R.id.tv_date_hints);

        setView();
        return rootView;
    }

    private void setView() {
        dateHintsTextView.setText(R.string.hint_time);
        Date currentDate = new Date();
        final String date = TYTool.dateToStrByFormat(currentDate, "yyyy-MM-dd");
        final String time = TYTool.dateToStrByFormat(currentDate, "HH:mm");
        curYearTextView.setText(date);
        curHoursTextView.setText(time);

        setCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setCheckBox.isChecked()) {
                    dateEt.setText(date);
                    timeEt.setText(time);
                } else {
                    dateEt.setText("");
                    timeEt.setText("");
                }
            }
        });

        dateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!setCheckBox.isChecked()) {
                    return;
                }
                EditText editText = (EditText) view;
                String[] number = editText.getText().toString().split("-");
                DatePickerDialog datePickerDialog = new DatePickerDialog(DateFragment.this.getActivity(), new DatePickerDialog
                        .OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker DatePickerView, int year, int monthOfYear, int dayOfMonth) {
                        String setDateStr = String.format("%02d", year) + "-" + String.format("%02d", monthOfYear + 1) + "-" +
                                String.format("%02d", dayOfMonth);
                        ((EditText) view).setText(setDateStr);
                        curYearTextView.setText(setDateStr);
                        try {
                            TimeSetUtil.setDate(year, monthOfYear, dayOfMonth);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, Integer.valueOf(number[0]), Integer.valueOf(number[1]) - 1, Integer.valueOf(number[2]));
                datePickerDialog.show();
            }
        });

        timeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!setCheckBox.isChecked()) {
                    return;
                }
                EditText editText = (EditText) view;
                String[] number = editText.getText().toString().split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(DateFragment.this.getActivity(), new TimePickerDialog
                        .OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        String setTimeStr = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
                        ((EditText) view).setText(setTimeStr);
                        curHoursTextView.setText(setTimeStr);
                        try {
                            TimeSetUtil.setTime(hourOfDay, minute);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, Integer.valueOf(number[0]), Integer.valueOf(number[1]), true);
                timePickerDialog.show();
            }
        });

        //时间同步
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TYTool.getServiceTime();
                MyXutils.getInstance().post(ResourceUpdate.SETTIME, null, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        if (result.startsWith("\"")) {
                            result = result.substring(1, result.length() - 1);
                        }
                        Date setDate = new Date();
                        setDate.setTime(Long.parseLong(result));
                        curYearTextView.setText(TYTool.dateToStrByFormat(setDate, "yyyy-MM-dd"));
                        curHoursTextView.setText(TYTool.dateToStrByFormat(setDate, "HH:mm"));
                    }

                    @Override
                    public void onError(Throwable ex) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
        });
    }
}
