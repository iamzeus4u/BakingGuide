package xyz.jovialconstruct.zeus.bakingguide.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jovialconstruct.zeus.bakingguide.R;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;
import xyz.jovialconstruct.zeus.bakingguide.data.RecipeColumns;

/**
 * Created by zeus on 31/05/2017.
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterViewHolder> {
    private final RecipeAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;
    private Context context;
    public RecipeAdapter(RecipeAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public RecipeAdapter.RecipeAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recipe_recyclerview_item, parent, false);
        return new RecipeAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeAdapter.RecipeAdapterViewHolder holder, int position) {
        if (mCursor.moveToPosition(position)) {
            String name = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.NAME));
            String image = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.IMAGE));
            int servings = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.SERVINGS));
            String stepsJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.STEPS_JSON));
            String ingredientsJson = mCursor.getString(mCursor.getColumnIndex(RecipeColumns.INGREDIENTS_JSON));
            Gson gson = new Gson();
            Resources resources = context.getResources();
            Recipe.Step[] steps = gson.fromJson(stepsJson, Recipe.Step[].class);
            Recipe.Ingredient[] ingredients = gson.fromJson(ingredientsJson, Recipe.Ingredient[].class);
            int numberOfSteps = steps.length;
            int numberOfIngredients = ingredients.length;
            if (!image.isEmpty()) {
                Picasso.with(context).load(image).error(R.drawable.recipe_placeholder).placeholder(R.drawable.recipe_placeholder).into(holder.mRecipeImageView);
            } else {
                holder.mRecipeImageView.setImageResource(R.drawable.recipe_placeholder);
            }
            holder.mRecipeNameTextView.setText(name);
            holder.mRecipeServingTextView.setText(resources.getQuantityString(R.plurals.servings, servings, servings));
            holder.mRecipeDescriptionTextView.setText(String.format("%s %s", resources.getQuantityString(R.plurals.ingredients, numberOfIngredients, numberOfIngredients), resources.getQuantityString(R.plurals.steps, numberOfSteps, numberOfSteps)));
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void swapCursor(Cursor data) {
        mCursor = data;
        notifyDataSetChanged();        ;
    }

    public interface RecipeAdapterOnClickHandler {
        void onClick(int selectedRecipeId);
    }

    public class RecipeAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        @BindView(R.id.recipe_image_imageView)
        ImageView mRecipeImageView;
        @BindView(R.id.recipe_name_textView)
        TextView mRecipeNameTextView;
        @BindView(R.id.recipe_servings_textView)
        TextView mRecipeServingTextView;
        @BindView(R.id.recipe_description_textView)
        TextView mRecipeDescriptionTextView;

        public RecipeAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (mCursor.moveToPosition(adapterPosition)) {
                int id = mCursor.getInt(mCursor.getColumnIndex(RecipeColumns.ID));
                mClickHandler.onClick(id);
            }
        }
    }
}
