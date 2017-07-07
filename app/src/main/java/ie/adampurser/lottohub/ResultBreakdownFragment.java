package ie.adampurser.lottohub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ResultBreakdownFragment extends Fragment{
    private static String TAG_DEBUG = "ResultBreakDownFragment.DEBUG";
    private static final String KEY_COMPLETE_RESULT = "resultBreakDownDraws";

    private RecyclerView mRecyclerView;

    private Result[] mCompleteResult;
    private ResultBreakdownTableAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompleteResult = (Result[]) getArguments().get(KEY_COMPLETE_RESULT);
        mAdapter = new ResultBreakdownTableAdapter(getContext(), mCompleteResult);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_breakdown, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.resultBlockContainerResultBreakdown);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public void notifyAdapter() {
        mAdapter = new ResultBreakdownTableAdapter(getContext(), mCompleteResult);
        mRecyclerView.setAdapter(mAdapter);
    }

    public static ResultBreakdownFragment newInstance(Result[] completeResult) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_COMPLETE_RESULT, completeResult);

        ResultBreakdownFragment fragment = new ResultBreakdownFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
