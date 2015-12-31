package com.bruce.emehiser.audiorecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bruce Emehiser on 12/23/2015.
 *
 * Edit Track Adapter
 */
public class EditTrackAdapter extends ArrayAdapter<EditTrack> {

    static class ViewHolder {
        TextView artist;
        TextView album;
        TextView title;
        TextView genre;
        TextView year;
        TextView description;
        TextView length;
        TextView startPosition;
        TextView endPosition;
    }

    private final Context mContext;
    private final ArrayList<EditTrack> mEditTracks;

    public EditTrackAdapter(Context context, ArrayList<EditTrack> editTracks) {
        super(context, -1, editTracks);
        this.mContext = context;
        this.mEditTracks = editTracks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the view
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if(convertView == null) {
            // inflate track layout view
            convertView = inflater.inflate(R.layout.edit_track, parent, false);

            // instantiate new holder
            holder = new ViewHolder();

            // get the widget views
            holder.artist = (TextView) convertView.findViewById(R.id.track_artist);
            holder.album = (TextView) convertView.findViewById(R.id.track_album);
            holder.title = (TextView) convertView.findViewById(R.id.track_title);
            holder.genre = (TextView) convertView.findViewById(R.id.track_genre);
            holder.year = (TextView) convertView.findViewById(R.id.track_year);
            holder.length = (TextView) convertView.findViewById(R.id.track_length);
            holder.description = (TextView) convertView.findViewById(R.id.track_description);
            holder.startPosition = (TextView) convertView.findViewById(R.id.edit_track_start_time);
            holder.endPosition = (TextView) convertView.findViewById(R.id.edit_track_end_time);

            // set holder tag
            convertView.setTag(holder);
        }
        else {
            // get pre existing holder
            holder = (ViewHolder) convertView.getTag();
        }

        // get the current track for this view
        EditTrack editTrack = mEditTracks.get(position);

        // set the data on the views
        holder.artist.setText(editTrack.getArtist());
        holder.album.setText(editTrack.getAlbum());
        holder.title.setText(editTrack.getTitle());
        holder.genre.setText(String.valueOf(editTrack.getGenre()));
        holder.year.setText(editTrack.getYear());
        holder.length.setText(String.valueOf(editTrack.getLength()));
        holder.description.setText(editTrack.getComment());
        holder.startPosition.setText(String.valueOf(editTrack.getStartTime()));
        holder.endPosition.setText(String.valueOf(editTrack.getEndTime()));

        return convertView;
    }

}
