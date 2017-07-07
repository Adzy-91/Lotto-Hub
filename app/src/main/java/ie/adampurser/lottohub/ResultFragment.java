package ie.adampurser.lottohub;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.Date;


public class ResultFragment extends Fragment {
    private static final String TAG_DEBUG = "DEBUG.ResultsFragment";
    private static final String KEY_COMPLETE_RESULT = "resultsFragmentDraws";

    private RecyclerView mRecyclerView;
    private CardView mButtonPrevious;
    private CardView mButtonNext;
    private FloatingActionButton mFabCalendar;
    private Toolbar mToolbarBottom;

    private Result[] mCompleteResult;
    private ResultBlockAdapter mAdapter;
    private Date mCurrentDrawDate;
    private Date mLatestDrawDate;

    private OnDrawDateChangedListener mOnDrawDateChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCompleteResult = (Result[]) getArguments().get(KEY_COMPLETE_RESULT);
        mAdapter = new ResultBlockAdapter(mCompleteResult, new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        }, ResultBlockAdapter.ADAPTER_LOCATION_RESULTS);

        mCurrentDrawDate = mCompleteResult[0].getDate();
        mLatestDrawDate = mCurrentDrawDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, parent, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.resultBlockContainerResultFragment);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);


        mToolbarBottom = (Toolbar) view.findViewById(R.id.toolbarResultFragment);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean scrollingUp;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollingUp = dy > 0;
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Hide
                    if (scrollingUp) {
                        mToolbarBottom.animate()
                                .translationY(mToolbarBottom.getHeight())
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(180);
                    }
                    // Show
                    else {
                        mToolbarBottom.animate()
                                .translationY(0)
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(180);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        }, 200);
                    }
                }
            }
        });

        mButtonPrevious = (CardView) view.findViewById(R.id.buttonPrevious);
        mButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Date previousDrawDate = mCompleteResult[0].getPreviousDrawDate();
            changeDraw(previousDrawDate);
        }
        });


        mButtonNext = (CardView) view.findViewById(R.id.buttonNext);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View view) {
            Date nextDrawDate = mCompleteResult[0].getNextDrawDate();
                if (mCurrentDrawDate.equals(mLatestDrawDate)) {
                    Toast.makeText(getContext(),
                            R.string.toast_latest_result_reached, Toast.LENGTH_SHORT).show();
                } else {
                    changeDraw(nextDrawDate);
                }
            }
        });

        mFabCalendar = (FloatingActionButton) view.findViewById(R.id.fabResultFragment);
        mFabCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarFragment calendarFragment = CalendarFragment.newInstance(
                        mCurrentDrawDate, mLatestDrawDate, mCompleteResult[0].getDrawType());
                calendarFragment.show(getChildFragmentManager(), CalendarFragment.FRAGMENT_ID);
            }
        });

        return view;
    }

    private void changeDraw(final Date newDate) {

        // check if we have a connection
        if(!AppUtils.hasInternetConnection(getContext())) {
            AppUtils.displayNoConnectionToast(getContext());
            return;
        }

        mOnDrawDateChangedListener.onDrawDateChanged(newDate);
    }

    public void notifyAdapter() {
        mAdapter = new ResultBlockAdapter(mCompleteResult, new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        }, ResultBlockAdapter.ADAPTER_LOCATION_RESULTS);
        mRecyclerView.setAdapter(mAdapter);
        mCurrentDrawDate = mCompleteResult[0].getDate();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mOnDrawDateChangedListener = (OnDrawDateChangedListener) getParentFragment();
    }

    public static ResultFragment newInstance(Result[] completeResult) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_COMPLETE_RESULT, completeResult);

        ResultFragment fragment = new ResultFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
