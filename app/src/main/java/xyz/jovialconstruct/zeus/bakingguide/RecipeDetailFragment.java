package xyz.jovialconstruct.zeus.bakingguide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.jovialconstruct.zeus.bakingguide.adapters.IngredientAdapter;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.utilities.MediaIsh;
import xyz.jovialconstruct.zeus.bakingguide.utilities.VideoCacheProxyFactory;

/**
 * A fragment representing a single Recipe detail screen.
 * This fragment is either contained in a {@link RecipeActivity}
 * in two-pane mode (on tablets) or a {@link RecipeDetailActivity}
 * on handsets.
 */
public class RecipeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_JSON = "item_json";
    public static final String ARG_ITEM_IS_STEP = "is_step";
    private static final String LOG_TAG = RecipeDetailFragment.class.getSimpleName();
    private String mItemJson;
    private Recipe.Step mStep;
    private List<Recipe.Ingredient> mIngredients = new ArrayList<>();
    private String mTitle;
    private Context mContext;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer simpleExoPlayer;
    private boolean mValidPlayer = false;
    private MediaIsh mediaIsh;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecipeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        mContext = getActivity();
        if (getArguments().containsKey(ARG_ITEM_JSON)) {
            mItemJson = getArguments().getString(ARG_ITEM_JSON);
            if (getArguments().containsKey(ARG_ITEM_IS_STEP)) {
                if (getArguments().getBoolean(ARG_ITEM_IS_STEP)) {
                    mStep = gson.fromJson(mItemJson, Recipe.Step.class);
                    mTitle = mStep.getShortDescription();
                } else {
                    Collections.addAll(mIngredients, gson.fromJson(mItemJson, Recipe.Ingredient[].class));
                    mTitle = getString(R.string.recipe_ingredient_item_title);
                }
            }

        }
    }

    private MediaSource newVideoSource(String url) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(getActivity(), "AndroidVideoCache sample");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
    }

    private SimpleExoPlayer newSimpleExoPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        return ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
    }


    private SimpleExoPlayer setupPlayer(String url) {
        simpleExoPlayerView.setUseController(false);
        HttpProxyCacheServer proxy = VideoCacheProxyFactory.getProxy(getActivity());
        String proxyUrl = proxy.getProxyUrl(url);
        Log.d(LOG_TAG, "Use proxy url " + proxyUrl + " instead of original url " + url);
        final SimpleExoPlayer exoPlayer = newSimpleExoPlayer();
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(exoPlayer);
        MediaSource videoSource = newVideoSource(proxyUrl);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        exoPlayer.prepare(loopingSource);
        return exoPlayer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            getActivity().getSupportFragmentManager().beginTransaction().detach(this).remove(this).commit();
        }
        Log.e(LOG_TAG, "onCreateView for " + mTitle);
        View rootView = inflater.inflate(R.layout.recipe_detail, container, false);
        mContext = getActivity();
        RecyclerView recyclerView = ((RecyclerView) rootView.findViewById(R.id.ingredients_recyclerview));
        ConstraintLayout constraintLayout = (ConstraintLayout) rootView.findViewById(R.id.recipe_step_container);
        TextView textView = ((TextView) rootView.findViewById(R.id.recipe_step_detail));
        simpleExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.videoplayer);
        if (getArguments().getBoolean(ARG_ITEM_IS_STEP)) {
            if (mStep != null) {
                textView.setText(mStep.getDescription());
                recyclerView.setVisibility(View.INVISIBLE);
                constraintLayout.setVisibility(View.VISIBLE);
                simpleExoPlayerView.setVisibility(View.GONE);
                if (mStep.getVideoURL().contains("http")) {
                    simpleExoPlayer = setupPlayer(mStep.getVideoURL());
                    mediaIsh.setExtras(mTitle, simpleExoPlayer);
                    simpleExoPlayer.addListener(mediaIsh);
                    simpleExoPlayerView.setPlayer(simpleExoPlayer);
                    simpleExoPlayer.setPlayWhenReady(false);
                    simpleExoPlayerView.setVisibility(View.VISIBLE);
                    mValidPlayer = true;
                }
            }
        } else {

            recyclerView.setAdapter(new IngredientAdapter(mIngredients));
            recyclerView.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.INVISIBLE);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserVisibleHint();
        Log.e(LOG_TAG, "onStart for " + mTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mValidPlayer && getUserVisibleHint()) {
            simpleExoPlayer.setPlayWhenReady(true);
        } else if (mValidPlayer) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mValidPlayer) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }

    public void onSelected() {
        if (mValidPlayer) {
            simpleExoPlayer.setPlayWhenReady(true);
        }
    }

    public void onDeSelected() {
        if (mValidPlayer) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
        Log.e(LOG_TAG, "onDeSelected for " + mTitle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mValidPlayer) {
            simpleExoPlayer.release();
        }
    }

    public void setMediaIsh(MediaIsh mediaIsh) {
        this.mediaIsh = mediaIsh;
    }
}
