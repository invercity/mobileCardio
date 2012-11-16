package ua.stu.view.fragments;

import ua.stu.view.scpview.PatientInfo;
import ua.stu.view.scpview.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


@TargetApi(11)
public class InfoFragment extends Fragment implements OnItemClickListener  {
	
	private static String TAG = "InfoFragment";
	
	private ListView lvMain;
	
	private String[] values = new String[] {"Пациент","Прочее"};
	
	public interface OnEventItemClickListener 
	{
	    public void itemClickEvent(int position);
	}
	
	OnEventItemClickListener onEventItemClick;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.info, null);
		
	    lvMain = (ListView) view.findViewById(R.id.lvMain);
	    lvMain.setOnItemClickListener(this);

	    //create adapter
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(),
        android.R.layout.simple_list_item_1, values);

	    //add adapter list
	    lvMain.setAdapter(adapter);
		
		return view;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	onEventItemClick = (OnEventItemClickListener) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		try {
        	onEventItemClick = (OnEventItemClickListener) view.getContext();
        } catch (ClassCastException e) {
        	throw new ClassCastException(view.getContext().toString() + " must implement onSomeEventListener");
        }
		onEventItemClick.itemClickEvent(position);
	}
}
