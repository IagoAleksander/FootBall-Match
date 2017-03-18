package com.filipe.footballmatch.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Activities.ListUsersActivity;
import com.filipe.footballmatch.Activities.ViewProfileActivity;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;

import java.util.ArrayList;

/**
 * Created by Filipe on 27/02/2017.
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, final int position) {

        final MyViewHolder holder = (MyViewHolder) holder_;
        holder.name.setText(users.get(position).getName());
        holder.email.setText(users.get(position).getEmail());

        if (activity.isFromCreateMatch) {
            holder.checkbox.setVisibility(View.VISIBLE);

            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("userId",users.get(position).getUserKey());
                        activity.setResult(Activity.RESULT_OK,returnIntent);
                        activity.finish();
                    }
                }
            });
        }


        holder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, ViewProfileActivity.class);
                intent.putExtra("userKey", users.get(position).getUserKey());
                activity.startActivity(intent);
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView email;
        CheckBox checkbox;
        LinearLayout userLayout;

        public MyViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.user_name);
            email = (TextView) v.findViewById(R.id.user_email);
            checkbox = (CheckBox) v.findViewById(R.id.user_checkBox);
            userLayout = (LinearLayout) v.findViewById(R.id.layoutUser);

        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
