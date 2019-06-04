package com.spearbothy.touch.core.parser;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spearbothy.touch.core.R;
import com.spearbothy.touch.core.recycler.JsonRecyclerView;

/**
 * @author mahao
 */
public class JsonViewerFragment extends Fragment {

    public static JsonViewerFragment newInstance(String json) {
        Bundle args = new Bundle();
        args.putString("data", json);
        JsonViewerFragment fragment = new JsonViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String json = getArguments().getString("data");
        JsonRecyclerView view = (JsonRecyclerView) inflater.inflate(R.layout.simple_touch_fragment_json_viewer, container, false);
        view.bindJson(json);
//        view.setScaleEnable(true);
        return view;
    }
}
