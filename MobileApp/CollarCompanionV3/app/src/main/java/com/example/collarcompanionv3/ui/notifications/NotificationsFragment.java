package com.example.collarcompanionv3.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.collarcompanionv3.AboutUsActivity;
import com.example.collarcompanionv3.Activity2;
import com.example.collarcompanionv3.AddAPetActivity;
import com.example.collarcompanionv3.ConnectActivity;
import com.example.collarcompanionv3.DogInfoActivity;
import com.example.collarcompanionv3.MainActivity;
import com.example.collarcompanionv3.NotificationsActivity;
import com.example.collarcompanionv3.R;
import com.example.collarcompanionv3.UserInfoActivity;
import com.example.collarcompanionv3.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {
    ListView lv;
    SearchView searchView;
    ArrayAdapter<String> adapter;
    String[] data = {"Connect A Device","User Information", "Dog Information", "Add A Pet", "Notifications", "About Us", "Log Out","FUCCCCCCKKKKKK"};

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        lv = (ListView) view.findViewById(R.id.idListView);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getContext(), "clicked item at position: "+position, Toast.LENGTH_SHORT).show();
                openActivity(position);
            }
        });
        return view;
    }

    public void openActivity(int position){
        if (position==5){
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        }
        else if (position==4){
            Intent intent = new Intent(getActivity(), NotificationsActivity.class);
            startActivity(intent);
        }
        else if (position==3){
            Intent intent = new Intent(getActivity(), AddAPetActivity.class);
            startActivity(intent);
        }
        else if (position==2){
            Intent intent = new Intent(getActivity(), DogInfoActivity.class);
            startActivity(intent);
        }
        else if (position==1){
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            startActivity(intent);
        }
        else if (position==0){
            Intent intent = new Intent(getActivity(), ConnectActivity.class);
            startActivity(intent);
        }
        else if (position==6){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }

        else{
            Toast.makeText(getContext(), "no page made yet!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}