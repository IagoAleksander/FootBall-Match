package com.filipe.footballmatch.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Activities.ListAvailableEventsActivity;
import com.filipe.footballmatch.Activities.ViewMatchActivity;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Filipe on 27/02/2017.
 */
public class AvailableEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Event> events = new ArrayList<>();
    private ListAvailableEventsActivity activity;

    public AvailableEventsAdapter(Activity activity, ArrayList<Event> events) {

        this.activity = (ListAvailableEventsActivity) activity;
        this.events = events;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_event, parent,
                false);
        MyViewHolder mvh = new MyViewHolder(v);

        return mvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, final int position) {

        final MyViewHolder holder = (MyViewHolder) holder_;
        holder.eventName.setText(events.get(position).getEventName());
        holder.placeName.setText(events.get(position).getName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(events.get(position).getDate());
        holder.date.setText(formattedDate);

        holder.eventLayout.setOnClickListener(v -> {

            Intent intent = new Intent(activity, ViewMatchActivity.class);
            intent.putExtra("eventKey", events.get(position).getEventKey());
            activity.startActivity(intent);
        });

        if (position == events.size()-1) {
            activity.progressDialog.dismiss();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;
        TextView placeName;
        TextView date;
        LinearLayout eventLayout;

        public MyViewHolder(View v) {
            super(v);

            eventName = (TextView) v.findViewById(R.id.textViewName);
            placeName = (TextView) v.findViewById(R.id.textViewPlace);
            date = (TextView) v.findViewById(R.id.textViewDate);
            eventLayout = (LinearLayout) v.findViewById(R.id.layoutEvent);

        }
    }

    @Override
    public int getItemCount() {

        if (events.isEmpty()){
            activity.progressDialog.dismiss();
        }
        return events.size();
    }
}
