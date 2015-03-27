package csc495.potato.walk.walkpotato.UI.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Models.BlockedApp;

public class BlockedAppsAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<BlockedApp> list = new ArrayList<BlockedApp>();
    private Context context;

    public BlockedAppsAdapter(ArrayList<BlockedApp> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    public boolean addItem(BlockedApp app) {
        boolean isAdded = list.add(app);
        notifyDataSetChanged();
        return isAdded;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.blocked_app_item, null);
        }

        ImageView appImageView = (ImageView)view.findViewById(R.id.appImageView);
        appImageView.setImageDrawable(list.get(position).getAppIcon());

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.app_name_string);
        listItemText.setText(list.get(position).getAppName());

        //Handle buttons and add onClickListeners
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.remove_app_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                list.remove(position); //or some other task
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
