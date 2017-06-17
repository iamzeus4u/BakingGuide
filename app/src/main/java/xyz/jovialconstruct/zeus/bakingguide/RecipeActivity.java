package xyz.jovialconstruct.zeus.bakingguide;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;
import xyz.jovialconstruct.zeus.bakingguide.utilities.MediaUtils;

import static xyz.jovialconstruct.zeus.bakingguide.MainActivity.RECIPE_ID;

public class RecipeActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String RECIPE_NAME = "recipe_name";
    @BindView(R.id.recipe_list)
    RecyclerView recyclerView;
    private boolean mTwoPane;
    private int mTwoPaneSelectedItem;
    private int mRecipeId;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecipeItemAdapter mRecipeItemAdapter;
    private MediaUtils mediaUtils;
    private MenuItem mFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intentThatStartedActivity = getIntent();
        if (intentThatStartedActivity.hasExtra(RECIPE_ID)) {
            mRecipeId = intentThatStartedActivity.getIntExtra(RECIPE_ID, 1);
        }
        Cursor mCursor = getContentResolver().query(RecipeProvider.Recipes.withId(mRecipeId), null, null, null, null);
        recyclerView.setHasFixedSize(true);
        mRecipeItemAdapter = new RecipeItemAdapter(this, mCursor);
        recyclerView.setAdapter(mRecipeItemAdapter);
        if (mCursor != null) {
            mCursor.close();
        }
        if (findViewById(R.id.recipe_detail_container) != null) {
            mTwoPane = true;
            mediaUtils = new MediaUtils(this, new MySessionCallback());
            mediaUtils.initializeMediaSession();
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (isFav()) {
            mFav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav1));
        } else {
            mFav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav0));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("recyclerview_position")) {
                mPosition = savedInstanceState.getInt("recyclerview_position");
            }
            if (savedInstanceState.containsKey("two_pane_item")) {
                mTwoPaneSelectedItem = savedInstanceState.getInt("two_pane_item");
            }
        }
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        recyclerView.smoothScrollToPosition(mPosition);
        if (mTwoPane) {
            mRecipeItemAdapter.selectItem(mTwoPaneSelectedItem);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTwoPane) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null && !fragments.isEmpty()) {
                for (Fragment fragment : fragments) {
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaUtils != null) {
            mediaUtils.release();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("recyclerview_position", mPosition);
        outState.putInt("recipe_id", mRecipeId);
        outState.putInt("two_pane_item", mTwoPaneSelectedItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recipe_activity, menu);
        mFav = menu.findItem(R.id.action_favorite);
        if (isFav()) {
            mFav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav1));
        } else {
            mFav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav0));
        }
        return true;
    }

    private boolean isFav() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int favouriteRecipeId = Integer.parseInt(prefs.getString(getString(R.string.pref_favourite_recipe_id_key), getString(R.string.pref_favourite_recipe_id_default)));
        if (favouriteRecipeId == mRecipeId) {
            Intent intent = new Intent(this, BakingIngredientsWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), BakingIngredientsWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            try {
                NavUtils.navigateUpFromSameTask(this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Intent startMainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(startMainActivityIntent);
            }
            return true;
        } else if (id == R.id.action_favorite) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(getString(R.string.pref_favourite_recipe_id_key), String.valueOf(mRecipeId)).apply();
        }
        return super.onOptionsItemSelected(item);
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
            if (mTwoPaneSelectedItem > 0) {
                mRecipeItemAdapter.selectItem(--mTwoPaneSelectedItem);
            }
        }

        @Override
        public void onSkipToNext() {
            if (mTwoPaneSelectedItem < mRecipeItemAdapter.getItemCount()) {
                mRecipeItemAdapter.selectItem(++mTwoPaneSelectedItem);
            }
        }
    }

    class RecipeItemAdapter extends RecyclerView.Adapter<RecipeItemAdapter.RecipeItemViewHolder> {

        private final Cursor mRecipeCursor;
        private final Context mContext;
        private final String mRecipeName;
        Gson gson = new Gson();
        private List<Recipe.Step> mSteps = new ArrayList<>();
        private List<Recipe.Ingredient> mIngredients = new ArrayList<>();

        RecipeItemAdapter(Context context, Cursor recipeCursor) {
            mContext = context;
            mRecipeCursor = recipeCursor;
            mRecipeCursor.moveToFirst();
            mRecipeName = mRecipeCursor.getString(mRecipeCursor.getColumnIndex(RecipeColumns.NAME));
            String stepJson = mRecipeCursor.getString(mRecipeCursor.getColumnIndex(RecipeColumns.STEPS_JSON));
            String ingredientJson = mRecipeCursor.getString(mRecipeCursor.getColumnIndex(RecipeColumns.INGREDIENTS_JSON));
            Recipe.Step[] steps = (gson.fromJson(stepJson, Recipe.Step[].class));
            Recipe.Ingredient[] ingredients = (gson.fromJson(ingredientJson, Recipe.Ingredient[].class));
            Collections.addAll(mSteps, steps);
            Collections.addAll(mIngredients, ingredients);
        }

        @Override
        public RecipeItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recipe_steps_recyclerview_item, parent, false);
            return new RecipeItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecipeItemViewHolder holder, int position) {
            if (position != 0) {
                final Recipe.Step mItem = mSteps.get(position - 1);
                holder.mTitleTextView.setText(mItem.getShortDescription());
                holder.mStepImageView.setVisibility(View.VISIBLE);
                if (mItem.getThumbnailURL() != null && !mItem.getThumbnailURL().isEmpty()) {
                    Picasso.with(mContext).load(mItem.getThumbnailURL()).error(R.drawable.recipe_placeholder).placeholder(R.drawable.recipe_placeholder).into(holder.mStepImageView);
                } else {
                    holder.mStepImageView.setImageResource(R.drawable.recipe_placeholder);
                }
            } else {

                holder.mStepImageView.setVisibility(View.INVISIBLE);
                holder.mTitleTextView.setText(R.string.recipe_ingredient_item_title);
            }
            final int tempPosition = position;
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(tempPosition);
                    mTwoPaneSelectedItem = tempPosition;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSteps.size() + 1;
        }

        void selectItem(int mSelectedPosition) {
            final String stepJson;
            final boolean isStep;

            if (mSelectedPosition != 0) {
                final Recipe.Step mItem = mSteps.get(mSelectedPosition - 1);
                stepJson = gson.toJson(mItem);
                isStep = true;
            } else {
                List<Recipe.Ingredient> mItem = mIngredients;
                stepJson = gson.toJson(mItem);
                isStep = false;
            }

            if (mTwoPane) {
                mediaUtils.setRecipeName(mRecipeName);
                Bundle arguments = new Bundle();
                arguments.putBoolean(RecipeDetailFragment.ARG_ITEM_IS_STEP, isStep);
                arguments.putString(RecipeDetailFragment.ARG_ITEM_JSON, stepJson);
                RecipeDetailFragment fragment = new RecipeDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.recipe_detail_container, fragment)
                        .commit();
            } else {
                Intent intent = new Intent(mContext, RecipeDetailActivity.class);
                intent.putExtra(RECIPE_NAME, mRecipeName);
                intent.putExtra(RecipeDetailFragment.ARG_ITEM_IS_STEP, isStep);
                intent.putExtra(RecipeDetailFragment.ARG_ITEM_JSON, stepJson);
                intent.putExtra(RecipeDetailActivity.INTENT_ITEM_POSITION, mSelectedPosition);
                intent.putExtra(RECIPE_ID, mRecipeId);
                mContext.startActivity(intent);
            }
        }

        class RecipeItemViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final ImageView mStepImageView;
            final TextView mTitleTextView;

            RecipeItemViewHolder(View view) {
                super(view);
                mView = view;
                mTitleTextView = (TextView) view.findViewById(R.id.title);
                mStepImageView = (ImageView) view.findViewById(R.id.step_image_imageView);
            }

        }
    }

}
