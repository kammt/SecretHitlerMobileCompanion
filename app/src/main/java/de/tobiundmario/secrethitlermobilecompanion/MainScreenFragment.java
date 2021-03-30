package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.EventCardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.PlayerCardRecyclerViewAdapter;


public class MainScreenFragment extends Fragment {


    public MainScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createGame = view.findViewById(R.id.btn_createGame);
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).replaceFragment(MainActivity.page_setup, true);
            }
        });

        final Button openRules = view.findViewById(R.id.btn_rules_external);
        openRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRulesWebsite();
            }
        });

        setupLayout(view);
    }

    private void openRulesWebsite() {
        String url = "https://secrethitler.io/rules";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void setupLayout(View v) {
        View includeView = v.findViewById(R.id.gameFragmentLayout);
        includeView.setAlpha(0.4f);

        v.findViewById(R.id.clickBlockade).setVisibility(View.VISIBLE);

        //First, hide the bottomSheets
        v.findViewById(R.id.bottom_sheet_add_event).setVisibility(View.GONE);
        v.findViewById(R.id.bottom_sheet_server_status).setVisibility(View.GONE);
        v.findViewById(R.id.bottom_sheet_game_status).setVisibility(View.GONE);

        setupDemoRecyclerViews(v);
    }

    private void setupDemoRecyclerViews(View v) {
        RecyclerView playerList = v.findViewById(R.id.playerList);
        playerList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        playerList.setAdapter(PlayerCardRecyclerViewAdapter.generateDemoAdapter(getContext()));

        RecyclerView cardList = v.findViewById(R.id.cardList);
        cardList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        cardList.setAdapter(EventCardRecyclerViewAdapter.generateDemoAdapter(getContext()));
    }
}