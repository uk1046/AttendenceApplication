package com.example.attendenceapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class MyCalender extends DialogFragment {
    Calendar  calendar = Calendar.getInstance();

    public setOnCalenderOkClickListener onCalenderOkClickListener;

    public interface setOnCalenderOkClickListener{
        void onClick(int year, int month, int day);
    }

    public void setOnCalenderOkClickListener(setOnCalenderOkClickListener onCalenderOkClickListener){
        this.onCalenderOkClickListener = onCalenderOkClickListener;
    }
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return new DatePickerDialog(getActivity(), ((view, year, month, dayofMonth) -> {
            onCalenderOkClickListener.onClick(year, month, dayofMonth);
        }), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }
    void setDate(int year, int month, int day){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
    }
    String getDate(){
        return DateFormat.format("dd.MM.yyyy", calendar).toString();
    }
}
