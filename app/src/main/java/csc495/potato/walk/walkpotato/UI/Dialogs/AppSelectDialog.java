package csc495.potato.walk.walkpotato.UI.Dialogs;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Adapters.ApplicationAdapter;

/**
 * Created by kenanwarren on 3/26/15.
 */
public class AppSelectDialog extends DialogFragment implements
        OnItemClickListener {
    private ListView appListView;
    private List<ApplicationInfo> appList;
    private ApplicationAdapter appAdapter;
    private Context context;

    public AppSelectDialog() {}

    public AppSelectDialog(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_app, null, false);
        appList = new ArrayList<ApplicationInfo>();
        appListView = (ListView) view.findViewById(R.id.list);

        if (context != null) {
            new LoadApplications().execute();
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        appAdapter = new ApplicationAdapter(getActivity(),
                android.R.layout.simple_list_item_1, appList);

        appListView.setAdapter(appAdapter);

        appListView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        dismiss();
        Toast.makeText(getActivity(), appList.get(position).toString(), Toast.LENGTH_SHORT)
                .show();
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> appList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != context.getPackageManager().getLaunchIntentForPackage(info.packageName)) {
                    appList.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return appList;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            List<ApplicationInfo> allAppList = checkForLaunchIntent(context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));

            for (ApplicationInfo app : allAppList) {
                if(isUserApp(app)) {
                    appList.add(app);
                }
            }

            appAdapter = new ApplicationAdapter(getActivity(),
                    R.layout.app_list_item, appList);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            appListView.setAdapter(appAdapter);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        private boolean isUserApp(ApplicationInfo ai) {
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return (ai.flags & mask) == 0;
        }
    }
}
