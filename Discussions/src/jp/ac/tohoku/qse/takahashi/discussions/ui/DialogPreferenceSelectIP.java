package jp.ac.tohoku.qse.takahashi.discussions.ui;

import java.util.TreeMap;

import org.odata4j.expression.OrderByExpression;

import jp.ac.tohoku.qse.takahashi.discussions.R;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.ui.adapter.ServerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Custom preference dialog with text field and listview.
 * @author Alexander Bigel
 */
public class DialogPreferenceSelectIP extends DialogPreference implements OnCheckedChangeListener{

	private EditText editTextSelectIP;
	private CheckBox checkBoxEnterIP;
	private ListView listViewServers;
	private ServerAdapter adapter;
	
	private TreeMap<String,String> mapAddress=new TreeMap<String, String>();
	private TreeMap<String,String> mapServerNames=new TreeMap<String, String>();
	
	public DialogPreferenceSelectIP(Context context, AttributeSet attrs) {
		super(context,attrs);
		setDialogLayoutResource(R.layout.dialog_preference_selectip);
	}
	
	public DialogPreferenceSelectIP(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.dialog_preference_selectip);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		
		mapAddress.put(getContext().getString(R.string.server_name_local),
				getContext().getString(R.string.local_server_address));
		mapAddress.put(getContext().getString(R.string.server_name_public),
				getContext().getString(R.string.public_server_address));
		mapAddress.put(getContext().getString(R.string.server_name_offline),
				getContext().getString(R.string.offline_server_address));
		mapAddress.put(getContext().getString(R.string.server_name_development),
				getContext().getString(R.string.development_server_address));
		
		mapServerNames.put(getContext().getString(R.string.local_server_address),
				getContext().getString(R.string.server_name_local));
		mapServerNames.put(getContext().getString(R.string.public_server_address),
				getContext().getString(R.string.server_name_public));
		mapServerNames.put(getContext().getString(R.string.offline_server_address),
				getContext().getString(R.string.server_name_offline));
		mapServerNames.put(getContext().getString(R.string.development_server_address),
				getContext().getString(R.string.server_name_development));
		
		
		checkBoxEnterIP=(CheckBox)view.findViewById(R.id.dialog_checkBoxEnterIP);
		editTextSelectIP=(EditText)view.findViewById(R.id.dialog_editText_selectIP);
		listViewServers=(ListView)view.findViewById(R.id.dialog_listViewSelectSerevr);
		
		editTextSelectIP.setText(PreferenceHelper.getServerAddress(getContext()));
		
		checkBoxEnterIP.setChecked(PreferenceHelper.isTypedServerAddressFlag(getContext()));
		checkBoxEnterIP.setOnCheckedChangeListener(this);
		
		String[] names=getContext().getResources().getStringArray(R.array.server_names);
		adapter=new ServerAdapter(getContext(),android.R.layout.simple_list_item_single_choice,names);
		listViewServers.setAdapter(adapter);

		
		if(checkBoxEnterIP.isChecked())
		{
			editTextSelectIP.setEnabled(true);
			listViewServers.setEnabled(false);
			if(adapter!=null)
				adapter.setMode(false);
		}
		else
		{
			editTextSelectIP.setEnabled(false);
			listViewServers.setEnabled(true);
			if(adapter!=null)
				adapter.setMode(true);
			
			selectListItem();
		}
		
		
		super.onBindDialogView(view);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		
		if(!positiveResult)
			return;
		else
		{
			PreferenceHelper.setTypedServerAddressFlag(getContext(), checkBoxEnterIP.isChecked());
			
			if(checkBoxEnterIP.isChecked())
			{
				//edit text
				PreferenceHelper.setServerAddress(getContext(), editTextSelectIP.getText().toString());
				PreferenceHelper.setTypedServerAddressFlag(getContext(), true);
			}
			else
			{
				//list
				String address=getSelectedListServer();
				if(address!=null)
				{
					PreferenceHelper.setServerAddress(getContext(), address);
					PreferenceHelper.setTypedServerAddressFlag(getContext(), false);
					
				}
			}
		}
		
		super.onDialogClosed(positiveResult);
	}

	/**
	 * Select server in listview on dialog
	 */
	private void selectListItem()
	{
		String address=PreferenceHelper.getServerAddress(getContext());
		String[] names=getContext().getResources().getStringArray(R.array.server_names);
		
		String serverName=mapServerNames.get(address);
		boolean select=false;
		
		for(int i=0;i<names.length;i++)
		{
			if(names[i].equals(serverName))
			{
				listViewServers.setSelection(i);
				listViewServers.setItemChecked(i, true);
				select=true;
			}
		}
		
		//select local server by defauult
		if(!select)
		{
			listViewServers.setSelection(0);
			listViewServers.setItemChecked(0, true);
		}
		
	}
	
	/**
	 * Return selected server IP from server list
	 * @return
	 */
	private String getSelectedListServer()
	{
		//int position=listViewServers.getSelectedItemPosition();
		int position=listViewServers.getCheckedItemPosition();
		if(0<=position)
		{
			String[] names=getContext().getResources().getStringArray(R.array.server_names);
			String serverName=names[position];
			String serverAddress=mapAddress.get(serverName);
			
			return serverAddress;
		}
		
		return null;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if(buttonView==checkBoxEnterIP)
		{
			if(isChecked)
			{
				editTextSelectIP.setEnabled(true);
				listViewServers.setEnabled(false);
				if(adapter!=null)
				{
					adapter.setMode(false);
					adapter.notifyDataSetChanged();
				}
			}
			else
			{
				editTextSelectIP.setEnabled(false);
				listViewServers.setEnabled(true);
				selectListItem();
				if(adapter!=null)
				{
					adapter.setMode(true);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
	
}
