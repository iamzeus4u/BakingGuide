package xyz.jovialconstruct.zeus.bakingguide;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;
import xyz.jovialconstruct.zeus.bakingguide.utilities.MediaUtils;

import static xyz.jovialconstruct.zeus.bakingguide.R.id.fab;
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
    private MediaUtils mediaUtils;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(fab);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            mPosition = getIntent().getIntExtra(INTENT_ITEM_POSITION, 0);
            mRecipeName = getIntent().getStringExtra(RECIPE_NAME);
            mId = getIntent().getIntExtra(MainActivity.RECIPE_ID, 0);
            mCursor = getContentResolver().query(RecipeProvider.Recipes.withId(mId), null, null, null, null);
        }
        mediaUtils = new MediaUtils(this, new MySessionCallback());
        mediaUtils.initializeMediaSession();
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.recipe_detail_container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.recipe_detail_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    public MediaUtils getMediaUtils() {
        return mediaUtils;
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mediaUtils.getmExoPlayer().setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mediaUtils.getmExoPlayer().setPlayWhenReady(false);
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
        mediaUtils.setRecipeName(mRecipeName);
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
            Intent backIntent = new Intent(this, RecipeActivity.class);
            backIntent.putExtra(MainActivity.RECIPE_ID, mId);
            NavUtils.navigateUpTo(this, backIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareText(String shareText, String title) {
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(shareText)
                .setChooserTitle(String.format(getString(R.string.sharing_string), title))
                .startChooser();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
        Gson gson = new Gson();
        private List<Recipe.Step> mSteps = new ArrayList<>();
        private List<Recipe.Ingredient> mIngredients = new ArrayList<>();
        RecipeDetailFragment[] mFragment;

        SectionsPagerAdapter(FragmentManager fm) {
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
            return newInstance(position + 1);
        }

        @Override
        public int getCount() {

            return mSteps.size() + 1;
        }

        RecipeDetailFragment newInstance(int stepNumber) {
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
                    return String.format("%s %S", getString(R.string.step_string), position);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(final int position) {
            Activity activity = RecipeDetailActivity.this;
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            ImageView imageView = (ImageView) activity.findViewById(R.id.recipe_thumbnail_image_view);
            if (appBarLayout != null) {
                if (position > 0) {
                    imageView.setVisibility(View.VISIBLE);
                    appBarLayout.setTitle(mSteps.get(position - 1).getShortDescription());
                    if (mSteps.get(position - 1).getThumbnailURL() != null && !mSteps.get(position - 1).getThumbnailURL().isEmpty()) {
                        Picasso.with(activity).load(mSteps.get(position - 1).getThumbnailURL()).error(R.drawable.recipe_placeholder).placeholder(R.drawable.recipe_placeholder).into(imageView);
                    }
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String text = mSteps.get(position - 1).getDescription() + "\n" + mSteps.get(position - 1).getVideoURL();
                            shareText(text, mSteps.get(position - 1).getShortDescription());
                        }
                    });
                } else {
                    appBarLayout.setTitle(getString(R.string.recipe_ingredient_item_title));
                    imageView.setVisibility(View.INVISIBLE);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < mIngredients.size(); i++) {
                                builder.append(mIngredients.get(i).getQuantity()).append(mIngredients.get(i).getMeasure()).append(" ").append(mIngredients.get(i).getIngredient()).append("\n");
                            }
                            String text = builder.deleteCharAt(builder.length() - 1).toString();
                            shareText(text, getString(R.string.recipe_ingredient_item_title));
                        }
                    });
                }
            }
            if (mFragment[position] != null) {
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
