package idv.hsu.wifiscannerlog;

import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import idv.hsu.wifiscannerlog.data.AccessPoint;
import idv.hsu.wifiscannerlog.data.EnumChannels;
import idv.hsu.wifiscannerlog.data.LogDbHelper;
import idv.hsu.wifiscannerlog.data.LogDbSchema;
import idv.hsu.wifiscannerlog.data.WifiChannels;

public class MainListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = MainListAdapter.class.getSimpleName();
    private static final boolean D = true;

    private LayoutInflater inflater;
    private List<String> groupData;
    private List<List<AccessPoint>> childData;
    private WifiChannels<EnumChannels> channles;

    private LogDbHelper dbHelper;

    public MainListAdapter(LayoutInflater inflater, List<String> group, List<List<AccessPoint>> child) {
        this.inflater = inflater;
        groupData = group;
        childData = child;
        channles = new WifiChannels<EnumChannels>(EnumChannels.class);
        dbHelper = new LogDbHelper(inflater.getContext());
        try {
            dbHelper.create();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error("Unable to copy database.");
        }
        dbHelper.open();
        dbHelper.getReadableDatabase();
    }

    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(groupPosition).get(childPosition);
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

    private static class GroupViewHolder {
        private TextView tv_title;
        private TextView tv_manufacturer;
        private ImageView iv_favor;
        public GroupViewHolder(View view) {
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_manufacturer = (TextView) view.findViewById(R.id.tv_manufacturer);
            iv_favor = (ImageView) view.findViewById(R.id.iv_favor);
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.main_list_group, null);
            GroupViewHolder holder = new GroupViewHolder(rowView);
            rowView.setTag(holder);
        }
        final GroupViewHolder holder = (GroupViewHolder) rowView.getTag();
        holder.tv_title.setText(groupData.get(groupPosition));
        holder.tv_manufacturer.setText(dbHelper.queryManufacture(groupData.get(groupPosition)));
        boolean isFavor = dbHelper.isBssidSaved(groupData.get(groupPosition));
        holder.iv_favor.setImageResource(isFavor ? R.drawable.ic_favorite_red_24dp : R.drawable.ic_favorite_black_24dp);

        if (isFavor) {
            List<AccessPoint> list = childData.get(groupPosition);
            for (int x = 0; x < list.size(); x++) {
                ContentValues values = new ContentValues();
                values.put(LogDbSchema.BSSID, list.get(x).getBssid());
                values.put(LogDbSchema.SSID, list.get(x).getSsid());
            }
        }
        return rowView;
    }

    private static class ChildViewHolder {
        private TextView tv_ssid;
        private TextView tv_capabilities;
        private TextView tv_frequency; // TODO plus channel num after frequency.
        private TextView tv_level;
        public ChildViewHolder(View view) {
            tv_ssid = (TextView) view.findViewById(R.id.tv_ssid_value);
            tv_capabilities = (TextView) view.findViewById(R.id.tv_capabilities_value);
            tv_frequency = (TextView) view.findViewById(R.id.tv_frequency_value);
            tv_level = (TextView) view.findViewById(R.id.tv_level_value);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.main_list_child, null);
            ChildViewHolder holder = new ChildViewHolder(rowView);
            rowView.setTag(holder);
        }
        final ChildViewHolder holder = (ChildViewHolder) rowView.getTag();
        AccessPoint ap = childData.get(groupPosition).get(childPosition);
        holder.tv_ssid.setText(ap.getSsid());
        holder.tv_capabilities.setText(ap.getCapabilities());
        StringBuilder channel = new StringBuilder("");
        try {
            if (channles.getChannel(ap.getFrequency()) != null) {
                String tmp = channles.getChannel(ap.getFrequency()).toString();
                String[] tmpArray = tmp.split("_");
                channel.append("  (" + tmpArray[1] + " " + tmpArray[2] + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.tv_frequency.setText(String.valueOf(ap.getFrequency()) + channel);
        holder.tv_level.setText(String.valueOf(ap.getLevel()));

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}