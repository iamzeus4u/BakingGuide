package xyz.jovialconstruct.zeus.bakingguide.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jovialconstruct.zeus.bakingguide.R;
import xyz.jovialconstruct.zeus.bakingguide.data.Recipe;

/**
 * Created by zeus on 31/05/2017.
 */

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientAdapterViewHolder> {
    private List<Recipe.Ingredient> mIngredients;

    public IngredientAdapter(List<Recipe.Ingredient> ingredients) {
        mIngredients = ingredients;
    }

    @Override
    public IngredientAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ingredient_recyclerview_item, parent, false);
        return new IngredientAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IngredientAdapterViewHolder holder, int position) {
        char c[] = mIngredients.get(position).getIngredient().toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        holder.mIngredientNameTextView.setText(new String(c));
        holder.mIngredientNumberTextView.setText(String.format("%s:", String.valueOf(position + 1)));
        holder.mIngredientDetailTextView.setText(String.format("%s %s", String.valueOf(mIngredients.get(position).getQuantity()), mIngredients.get(position).getMeasure()));
    }

    @Override
    public int getItemCount() {
        if (mIngredients != null) {
            return mIngredients.size();
        } else {
            return 0;
        }
    }

    class IngredientAdapterViewHolder extends RecyclerView.ViewHolder {//implements OnClickListener {
        @BindView(R.id.ingredients_recyclerview_item_textview)
        TextView mIngredientNameTextView;
        @BindView(R.id.ingredients_recyclerview_number_textview)
        TextView mIngredientNumberTextView;
        @BindView(R.id.ingredients_recyclerview_detail_textview)
        TextView mIngredientDetailTextView;

        IngredientAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
