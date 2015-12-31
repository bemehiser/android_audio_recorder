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
 * Track Array Adapter, used for making pretty looking tracks
 */
public class TrackAdapter extends ArrayAdapter<ID3v1Tag> {

    static class ViewHolder {
        TextView artist;
        TextView album;
        TextView title;
        TextView genre;
        TextView year;
        TextView description;
        TextView length;
    }

    private final Context mContext;
    private final ArrayList<ID3v1Tag> mID3v1Tags;

    public TrackAdapter(Context context, ArrayList<ID3v1Tag> ID3v1Tags) {
        super(context, -1, ID3v1Tags);
        this.mContext = context;
        this.mID3v1Tags = ID3v1Tags;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the view
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if(convertView == null) {
            // inflate track layout view
            convertView = inflater.inflate(R.layout.track, parent, false);

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

            // set holder tag
            convertView.setTag(holder);
        }
        else {
            // get pre existing holder
            holder = (ViewHolder) convertView.getTag();
        }

        // get the current track for this view
        ID3v1Tag ID3v1Tag = mID3v1Tags.get(position);

        // set the data on the views
        holder.artist.setText(ID3v1Tag.getArtist());
        holder.album.setText(ID3v1Tag.getAlbum());
        holder.title.setText(ID3v1Tag.getTitle());
        holder.genre.setText(ID3v1Tag.getGenre());
        holder.year.setText(String.valueOf(ID3v1Tag.getYear()));
//        holder.length.setText(String.valueOf(ID3v1Tag.getLength()));
        holder.description.setText(ID3v1Tag.getComment());

        return convertView;
    }
}
