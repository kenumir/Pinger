package com.wt.pinger.dialog;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wt.pinger.R;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.DbContentProvider;
import com.wt.pinger.providers.data.AddressItem;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kenumir on 2016-09-01.
 *
 */
public class AddressDialog extends DialogFragment {

    public static AddressDialog newInstance(@Nullable AddressItem a) {
        AddressDialog d = new AddressDialog();
        Bundle args = new Bundle();
        if (a != null) {
            a.saveToBundle(args);
        }
        d.setArguments(args);
        return d;
    }

    @BindView(R.id.editText1) EditText editText1;
    @BindView(R.id.editText0) EditText editText0;
    @BindView(R.id.editText2) EditText editText2;
    @BindView(R.id.editText3) EditText editText3;

    private AddressItem item;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            item = ItemProto.fromBundle(savedInstanceState, AddressItem.class);
        } else {
            item = ItemProto.fromBundle(getArguments(), AddressItem.class);
        }
        if (item == null) {
            item = new AddressItem();
        }

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.label_entry)
                .autoDismiss(false)
                .customView(R.layout.dialog_address_form, true)
                .positiveText(R.string.label_ok)
                .negativeText(R.string.label_cancel)
                .neutralText(item._id != null ? R.string.label_delete : 0)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        item.addres = editText1.getText().toString().trim();
                        item.display_name = editText0.getText().toString();
                        if (item.addres.trim().length() == 0) {
                            editText1.requestFocus();
                            Toast.makeText(getActivity(), R.string.toast_enter_url, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            item.packet = Integer.parseInt(editText2.getText().toString());
                        } catch (Exception e) {
                            item.packet = null;
                        }
                        try {
                            item.pings = Integer.parseInt(editText3.getText().toString());
                        } catch (Exception e) {
                            item.pings = null;
                        }
                        if (item._id == null) {
                            // insert
                            new AsyncQueryHandler(getActivity().getContentResolver()){}.
                                    startInsert(0, null, DbContentProvider.URI_CONTENT, item.toContentValues(true));
                        } else {
                            // update
                            new AsyncQueryHandler(getActivity().getContentResolver()){}.
                                    startUpdate(0, null, DbContentProvider.URI_CONTENT, item.toContentValues(false), AddressItem.FIELD_ID + "=?", new String[]{item._id.toString()});
                        }
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (item._id != null) {
                            new AsyncQueryHandler(getActivity().getContentResolver()) {}
                                    .startDelete(0, null, DbContentProvider.URI_CONTENT, null, new String[]{item._id.toString()});
                        }
                        dialog.dismiss();
                    }
                })
                .build();

        if (dialog.getCustomView() != null) {
            ButterKnife.bind(this, dialog.getCustomView());

            editText1.setText(item.addres);
            editText0.setText(item.display_name);
            editText2.setText(item.packet == null ? "" : item.packet.toString());
            editText3.setText(item.pings == null ? "" : item.pings.toString());
        }

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (item != null) {
            item.saveToBundle(outState);
        }
        super.onSaveInstanceState(outState);
    }
}
