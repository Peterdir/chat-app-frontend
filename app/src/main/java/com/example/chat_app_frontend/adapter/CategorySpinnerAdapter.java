package com.example.chat_app_frontend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.chat_app_frontend.R;
import com.example.chat_app_frontend.model.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    private LayoutInflater inflater;

    public CategorySpinnerAdapter(Context context, List<Category> categories) {
        super(context, R.layout.item_category_spinner, categories);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category_spinner, parent, false);
        }

        Category category = getItem(position);
        if (category != null) {
            TextView tvCategoryName = convertView.findViewById(R.id.tv_category_name);
            tvCategoryName.setText(category.getName());
        }

        return convertView;
    }
}
