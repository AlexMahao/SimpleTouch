package com.spearbothy.touch.core.parser;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.spearbothy.touch.core.R;
import com.spearbothy.touch.core.Touch;
import com.spearbothy.touch.core.print.FilePrint;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mahao
 */

public class FileListActivity extends AppCompatActivity implements OnItemClickListener {

    private List<String> mFiles = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_touch_activity_file_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerDecoration(recyclerView, getResources().getColor(R.color.simple_touch_divider_special), 1, 0, 0, 0));
        mAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        makeData();
    }

    private void makeData() {
        String absFileDir = Environment.getExternalStorageDirectory() + File.separator + FilePrint.DIR + File.separator + Touch.sHostPackage;
        File file = new File(absFileDir);
        if (!file.exists()) {
            Toast.makeText(this, "请查看" + absFileDir + "是否有文件", Toast.LENGTH_SHORT).show();
            return;
        }
        for (File f : file.listFiles()) {
            mFiles.add(f.getAbsolutePath());
        }
        Collections.reverse(mFiles);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        startActivity(ChartActivity.getIntent(this, mFiles.get(position)));
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mLayoutInflater;

        private RecyclerViewAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.simple_touch_item_file_list, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.desc.setText(mFiles.get(position));
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView desc;
        private TextView hasParameter;
        private TextView className;

        ViewHolder(View view) {
            super(view);
            desc = (TextView) view.findViewById(R.id.desc);
        }
    }
}
