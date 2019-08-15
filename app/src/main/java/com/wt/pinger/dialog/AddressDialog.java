package com.wt.pinger.dialog;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.kenumir.eventclip.EventClip;
import com.kenumir.eventclip.proto.EventParam;
import com.wt.pinger.R;
import com.wt.pinger.events.EventNames;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.DbContentProvider;
import com.wt.pinger.providers.data.AddressItem;

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

    private EditText editText1;
    private EditText editText0;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;

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
                .typeface(ResourcesCompat.getFont(getActivity(), R.font.medium), ResourcesCompat.getFont(getActivity(), R.font.regular))
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
                        try {
                            item.interval = Integer.parseInt(editText4.getText().toString());
                        } catch (Exception e) {
                            item.interval = null;
                        }
                        if (item._id == null) {
                            // insert
                            new AsyncQueryHandler(getActivity().getContentResolver()){}.
                                    startInsert(0, null, DbContentProvider.URI_CONTENT, item.toContentValues(true));
                            EventClip.deliver(new EventParam(EventNames.ADDRESS_ADDED));
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

            editText1 = dialog.getCustomView().findViewById(R.id.editText1);
            editText0 = dialog.getCustomView().findViewById(R.id.editText0);
            editText2 = dialog.getCustomView().findViewById(R.id.editText2);
            editText3 = dialog.getCustomView().findViewById(R.id.editText3);
            editText4 = dialog.getCustomView().findViewById(R.id.editText4);

            editText1.setText(item.addres);
            editText0.setText(item.display_name);
            editText2.setText(item.packet == null ? "" : item.packet.toString());
            editText3.setText(item.pings == null ? "" : item.pings.toString());
            editText4.setText(item.interval == null ? "" : item.interval.toString());
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("address-dialog")
                        .putContentName("Address Dialog")
                        .putContentType("dialog")
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (item != null) {
            item.saveToBundle(outState);
        }
        super.onSaveInstanceState(outState);
    }
}
