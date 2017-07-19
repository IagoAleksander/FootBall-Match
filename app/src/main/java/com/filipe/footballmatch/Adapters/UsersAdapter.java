package com.filipe.footballmatch.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.filipe.footballmatch.Activities.ListUsersActivity;
import com.filipe.footballmatch.Activities.ViewProfileActivity;
import com.filipe.footballmatch.Models.Person;
import com.filipe.footballmatch.R;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Filipe on 27/02/2017.
 */
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Person> users = new ArrayList<>();
    private ListUsersActivity activity;
    private ProgressDialog pd;

    public UsersAdapter(Activity activity, ArrayList<Person> users) {

        this.activity = (ListUsersActivity) activity;
        this.users = users;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_user, parent,
                false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, final int position) {

        final MyViewHolder holder = (MyViewHolder) holder_;
        holder.name.setText(users.get(position).getName());
        holder.email.setText(users.get(position).getEmail());

        if (users.get(position).getImageUrl() != null && !users.get(position).getImageUrl().isEmpty()) {
            new DownloadImageTask(holder.image).execute(users.get(position).getImageUrl());
        }

        if (activity.isFromCreateMatch) {
            holder.checkbox.setVisibility(View.VISIBLE);

            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("userId", users.get(position).getUserKey());
                    activity.setResult(Activity.RESULT_OK, returnIntent);
                    activity.finish();
                }
            });
        }


        holder.userLayout.setOnClickListener(v -> {

            Intent intent = new Intent(activity, ViewProfileActivity.class);
            intent.putExtra("userKey", users.get(position).getUserKey());
            activity.startActivity(intent);
        });

        if (position == users.size()-1) {
            activity.progressDialog.dismiss();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView email;
        CheckBox checkbox;
        LinearLayout userLayout;
        CircleImageView image;

        MyViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.user_name);
            email = (TextView) v.findViewById(R.id.user_email);
            checkbox = (CheckBox) v.findViewById(R.id.user_checkBox);
            userLayout = (LinearLayout) v.findViewById(R.id.layoutUser);
            image = (CircleImageView) v.findViewById(R.id.user_image);

        }
    }

    @Override
    public int getItemCount() {

        if (users.isEmpty()){
            activity.progressDialog.dismiss();
        }
        return users.size();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pd = new ProgressDialog(activity);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }


        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            pd.dismiss();
            bmImage.setImageBitmap(result);
        }
    }
}
