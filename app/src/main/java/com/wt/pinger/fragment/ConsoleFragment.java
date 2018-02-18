package com.wt.pinger.fragment;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.squareup.otto.Subscribe;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.R2;
import com.wt.pinger.proto.SimpleQueryHandler;
import com.wt.pinger.providers.CmdContentProvider;
import com.wt.pinger.providers.DbContentProvider;
import com.wt.pinger.service.CmdService;
import com.wt.pinger.utils.BusProvider;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class ConsoleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	@BindView(R2.id.cmd_edit) EditText edit;
	@BindView(R2.id.cmd_list) ListView list;
	@BindView(R2.id.cmd_placeholder) LinearLayout placeholder;
	@BindView(R2.id.cmdBtn) ImageView cmdBtn;

	private interface OnSelectCommand{
		void onResult(Cursor cursor);
	}

	private static class SelectCommandItems {

		private ContentResolver mContentResolver;
		private OnSelectCommand mOnSelectCommand;

		SelectCommandItems(Context cr) {
			mContentResolver = cr.getContentResolver();

		}
		SelectCommandItems callback(OnSelectCommand c) {
			mOnSelectCommand = c;
			return this;
		}
		SelectCommandItems execute() {
			new AsyncQueryHandler(mContentResolver){
				@Override
				protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
					if (mOnSelectCommand != null) {
						mOnSelectCommand.onResult(cursor);
					} else {
						cursor.close();
					}
				}
			}.startQuery(0, null, DbContentProvider.URI_CONTENT_COMMANDS, null, null, null, null);
			return this;
		}
	}

	@OnClick(R2.id.cmdSelectBtn) void cmdSelectBtnClick(View v) {

		new SelectCommandItems(getActivity()).callback(new OnSelectCommand() {
			@Override
			public void onResult(Cursor cursor) {
				final ArrayList<Long> commandsIdList = new ArrayList<>();
				CharSequence[] commands = new CharSequence[]{};
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						commands = new CharSequence[cursor.getCount()];
						int col1 = cursor.getColumnIndex(DbContentProvider.Commands.FIELD_COMMAND_TEXT);
						int col2 = cursor.getColumnIndex(DbContentProvider.Commands.FIELD_COMMAND_ID);
						int counter = 0;
						do {
							commands[counter] = cursor.getString(col1);
							commandsIdList.add(cursor.getLong(col2));
							counter++;
						} while (cursor.moveToNext());
					}
					cursor.close();
				}

				if (!isAdded()) {
					// skip show dialog window, activity is gone
					// query may take long time - activity may be closed
					return;
				}

				new MaterialDialog.Builder(getActivity())
						.title(R.string.label_list_of_commands)
						.items(commands)
						.autoDismiss(false)
						.typeface(ResourcesCompat.getFont(getActivity(), R.font.medium), ResourcesCompat.getFont(getActivity(), R.font.regular))
						.itemsCallback(new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
								dialog.dismiss();
								edit.setText(text);
							}
						})
						.itemsLongCallback(new MaterialDialog.ListLongCallback() {
							@Override
							public boolean onLongSelection(final MaterialDialog dialog, View itemView, final int position, CharSequence text) {
								if (isAdded()) {
									String posString;
									try {
										posString = Long.toString(commandsIdList.get(position));
									} catch (Exception e) {
										posString = "0";
									}
									new AsyncQueryHandler(getActivity().getContentResolver()){
										@Override
										protected void onDeleteComplete(int token, Object cookie, int result) {
											try {
												commandsIdList.remove(position);
											} catch (Exception e) {
												// ignore
											}
											if (isAdded() && dialog.isShowing()) {
												new SelectCommandItems(getActivity()).callback(new OnSelectCommand(){
													@Override
													public void onResult(Cursor cursor) {
														CharSequence[] cc = new CharSequence[]{};
														if (cursor.moveToFirst()) {
															int counter = 0;
															int col1 = cursor.getColumnIndex(DbContentProvider.Commands.FIELD_COMMAND_TEXT);
															cc = new CharSequence[cursor.getCount()];
															do {
																cc[counter] = cursor.getString(col1);
																counter++;
															} while (cursor.moveToNext());
														}
														cursor.close();
														if (isAdded() && dialog.isShowing()) {
															dialog.setItems(cc);
															Toast.makeText(getActivity(), R.string.toast_command_deleted, Toast.LENGTH_SHORT).show();
														}
													}
												}).execute();
											}
										}
									}.startDelete(0, null, DbContentProvider.URI_CONTENT_COMMANDS, DbContentProvider.Commands.FIELD_COMMAND_ID + "=?", new String[]{posString});
								}
								return true;
							}
						})
						.neutralText(R.string.label_restore_defaults)
						.onNeutral(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
								new AsyncQueryHandler(getActivity().getContentResolver()){
									@Override
									protected void onInsertComplete(int token, Object cookie, Uri uri) {
										if (isAdded() && dialog.isShowing()) {
											dialog.setItems(DbContentProvider.DEFAULT_COMMAND_LIST);
											commandsIdList.clear();
											new SelectCommandItems(getActivity()).callback(new OnSelectCommand(){
												@Override
												public void onResult(Cursor cursor) {
													if (cursor.moveToFirst()) {
														int col1 = cursor.getColumnIndex(DbContentProvider.Commands.FIELD_COMMAND_ID);
														do {
															commandsIdList.add(cursor.getLong(col1));
														} while (cursor.moveToNext());
													}
													cursor.close();
												}
											}).execute();
										}
									}
								}.startInsert(0, null, DbContentProvider.URI_CONTENT_COMMANDS_RESET, null);
							}
						})
						.positiveText(R.string.label_close)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								dialog.dismiss();
							}
						})
						.build()
						.show();
			}
		}).execute();
	}

	@OnClick(R2.id.cmdAddBtn) void cmdAddBtnClick(View v) {
		String text = edit.getText().toString();
		if (text.length() > 0) {
			final Context ctx = getActivity().getApplicationContext();
			ContentValues values = new ContentValues();
			values.put(DbContentProvider.Commands.FIELD_COMMAND_TEXT, text);
			new AsyncQueryHandler(ctx.getContentResolver()) {
				@Override
				protected void onInsertComplete(int token, Object cookie, Uri uri) {
					Toast.makeText(ctx, R.string.toast_command_added, Toast.LENGTH_SHORT).show();
				}
			}.startInsert(0, null, DbContentProvider.URI_CONTENT_COMMANDS, values);
		}
	}

	private SimpleCursorAdapter adapter;

	public ConsoleFragment() {}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View res = inflater.inflate(R.layout.fragment_console, container, false);
		ButterKnife.bind(this, res);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_cmd, null, new String[]{"data"}, new int[]{R.id.cmd_item}, 0);
		list.setAdapter(adapter);
		cmdBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				list.setSelectionAfterHeaderView();
				cmdBtn.setImageResource(R.drawable.ic_clear_black_24dp);
				CmdService.executeCmd(getActivity(), edit.getText().toString());
			}
		});
		edit.setText("");
		TextKeyListener.clear(edit.getText());
		return res;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		menu.add(R.string.label_share).setIcon(R.drawable.ic_share_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				if (getActivity() != null) { // <- fox NPE on getActivity()
					SimpleQueryHandler qh = new SimpleQueryHandler(getActivity().getContentResolver(), new SimpleQueryHandler.QueryListener() {
						@SuppressLint("StaticFieldLeak")
						@Override
						public void onQueryComplete(int token, Object cookie, Cursor cursor) {
							if (cursor != null) {
								new AsyncTask<Cursor, Void, String>() {
									@Override
									protected String doInBackground(Cursor... params) {
										StringBuilder sb = new StringBuilder();
										Cursor cursor = params[0];
										final int maxShareSize = 250 * 1024;
										if (cursor.moveToFirst()) {
											do {
												sb.append(cursor.getString(cursor.getColumnIndex("data")));
												sb.append("\n");

												int len = sb.length();
												if (len > maxShareSize) {
													// trim
													sb.setLength(maxShareSize);
													ERA.log("Share length trim from " + len);
													ERA.logException(new Exception("Share length trim from " + len));
													break;
												}
											} while (cursor.moveToNext());
										}
										cursor.close();
										return sb.toString();
									}

									@Override
									protected void onPostExecute(String s) {
										if (s.length() > 0) {
											try {
												Intent sendIntent = new Intent();
												sendIntent.setAction(Intent.ACTION_SEND);
												sendIntent.putExtra(Intent.EXTRA_TEXT, s);
												sendIntent.setType("text/plain");
												startActivity(sendIntent);
											} catch (ActivityNotFoundException e) {
												ERA.logException(e);
											}
										} else {
											if (getActivity() != null) {
												Toast.makeText(getActivity(), R.string.toast_no_data_to_share, Toast.LENGTH_LONG).show();
											}
										}
									}
								}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursor);
							} else {
								if (getActivity() != null) {
									Toast.makeText(getActivity(), R.string.toast_no_data_to_share, Toast.LENGTH_LONG).show();
								}
							}
						}
					});
					qh.startQuery(0, null, CmdContentProvider.URI_CONTENT, null, null, null, null);
				}
				return false;
			}
		}).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(1, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Answers.getInstance().logContentView(
				new ContentViewEvent()
						.putContentId("console-fragment")
						.putContentName("Console Fragment")
						.putContentType("fragment")
		);
		BusProvider.getInstance().register(this);
		CmdService.checkService(getActivity());
	}

	@Override
	public void onPause() {
		BusProvider.getInstance().unregister(this);
		super.onPause();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), CmdContentProvider.URI_CONTENT, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		placeholder.setVisibility(data != null && data.getCount() > 0 ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}


	@Subscribe @SuppressWarnings("unused")
	public void serviceMessages(CmdService.CmdServiceMessage msg) {
		if (BuildConfig.DEBUG) {
			Console.logi("serviceMessages " + msg.type + ", data=" + msg.data);
		}
		switch(msg.type) {
			case CmdService.CMD_MSG_CHECK:
				boolean isWorking = msg.getDataAsBool(false);
				cmdBtn.setImageResource(!isWorking ? R.drawable.ic_send_black_24dp : R.drawable.ic_clear_black_24dp);
				break;
		}
	}
}
