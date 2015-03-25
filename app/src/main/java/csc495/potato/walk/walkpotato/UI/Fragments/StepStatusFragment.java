package csc495.potato.walk.walkpotato.UI.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import csc495.potato.walk.walkpotato.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StepStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepStatusFragment extends Fragment {
    private PieChart mPieChart;
    private TextView stepsView;
    private TextView totalPotatoesView;

    private final int NUM_STEPS_PER_CAL = 20;
    private final int NUM_CALS_PER_POTATO = 138;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StepStatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StepStatusFragment newInstance() {
        StepStatusFragment fragment = new StepStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StepStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v =  inflater.inflate(R.layout.fragment_step_status, container, false);

        int stepsTaken = 15000;
        int goalSteps = 20000;
        int numPotatoesBurned = calculatePotatoesBurned(stepsTaken);

        mPieChart = (PieChart) v.findViewById(R.id.graph);
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalPotatoesView = (TextView) v.findViewById(R.id.potatoes);

        stepsView.setText(Integer.toString(stepsTaken));
        totalPotatoesView.setText(numPotatoesBurned == 1 ? numPotatoesBurned + " potato"
                : numPotatoesBurned + " potatoes");

        mPieChart.clearChart();
        mPieChart.addPieSlice(new PieModel("", stepsTaken, Color.parseColor("#143ACC")));
        mPieChart.addPieSlice(new PieModel("", goalSteps - stepsTaken, Color.parseColor("#FF3A00")));

        mPieChart.startAnimation();

        ((TextView) v.findViewById(R.id.unit)).setText(getString(R.string.steps));

        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        mPieChart.startAnimation();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private int calculatePotatoesBurned(int stepsTaken) {
        return (stepsTaken / NUM_STEPS_PER_CAL) / NUM_CALS_PER_POTATO;

    }

}
