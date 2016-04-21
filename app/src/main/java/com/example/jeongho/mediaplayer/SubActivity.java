package com.example.jeongho.mediaplayer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SubActivity extends AppCompatActivity {

    public class ItemsAdapter extends ArrayAdapter<Item> {
        public ItemsAdapter(Context context, ArrayList<Item> items) {
            super(context, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.string_item, parent, false);
            }

            TextView tvType = (TextView) convertView.findViewById(R.id.itemType);
            TextView tvName = (TextView) convertView.findViewById(R.id.itemName);

            tvType.setText(item.type);
            tvName.setText(item.name);

            return convertView;
        }
    }

    ArrayList<Item> items = new ArrayList<>();
    ItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        items.add(new Item("Type1", "Name1"));
        items.add(new Item("Type2", "Name2"));
        adapter = new ItemsAdapter(this, items);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
}
