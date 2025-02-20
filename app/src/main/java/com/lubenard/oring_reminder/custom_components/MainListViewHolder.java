package com.lubenard.oring_reminder.custom_components;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.AfterBootBroadcastReceiver;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView weared_from;
    private TextView weared_to;
    private TextView weared_during;
    private Context context;
    private CustomListAdapter.onListItemClickListener onListItemClickListener;

    public MainListViewHolder(@NonNull View itemView, CustomListAdapter.onListItemClickListener onListItemClickListener) {
        super(itemView);
        weared_from = itemView.findViewById(R.id.custom_view_date_weared_from);
        weared_to = itemView.findViewById(R.id.custom_view_date_weared_to);
        weared_during = itemView.findViewById(R.id.custom_view_date_time_weared);
        this.onListItemClickListener = onListItemClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Convert the timeWeared from a int into a readable hour:minutes format
     * @param timeWeared timeWeared is in minutes
     * @return a string containing the time the user weared the protection
     */
    private String convertTimeWeared(int timeWeared) {
        if (timeWeared < 60)
            return timeWeared + context.getString(R.string.minute_with_M_uppercase);
        else if (timeWeared <= 1440)
            return String.format("%dh%02dm", timeWeared / 60, timeWeared % 60);
        else
            return context.getString(R.string.more_than_one_day);
    }

    /**
     * Get the total time pause for one session
     * @param datePut The datetime the user put the protection
     * @param entryId the entry id of the session
     * @param dateRemoved The datetime the user removed the protection
     * @return the total time in Minutes of new wearing time
     */
    private int getTotalTimePause(String datePut, long entryId, String dateRemoved) {
        long oldTimeBeforeRemove;
        int newValue;
        long totalTimePause = 0;

        if (dateRemoved == null)
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
        else
            oldTimeBeforeRemove = Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES);

        totalTimePause = AfterBootBroadcastReceiver.computeTotalTimePause(CustomListAdapter.getDbManager(), entryId);
        newValue = (int) (oldTimeBeforeRemove - totalTimePause);
        return (newValue < 0) ? 0 : newValue;
    }

    public void updateElementDatas(RingModel dataModel, Context context) {
        this.context = context;
        String[] datePut = dataModel.getDatePut().split(" ");
        weared_from.setText(datePut[0] + "\n" + datePut[1]);

        if (!dataModel.getDateRemoved().equals("NOT SET YET")) {
            String[] dateRemoved = dataModel.getDateRemoved().split(" ");
            weared_to.setText(dateRemoved[0] + "\n" + dateRemoved[1]);
        } else
            weared_to.setText(dataModel.getDateRemoved());

        if (dataModel.getIsRunning() == 0) {
            int totalTimePause = getTotalTimePause(dataModel.getDatePut(), dataModel.getId(), dataModel.getDateRemoved());
            if (totalTimePause / 60 >= 15)
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            else
                weared_during.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            weared_during.setText(convertTimeWeared(totalTimePause));
        }
        else {
            long timeBeforeRemove = getTotalTimePause(dataModel.getDatePut(), dataModel.getId(), null);
            weared_during.setTextColor(context.getResources().getColor(R.color.yellow));
            weared_during.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
        }
    }

    @Override
    public void onClick(View view) {
        onListItemClickListener.onListItemClickListener(getAdapterPosition());
    }
}
