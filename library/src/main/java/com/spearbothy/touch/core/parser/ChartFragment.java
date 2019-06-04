package com.spearbothy.touch.core.parser;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spearbothy.touch.core.Constants;
import com.spearbothy.touch.core.R;
import com.spearbothy.touch.core.parser.entity.ChartBlock;
import com.spearbothy.touch.core.parser.entity.ChartFlow;
import com.spearbothy.touch.core.parser.entity.ChartGroup;
import com.spearbothy.touch.core.print.JsonFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author mahao
 */
public class ChartFragment extends Fragment {

    private JsonFactory.JsonPrintEntity mEntityList;

    private int position;

    private Comparator<ChartBlock> mBlockComparator = new Comparator<ChartBlock>() {
        @Override
        public int compare(ChartBlock o1, ChartBlock o2) {
            if (o1.getTitle().equals(Constants.ON_TOUCH_EVENT) && (o2.getTitle().equals(Constants.DISPATCH_TOUCH_EVENT) || o2.getTitle().equals(Constants.ON_INTERCEPT_TOUCH_EVENT))) {
                return -1;
            }

            if (o1.getTitle().equals(Constants.DISPATCH_TOUCH_EVENT) && o2.getTitle().equals(Constants.ON_INTERCEPT_TOUCH_EVENT)) {
                return -1;
            }
            return 1;
        }
    };
    private ChartView mChartView;


    public static ChartFragment newInstance(String json) {
        Bundle args = new Bundle();
        args.putString("data", json);
        ChartFragment fragment = new ChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        String json = getArguments().getString("data");
        mEntityList = JsonFactory.toEntity(json);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.simple_touch_fragment_chart, container, false);

        for (JsonFactory.JsonPrintEntity.TouchView touchView : mEntityList) {
            List<ChartGroup> groups = new ArrayList<>();
            position = 0;
            addChartGroup(groups, touchView);
            ChartView chartView = new ChartView(getContext());
            chartView.setData(groups);
            view.addView(chartView);
        }
        return view;
    }

    private void addChartGroup(List<ChartGroup> groups, JsonFactory.JsonPrintEntity.TouchView view) {
        ChartGroup group = null;

        for (ChartGroup temp : groups) {
            if (temp.getToken() == view.getViewToken()) {
                group = temp;
                break;
            }
        }

        if (group == null) {
            group = new ChartGroup();
            group.setTitle(view.getClassName());
            group.setDesc(view.getAbsClassName() + " " + view.getId());
            group.setToken(view.getViewToken());
            groups.add(group);
        }

        for (Object object : view.getCalls()) {
            if (object instanceof JSONObject) {
                JSONObject jo = (JSONObject) object;
                if (jo.containsKey("methodName")) {
                    JsonFactory.JsonPrintEntity.TouchMethod method = JSON.parseObject(jo.toJSONString(), JsonFactory.JsonPrintEntity.TouchMethod.class);
                    position++;
                    addBlock(position, group, method);
                } else if (jo.containsKey("className")) {
                    addChartGroup(groups, JSON.parseObject(jo.toJSONString(), JsonFactory.JsonPrintEntity.TouchView.class));
                }
            }
        }
    }

    private void addBlock(int position, ChartGroup group, JsonFactory.JsonPrintEntity.TouchMethod method) {
        ChartBlock block = null;
        for (ChartBlock temp : group.getBlocks()) {
            if (temp.getTitle().equals(method.getMethodName())) {
                block = temp;
                break;
            }
        }

        if (block == null) {
            block = new ChartBlock();
            block.setTitle(method.getMethodName());
            block.setDesc(method.getEvent());
            if (method.getMethodName().equals(Constants.DISPATCH_TOUCH_EVENT)) {
                block.setHorizontal(ChartBlock.Horizontal.CENTER);
            } else if (method.getMethodName().equals(Constants.ON_TOUCH_EVENT)) {
                block.setHorizontal(ChartBlock.Horizontal.LEFT);
            } else if (method.getMethodName().equals(Constants.ON_INTERCEPT_TOUCH_EVENT)) {
                block.setHorizontal(ChartBlock.Horizontal.RIGHT);
            }
            group.getBlocks().add(block);
        }

        ChartFlow flow = new ChartFlow();
        flow.setPosition(position);
        if (method.getResult() != null) {
            flow.setDesc(String.valueOf(method.getResult()));
        }
        if (method.getMethodName().equals(Constants.DISPATCH_TOUCH_EVENT)) {
            if (method.getDirection().equals(JsonFactory.ENTER)) {
                flow.setDirection(ChartFlow.Direction.TOP_ENTER);
            } else {
                flow.setDirection(ChartFlow.Direction.TOP_EXIT);
            }
        } else if (method.getMethodName().equals(Constants.ON_INTERCEPT_TOUCH_EVENT)) {
            if (method.getDirection().equals(JsonFactory.ENTER)) {
                flow.setDirection(ChartFlow.Direction.LEFT_ENTER);
            } else {
                flow.setDirection(ChartFlow.Direction.LEFT_EXIT);
            }
        } else if (method.getMethodName().equals(Constants.ON_TOUCH_EVENT)) {
            if (method.getDirection().equals(JsonFactory.ENTER)) {
                flow.setDirection(ChartFlow.Direction.RIGHT_ENTER);
            } else {
                flow.setDirection(ChartFlow.Direction.RIGHT_EXIT);
            }
        }
        block.getFlows().add(flow);
    }
}
