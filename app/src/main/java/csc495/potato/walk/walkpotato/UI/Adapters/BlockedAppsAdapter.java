package csc495.potato.walk.walkpotato.UI.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloatSmall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import csc495.potato.walk.walkpotato.R;

public class BlockedAppsAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
    private Context context;
    private PackageManager packageManager;

    public BlockedAppsAdapter(ArrayList<ApplicationInfo> list, Context context) {
        this.list = list;
        this.context = context;
        packageManager = context.getPackageManager();
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
        return list.get(position).uid;
    }

    public boolean addItem(ApplicationInfo app) {
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

        ApplicationInfo applicationInfo = list.get(position);

        ImageView appImageView = (ImageView)view.findViewById(R.id.appImageView);
        appImageView.setImageDrawable(applicationInfo.loadIcon(packageManager));

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.app_name_string);
        listItemText.setText(applicationInfo.loadLabel(packageManager));

        //Handle buttons and add onClickListeners
        ButtonFloatSmall deleteBtn = (ButtonFloatSmall) view.findViewById(R.id.remove_app_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                SharedPreferences blockedApps = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE);
                Set<String> blockedAppSet = blockedApps.getStringSet("app_list", new HashSet<String>());
                SharedPreferences.Editor editor = blockedApps.edit();
                blockedAppSet.remove(list.get(position).packageName);
                editor.putStringSet("app_list", blockedAppSet);
                editor.apply();
                list.remove(position); //or some other task
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
