package com.filipe.footballmatch;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alks_ander on 27/02/2017.
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, int position) {

        final MyViewHolder holder = (MyViewHolder) holder_;
        holder.name.setText(events.get(position).getName());
        holder.address.setText(events.get(position).getPlace());
        holder.date.setText(events.get(position).getDate().toString());
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView address;
        TextView date;

        public MyViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.textViewName);
            address = (TextView) v.findViewById(R.id.textViewPlace);
            date = (TextView) v.findViewById(R.id.textViewDate);

        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
