package xyz.jovialconstruct.zeus.bakingguide.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import xyz.jovialconstruct.zeus.bakingguide.MainActivity;
import xyz.jovialconstruct.zeus.bakingguide.R;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;


public class StepListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StepListRemoteViewsFactory(this.getApplicationContext());
    }
}

class StepListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;

    StepListRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        mCursor = mContext.getContentResolver().query(RecipeProvider.Recipes.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        int id = 0;
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.step_widget_item);
        if (mCursor == null || mCursor.getCount() == 0) return null;
        if (mCursor.moveToPosition(position)) {
            id = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.ID));
            String name = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.NAME));
            int servings = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.SERVINGS));
            String stepsJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.STEPS_JSON));
            String ingredientsJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.INGREDIENTS_JSON));
            Gson gson = new Gson();
            Resources resources = mContext.getResources();
            Recipe.Step[] steps = gson.fromJson(stepsJson, Recipe.Step[].class);
            Recipe.Ingredient[] ingredients = gson.fromJson(ingredientsJson, Recipe.Ingredient[].class);
            int numberOfSteps = steps.length;
            int numberOfIngredients = ingredients.length;
            views.setTextViewText(R.id.recipe_name_textView, name);
            views.setTextViewText(R.id.recipe_servings_textView, resources.getQuantityString(R.plurals.servings, servings, servings));
            views.setTextViewText(R.id.recipe_description_textView, String.format("%s %s", resources.getQuantityString(R.plurals.ingredients, numberOfIngredients, numberOfIngredients), resources.getQuantityString(R.plurals.steps, numberOfSteps, numberOfSteps)));
        }
        Bundle extras = new Bundle();
        extras.putInt(MainActivity.RECIPE_ID, id);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.recipe_recyclerview_item_container, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the GridView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

