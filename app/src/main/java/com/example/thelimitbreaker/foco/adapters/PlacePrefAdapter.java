package com.example.thelimitbreaker.foco.adapters;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.thelimitbreaker.foco.R;
import com.example.thelimitbreaker.foco.models.PlacePrefs;

import java.util.ArrayList;

public class PlacePrefAdapter extends BaseExpandableListAdapter {
    private ArrayList<PlacePrefs> places;
    private Context context;
    public PlacePrefAdapter(ArrayList<PlacePrefs> places,Context context){
        this.places=places;
        this.context=context;
    }
    @Override
    public int getGroupCount() {
        return places.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return places.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return places.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        PlacePrefs thisPrefs = (PlacePrefs) getGroup(groupPosition);
        if (convertView==null){
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=li.inflate(R.layout.layout_place_prefs,parent,false);
        }

        ((TextView)convertView.findViewById(R.id.tvPlaceTitle)).setText(thisPrefs.getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PlacePrefs thisPref = (PlacePrefs) getChild(groupPosition,childPosition);
            LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=li.inflate(R.layout.layout_place_child_view,parent,false);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class GroupHolder{
        TextView tvPlaceName;
        TextView tvTime;
        SwitchCompat switchCompat;

        GroupHolder(View itemView){
            tvPlaceName=itemView.findViewById(R.id.tvPlaceTitle);
            tvTime=itemView.findViewById(R.id.tvTimeHead);
            switchCompat=itemView.findViewById(R.id.turnOnOff);
        }
    }


}
