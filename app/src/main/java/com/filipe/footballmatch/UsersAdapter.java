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
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Person> users = new ArrayList<>();
    ListUsersActivity activity;

    public UsersAdapter(Activity activity, ArrayList<Person> users) {

        this.activity = (ListUsersActivity) activity;
        this.users = users;

    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_user, parent,
                false);
        MyViewHolder mvh = new MyViewHolder(v);

        return mvh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, int position) {

        final MyViewHolder holder = (MyViewHolder) holder_;
        holder.name.setText(users.get(position).getName());
        holder.age.setText(users.get(position).getAge());
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView age;

        public MyViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.textViewName);
            age = (TextView) v.findViewById(R.id.textViewAge);

        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
