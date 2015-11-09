package idv.hsu.wifiscannerlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import idv.hsu.wifiscannerlog.data.AccessPoint;

public class MainListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = MainListAdapter.class.getSimpleName();
    private static final boolean D = true;

    private LayoutInflater inflater;
    private List<String> groupData;
    private List<List<AccessPoint>> childData;

    public MainListAdapter(LayoutInflater inflater, List<String> group, List<List<AccessPoint>> child) {
        this.inflater = inflater;
        groupData = group;
        childData = child;
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
        public GroupViewHolder(View view) {
            tv_title = (TextView)view.findViewById(R.id.tv_title);
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
        holder.tv_frequency.setText(String.valueOf(ap.getFrequency()));
        holder.tv_level.setText(String.valueOf(ap.getLevel()));

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}