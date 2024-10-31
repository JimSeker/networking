package edu.cs4730.tcpdemo_kt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.createNavigateOnClickListener
import edu.cs4730.tcpdemo_kt.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private var TAG: String = "MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.helpClient.setOnClickListener(
            createNavigateOnClickListener(
                R.id.action_help_to_client, null
            )
        )
        binding.helpServer.setOnClickListener(
            createNavigateOnClickListener(
                R.id.action_help_to_server, null
            )
        )

        return binding.root
    }

    //A simple method to append data to the logger textview.
    fun logthis(msg: String) {
        binding.logger.append(msg + "\n")
        Log.d(TAG, msg)
    }
}