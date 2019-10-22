package com.wt.pinger.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.activity.MainActivity;
import com.wt.pinger.dialog.AddressDialog;
import com.wt.pinger.proto.AddressAdapter;
import com.wt.pinger.providers.DbContentProvider;



/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class AddressFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private LinearLayout adr_placeholder;
    private AddressAdapter adapter;

    public AddressFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_address, container, false);

        res.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddressDialog d = AddressDialog.newInstance(null);
            d.show(getFragmentManager(), "address_edit");
        });

        RecyclerView recycler = res.findViewById(R.id.recyclerAddress);
        adr_placeholder = res.findViewById(R.id.adr_placeholder);

        adapter = new AddressAdapter(getActivity());
        adapter.setOnItemClick(item -> {
            if (!((MainActivity) getActivity()).isSaveInstanceStateCalled()) {
                AddressDialog d = AddressDialog.newInstance(item);
                d.show(getFragmentManager(), "edit");
            } else {
                if (BuildConfig.DEBUG) {
                    Console.logw("Skip showing edit dialog after `onSaveInstanceState` is called");
                }
            }
        });

        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setAdapter(adapter);

        return res;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("address-fragment")
                        .putContentName("Address Fragment")
                        .putContentType("fragment")
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(2, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), DbContentProvider.URI_CONTENT, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adr_placeholder.setVisibility(data != null && data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
