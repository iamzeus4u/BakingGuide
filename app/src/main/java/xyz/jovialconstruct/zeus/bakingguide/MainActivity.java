package xyz.jovialconstruct.zeus.bakingguide;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jovialconstruct.zeus.bakingguide.IdlingResource.SimpleIdlingResource;
import xyz.jovialconstruct.zeus.bakingguide.adapters.RecipeAdapter;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;
import xyz.jovialconstruct.zeus.bakingguide.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.RecipeAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String RECIPE_ID = "recipe_id";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RECIPE_LOADER_ID = 0;
    @BindView(R.id.recipe_recycler_view)
    RecyclerView mRecipeRecyclerView;
    @BindView(R.id.main_activity_progressbar)
    ProgressBar mMainActivityProgressBar;
    private RecipeAdapter mRecipeAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    // The Idling Resource which will be null in production.
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    /**
     * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPxls = metrics.widthPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPxls / scaleFactor;

        if (widthDp > 600) {
            GridLayoutManager recipeLayoutManager = new GridLayoutManager(this, Math.round(widthDp / 300), GridLayoutManager.VERTICAL, false);
            mRecipeRecyclerView.setLayoutManager(recipeLayoutManager);
        } else {
            LinearLayoutManager recipeLayoutManager = new LinearLayoutManager(this, GridLayoutManager.VERTICAL, false);
            mRecipeRecyclerView.setLayoutManager(recipeLayoutManager);
        }
        mRecipeRecyclerView.setHasFixedSize(true);
        mRecipeAdapter = new RecipeAdapter(this);
        mRecipeRecyclerView.setAdapter(mRecipeAdapter);
        getSupportLoaderManager().initLoader(RECIPE_LOADER_ID, null, MainActivity.this);
        getIdlingResource();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(int selectedRecipeId) {
        Intent intentToStartRecipeActivity = new Intent(this, RecipeActivity.class);
        intentToStartRecipeActivity.putExtra(RECIPE_ID, selectedRecipeId);
        startActivity(intentToStartRecipeActivity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (true) {
            return new AsyncTaskLoader<Cursor>(MainActivity.this) {
                Cursor cursor = null;

                @Override
                protected void onStartLoading() {
                    if (mIdlingResource != null) {
                        mIdlingResource.setIdleState(false);
                    }
                    if (cursor != null) {
                        deliverResult(cursor);
                    } else {
                        mMainActivityProgressBar.setVisibility(View.VISIBLE);
                        mRecipeRecyclerView.setVisibility(View.INVISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        String recipesJson = NetworkUtils.getResponseFromHttpUrl();
                        Gson gson = new Gson();
                        Recipe[] recipes = gson.fromJson(recipesJson, Recipe[].class);

                        for (Recipe recipe : recipes) {
                            try {
                                getContentResolver().delete(RecipeProvider.Recipes.withId(recipe.getId()), null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(RecipeColumns.ID, recipe.getId());
                            mContentValues.put(RecipeColumns.NAME, recipe.getName());
                            mContentValues.put(RecipeColumns.IMAGE, recipe.getImage());
                            mContentValues.put(RecipeColumns.SERVINGS, recipe.getServings());
                            mContentValues.put(RecipeColumns.INGREDIENTS_JSON, gson.toJson(recipe.getIngredients()));
                            mContentValues.put(RecipeColumns.STEPS_JSON, gson.toJson(recipe.getSteps()));
                            getContentResolver().insert(RecipeProvider.Recipes.CONTENT_URI, mContentValues);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        return getContentResolver().query(RecipeProvider.Recipes.CONTENT_URI, null, null, null, null);
                    }
                }

                public void deliverResult(Cursor data) {
                    cursor = data;
                    super.deliverResult(data);
                }
            };
        } else {
            return new CursorLoader(MainActivity.this, RecipeProvider.Recipes.CONTENT_URI, null, null, null, null);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecipeAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecipeRecyclerView.smoothScrollToPosition(mPosition);
        mMainActivityProgressBar.setVisibility(View.INVISIBLE);
        mRecipeRecyclerView.setVisibility(View.VISIBLE);
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
