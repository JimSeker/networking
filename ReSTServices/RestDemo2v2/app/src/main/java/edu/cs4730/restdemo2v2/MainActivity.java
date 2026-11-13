package edu.cs4730.restdemo2v2;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import edu.cs4730.restdemo2v2.databinding.ActivityMainBinding;

/**
 * restDemo2 v2
 * All the networking code has been moved into the ViewModel, so that the MVC is better followed.
 * the main code here, is just about displaying the data (ie CRUD) operations, which don't care
 * about how the data is handled.
 */

public class MainActivity extends AppCompatActivity implements myDialogFragment.OnFragmentInteractionListener {

    DataViewModel mViewModel;
    ActivityMainBinding binding;
    myAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        mViewModel = new ViewModelProvider(this).get(DataViewModel.class);
        mViewModel.getData().observe(this, new Observer<ArrayList<myObj>>() {
            @Override
            public void onChanged(@Nullable ArrayList<myObj> data) {
                binding.activityMainSwipeRefreshLayout.setRefreshing(false);  //in case this was refresh.
                mAdapter.setData(data);  //set the data to the adapter, which will update the recyclerview.
            }
        });


        //set up the RecyclerView
        binding.list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.list.setItemAnimator(new DefaultItemAnimator());
        //set up the adapter, which is myAdapter, see the code.  set it initially to null
        mAdapter = new myAdapter(null, getApplicationContext());
        mAdapter.setOnItemClickListener(new myAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String mid, String mtitle, String mbody) {
                myDialogFragment myDialog = myDialogFragment.newInstance(true, Integer.parseInt(mid), mtitle, mbody);
                myDialog.show(getSupportFragmentManager(), null);
            }
        });
        //add the adapter to the recyclerview
        binding.list.setAdapter(mAdapter);

        //SwipeRefreshlayout setup.
        //setup some colors for the refresh circle.
        binding.activityMainSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        //now set up the swiperefrestlayout listener where the main work is done.
        binding.activityMainSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //where we call the refresher parts.
                mViewModel.query();
            }
        });

        //setup left/right swipes on the cardviews for a delete.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //likely allows to for animations?  or moving items in the view I think.
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //called when it has been animated off the screen.  So item is no longer showing.
                //use ItemtouchHelper.X to find the correct one.

                if (direction == ItemTouchHelper.RIGHT) {
                    //user wants to delet this entry.
                    int item = Integer.parseInt(((myAdapter.ViewHolder) viewHolder).viewBinding.tvId.getText().toString());
                    //run thread task to delete this data item.
                    mViewModel.delete(item);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.list);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the user clicked the add button, so start the dialog with for add.
                myDialogFragment myDialog = myDialogFragment.newInstance(false, -1, "", "");
                myDialog.show(getSupportFragmentManager(), null);
            }
        });
    }

    //fragment listener for update/add for myDialogFragment
    //then set up the data structure for either update or insert and call the asynctask to do the work.
    @Override
    public void onFragmentInteraction(Boolean update, int id, String title, String body) {
        //return from dialog, now save the data.
        // Log.v("listener", "id " + id + title + body);
        if (update) {
            mViewModel.update(id, title, body);
        } else {
            mViewModel.insert(title, body);
        }

    }

}