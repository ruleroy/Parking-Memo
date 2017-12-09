package com.vannakittikun.parkingmemo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Rule on 12/7/2017.
 */

public class MainLandscapeFragment extends Fragment {

    MyDBHandler myDBHandler;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_landscape, container, false);

        myDBHandler = new MyDBHandler(getActivity());

        ArrayList<String> addresses = myDBHandler.getAllParkingAddress();
        Collections.reverse(addresses);

        ListAdapter listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, addresses);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

        return view;
    }

}
