package jp.ac.tohoku.qse.takahashi.discussions.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class ServerAdapter extends ArrayAdapter<String> {

	
	private boolean mode=true;
	private LayoutInflater mInflater;
    private String[] mStrings;
    private int mViewResourceId;

    public ServerAdapter(Context ctx, int viewResourceId,String[] strings) {
        super(ctx, viewResourceId, strings);

        mInflater = (LayoutInflater)ctx.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mStrings = strings;
        mViewResourceId = viewResourceId;
    }

    @Override
    public int getCount() {
        return mStrings.length;
    }

    @Override
    public String getItem(int position) {
        return mStrings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mViewResourceId, null);

        convertView.setMinimumHeight(62);
        TextView tv = (TextView)convertView.findViewById(android.R.id.text1); //Give Id to your textview
        if(tv!=null)
        {
	        tv.setText(mStrings[position]);
	        
	        if(this.mode==true)
	        	tv.setTextColor(Color.BLACK);
	        else
	        	tv.setTextColor(Color.GRAY);
        }
        return convertView;
    }
    
    public void setMode(boolean mode) {
		this.mode = mode;
	}
}
