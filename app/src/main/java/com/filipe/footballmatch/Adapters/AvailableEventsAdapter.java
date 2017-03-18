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
import com.filipe.footballmatch.Activities.ViewProfileActivity;
import com.filipe.footballmatch.Models.Event;
import com.filipe.footballmatch.R;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * Created by Filipe on 27/02/2017.
 */
public class AvailableEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Event> events = new ArrayList<>();
    ListAvailableEventsActivity activity;

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
        holder.name.setText(events.get(position).getName());
        holder.address.setText(events.get(position).getAddress());
        holder.date.setText(events.get(position).getDate().toString());

        holder.eventLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ViewMatchActivity.class);
                intent.putExtra("eventKey", events.get(position).getEventKey());
                activity.startActivity(intent);
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView address;
        TextView date;
        LinearLayout eventLayout;

        public MyViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.textViewName);
            address = (TextView) v.findViewById(R.id.textViewPlace);
            date = (TextView) v.findViewById(R.id.textViewDate);
            eventLayout = (LinearLayout) v.findViewById(R.id.layoutEvent);

        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
