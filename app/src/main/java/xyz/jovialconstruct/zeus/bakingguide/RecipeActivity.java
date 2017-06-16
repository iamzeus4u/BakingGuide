package xyz.jovialconstruct.zeus.bakingguide;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;
import xyz.jovialconstruct.zeus.bakingguide.utilities.MediaIsh;

import static xyz.jovialconstruct.zeus.bakingguide.MainActivity.RECIPE_ID;

/**
 * An activity representing a list of Recipes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link RecipeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class RecipeActivity extends AppCompatActivity {
    public static final String RECIPE_NAME = "recipe_name";
    private boolean mTwoPane;
    private Cursor mCursor;
    private int mTwoPaneSelectedItem;
    private int mRecipeId;
    private int mPosition = RecyclerView.NO_POSITION;
    @BindView(R.id.recipe_list)
    RecyclerView recyclerView;
    private RecipeItemAdapter mRecipeItemAdapter;
    private MediaIsh mediaIsh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intentThatStartedActivity = getIntent();
        if (intentThatStartedActivity.hasExtra(RECIPE_ID)) {
            mRecipeId = intentThatStartedActivity.getIntExtra(RECIPE_ID, 1);
        }
        mCursor = getContentResolver().query(RecipeProvider.Recipes.withId(mRecipeId), null, null, null, null);
        recyclerView.setHasFixedSize(true);
        mRecipeItemAdapter = new RecipeItemAdapter(this, mCursor);
        recyclerView.setAdapter(mRecipeItemAdapter);

        mediaIsh = new MediaIsh(this, new MySessionCallback());

        if (findViewById(R.id.recipe_detail_container) != null) {
            mTwoPane = true;
            mediaIsh.initializeMediaSession();
        }
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
    protected void onDestroy() {
        super.onDestroy();
        mediaIsh.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("recyclerview_position", mPosition);
        outState.putInt("recipe_id", mRecipeId);
        outState.putInt("two_pane_item", mTwoPaneSelectedItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            try {
                NavUtils.navigateUpFromSameTask(this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                Intent startMainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(startMainActivityIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class RecipeItemAdapter extends RecyclerView.Adapter<RecipeItemAdapter.RecipeItemViewHolder> {

        private final Cursor mRecipeCursor;
        private final Context mContext;
        private final String mRecipeName;
        Gson gson = new Gson();
        private List<Recipe.Step> mSteps = new ArrayList<>();
        private List<Recipe.Ingredient> mIngredients = new ArrayList<>();

        public RecipeItemAdapter(Context context, Cursor recipeCursor) {
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
        public void onBindViewHolder(final RecipeItemViewHolder holder, final int position) {
            if (position != 0) {
                final Recipe.Step mItem = mSteps.get(position - 1);
                holder.mTitleTextView.setText(mItem.getShortDescription());
            } else {
                List<Recipe.Ingredient> mItem = mIngredients;
                holder.mTitleTextView.setText(R.string.recipe_ingredient_item_title);
                int numberOfIngredients = mItem.size();
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(position);
                    mTwoPaneSelectedItem = position;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSteps.size() + 1;
        }

        public void selectItem(int mSelectedPosition) {
            final String stepJson;
            final boolean isStep;
            mediaIsh.setRecipeName(mRecipeName);

            if (mSelectedPosition != 0) {
                final Recipe.Step mItem = mSteps.get(mSelectedPosition - 1);
                stepJson = gson.toJson(mItem);
                isStep = true;
                ;
            } else {
                List<Recipe.Ingredient> mItem = mIngredients;
                stepJson = gson.toJson(mItem);
                isStep = false;
                ;
            }

            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putBoolean(RecipeDetailFragment.ARG_ITEM_IS_STEP, isStep);
                arguments.putString(RecipeDetailFragment.ARG_ITEM_JSON, stepJson);
                RecipeDetailFragment fragment = new RecipeDetailFragment();
                fragment.setArguments(arguments);
                fragment.setMediaIsh(mediaIsh);
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

        public class RecipeItemViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleTextView;
            //public final TextView mContentTextView;

            public RecipeItemViewHolder(View view) {
                super(view);
                mView = view;
                mTitleTextView = (TextView) view.findViewById(R.id.title);
                //mContentTextView = (TextView) view.findViewById(R.id.content);
            }

        }
    }

}
