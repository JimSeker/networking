package edu.cs4730.tcpdemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.cs4730.tcpdemo.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    FragmentMainBinding binding;
    String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);

        binding.helpClient.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_client, null));
        binding.helpServer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_server, null));

        return binding.getRoot();

    }

    //A simple method to append data to the logger textview.
    public void logthis(String msg) {
        binding.logger.append(msg + "\n");
        Log.d(TAG, msg);
    }
}