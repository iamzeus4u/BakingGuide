package xyz.jovialconstruct.zeus.bakingguide;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;
import xyz.jovialconstruct.zeus.bakingguide.utilities.MediaIsh;

import static xyz.jovialconstruct.zeus.bakingguide.RecipeActivity.RECIPE_NAME;

/**
 * An activity representing a single Recipe detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link RecipeActivity}.
 */
public class RecipeDetailActivity extends AppCompatActivity {
    public static final String INTENT_ITEM_POSITION = "selected_item";
    private int mPosition;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Cursor mCursor;
    private int mId;
    private String mRecipeName;
    private MediaIsh mediaIsh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            mPosition = getIntent().getIntExtra(INTENT_ITEM_POSITION, 0);
            mRecipeName = getIntent().getStringExtra(RECIPE_NAME);
            mId = getIntent().getIntExtra(MainActivity.RECIPE_ID, 0);
            mCursor = getContentResolver().query(RecipeProvider.Recipes.withId(mId), null, null, null, null);

            /*Bundle arguments = new Bundle();
            arguments.putString(RecipeDetailFragment.ARG_ITEM_JSON,
                    getIntent().getStringExtra(RecipeDetailFragment.ARG_ITEM_JSON));
            arguments.putBoolean(RecipeDetailFragment.ARG_ITEM_IS_STEP,
                    getIntent().getBooleanExtra(RecipeDetailFragment.ARG_ITEM_IS_STEP, true));
            RecipeDetailFragment fragment = new RecipeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.mId.recipe_detail_container, fragment)
                    .commit();*/
        }
        mediaIsh = new MediaIsh(this, new MySessionCallback());
        mediaIsh.initializeMediaSession();
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.recipe_detail_container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.recipe_detail_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mediaIsh.getmExoPlayer().setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mediaIsh.getmExoPlayer().setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            if (mPosition > 0) {
                mViewPager.setCurrentItem(--mPosition);
                mSectionsPagerAdapter.onPageSelected(mPosition);
            }
        }

        @Override
        public void onSkipToNext() {
            if (mPosition < mSectionsPagerAdapter.getCount()) {
                mViewPager.setCurrentItem(++mPosition);
                mSectionsPagerAdapter.onPageSelected(mPosition);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(mPosition);
        mSectionsPagerAdapter.onPageSelected(mPosition);
        mediaIsh.setRecipeName(mRecipeName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mPosition")) {
                mPosition = savedInstanceState.getInt("mPosition");
            }
            if (savedInstanceState.containsKey("mRecipeName")) {
                mRecipeName = savedInstanceState.getString("mRecipeName");
            }
            if (savedInstanceState.containsKey("mId")) {
                mId = savedInstanceState.getInt("mId");
                mCursor = getContentResolver().query(RecipeProvider.Recipes.withId(mId), null, null, null, null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mPosition", mPosition);
        outState.putString("mRecipeName", mRecipeName);
        outState.putInt("mId", mId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Intent backIntent = new Intent(this, RecipeActivity.class);
            backIntent.putExtra(MainActivity.RECIPE_ID, mId);
            NavUtils.navigateUpTo(this, backIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
        Gson gson = new Gson();
        private List<Recipe.Step> mSteps = new ArrayList<>();
        private List<Recipe.Ingredient> mIngredients = new ArrayList<>();
        RecipeDetailFragment[] mFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mCursor.moveToFirst();
            String stepJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.STEPS_JSON));
            String ingredientJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.INGREDIENTS_JSON));
            Recipe.Step[] steps = (gson.fromJson(stepJson, Recipe.Step[].class));
            Recipe.Ingredient[] ingredients = (gson.fromJson(ingredientJson, Recipe.Ingredient[].class));
            Collections.addAll(mSteps, steps);
            Collections.addAll(mIngredients, ingredients);
            mFragment = new RecipeDetailFragment[mSteps.size() + 1];
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return newInstance(position + 1);
        }

        @Override
        public int getCount() {

            return mSteps.size() + 1;
        }

        public RecipeDetailFragment newInstance(int stepNumber) {
            final String stepJson;
            final boolean isStep;
            if (stepNumber > 1) {
                final Recipe.Step mItem = mSteps.get(stepNumber - 2);
                stepJson = gson.toJson(mItem);
                isStep = true;
            } else {
                List<Recipe.Ingredient> mItem = mIngredients;
                stepJson = gson.toJson(mItem);
                isStep = false;
            }

            Bundle arguments = new Bundle();
            arguments.putString(RecipeDetailFragment.ARG_ITEM_JSON, stepJson);
            arguments.putBoolean(RecipeDetailFragment.ARG_ITEM_IS_STEP, isStep);
            RecipeDetailFragment fragment = new RecipeDetailFragment();
            fragment.setArguments(arguments);
            fragment.setMediaIsh(mediaIsh);
            mFragment[stepNumber - 1] = fragment;
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return getString(R.string.recipe_ingredient_item_title);
                case 1:
                    return mSteps.get(position - 1).getShortDescription();
                default:
                    return String.format("%s %S", "Step", position);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Activity activity = RecipeDetailActivity.this;
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if (position > 0) {
                    appBarLayout.setTitle(mSteps.get(position - 1).getShortDescription());
                } else {
                    appBarLayout.setTitle(getString(R.string.recipe_ingredient_item_title));
                }
            }
            if (mFragment[position] != null) {
                int length = mFragment.length - 1;
                mFragment[position].onSelected();
                if (position == 0) {
                    if (mFragment[position + 1] != null) {
                        mFragment[position + 1].onDeSelected();
                    }
                } else if (position == mFragment.length - 1) {
                    if (mFragment[position - 1] != null) {
                        mFragment[position - 1].onDeSelected();
                    }
                } else {
                    if (mFragment[position - 1] != null) {
                        mFragment[position - 1].onDeSelected();
                    }
                    if (mFragment[position + 1] != null) {
                        mFragment[position + 1].onDeSelected();
                    }
                }
            }
            mPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
