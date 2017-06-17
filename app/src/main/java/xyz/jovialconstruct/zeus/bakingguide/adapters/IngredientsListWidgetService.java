package xyz.jovialconstruct.zeus.bakingguide.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import xyz.jovialconstruct.zeus.bakingguide.R;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeProvider;


public class IngredientsListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new IngredientListRemoteViewsFactory(this.getApplicationContext());
    }
}

class IngredientListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Recipe.Ingredient[] mIngredients;

    IngredientListRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Cursor mCursor = mContext.getContentResolver().query(RecipeProvider.Recipes.CONTENT_URI, null, null, null, null);
        if (!(mCursor == null || mCursor.getCount() == 0)) {
            if (mCursor.moveToPosition(1)) {
                int id = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.ID));
                String ingredientsJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.INGREDIENTS_JSON));
                Gson gson = new Gson();
                mIngredients = gson.fromJson(ingredientsJson, Recipe.Ingredient[].class);
            }
            mCursor.close();
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (mIngredients == null) return 0;
        return mIngredients.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.ingredient_widget_item);
        views.setTextViewText(R.id.ingredients_widget_item_textview, mIngredients[position].getIngredient());
        views.setTextViewText(R.id.ingredients_widget_number_textview, String.valueOf(mIngredients[position].getQuantity()));
        views.setTextViewText(R.id.ingredients_widget_detail_textview, mIngredients[position].getMeasure());
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

