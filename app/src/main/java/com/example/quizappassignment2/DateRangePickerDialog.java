package com.example.quizappassignment2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

public class DateRangePickerDialog extends Dialog {

    private DatePicker datePicker;
    private Button btnSelectStart, btnSelectEnd;
    private Date selectedStartDate, selectedEndDate;

    private OnDateRangeSelectedListener listener;

    public DateRangePickerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_range_picker);

        datePicker = findViewById(R.id.datePicker);
        btnSelectStart = findViewById(R.id.btnSelectStart);
        btnSelectEnd = findViewById(R.id.btnSelectEnd);

        // Set click listeners for the buttons
        btnSelectStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStartDate = getDateFromDatePicker(datePicker);
                listener.onStartDateSelected(selectedStartDate);
                dismiss();
            }
        });

        btnSelectEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedEndDate = getDateFromDatePicker(datePicker);
                listener.onEndDateSelected(selectedEndDate);
                dismiss();
            }
        });
    }

    // Helper method to extract Date object from DatePicker
    private Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    // Getter methods for retrieving selected start and end dates
    public Date getSelectedStartDate() {
        return selectedStartDate;
    }

    public Date getSelectedEndDate() {
        return selectedEndDate;
    }

    // Setter method for setting the date range selected listener
    public void setOnDateRangeSelectedListener(OnDateRangeSelectedListener listener) {
        this.listener = listener;
    }

    // Interface for communicating date range selection to the caller
    public interface OnDateRangeSelectedListener {
        void onStartDateSelected(Date startDate);
        void onEndDateSelected(Date endDate);
    }
}
