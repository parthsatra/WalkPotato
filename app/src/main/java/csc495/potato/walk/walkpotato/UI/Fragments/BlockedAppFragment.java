package csc495.potato.walk.walkpotato.UI.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import csc495.potato.walk.walkpotato.R;
import csc495.potato.walk.walkpotato.UI.Adapters.BlockedAppsAdapter;
import csc495.potato.walk.walkpotato.UI.Dialogs.AppSelectDialog;
import csc495.potato.walk.walkpotato.UI.Widgets.FixButtonFloat;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BlockedAppFragment extends Fragment implements AbsListView.OnItemClickListener, AppSelectDialog.OnAppSelectListener {
    private Context context;
    private ArrayList<ApplicationInfo> blockedAppList;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private ButtonFloat addButton;

    /**
     * The Adapters which will be used to populate the ListView/GridView with
     * Views.
     */
    private BlockedAppsAdapter mAdapter;

    public static BlockedAppFragment newInstance() {
        BlockedAppFragment fragment = new BlockedAppFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlockedAppFragment() {
    }

    public BlockedAppFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockedAppList = new ArrayList<ApplicationInfo>();

        SharedPreferences blockedApps = this.getActivity().getSharedPreferences("blocked_apps", Context.MODE_PRIVATE);
        Set<String> blockedAppSet = blockedApps.getStringSet("app_list", new HashSet<String>());

        for (String packageName : blockedAppSet) {
            try {
                ApplicationInfo app = this.getActivity().getPackageManager().getApplicationInfo(packageName, 0);
                blockedAppList.add(app);
            } catch (Exception e) {
                Log.e("Error", "Couldn't find package with name : " + packageName);
            }
        }

        mAdapter = new BlockedAppsAdapter(blockedAppList, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blockedapp, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        FixButtonFloat appAddBtn = (FixButtonFloat) view.findViewById(android.R.id.button1);
        appAddBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog();
            }
        });

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onBlockAppFragmentInteraction(blockedAppList.get(position).toString());
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void showDialog() {
        FragmentManager manager = getFragmentManager();
        AppSelectDialog dialog = new AppSelectDialog(context);
        dialog.setTargetFragment(this, 0);
        dialog.show(manager, "dialog");
    }

    @Override
    public void onAppSelectListener(ApplicationInfo appInfo) {

        Gson gson = new Gson();

        SharedPreferences blockedApps = this.getActivity().getSharedPreferences("blocked_apps", Context.MODE_PRIVATE);
        Set<String> blockedAppSet = blockedApps.getStringSet("app_list", new HashSet<String>());
        SharedPreferences.Editor editor = blockedApps.edit();
        blockedAppSet.add(appInfo.packageName);
        editor.putStringSet("app_list", blockedAppSet);
        editor.apply();
        mAdapter.addItem(appInfo);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onBlockAppFragmentInteraction(String id);
    }
}
