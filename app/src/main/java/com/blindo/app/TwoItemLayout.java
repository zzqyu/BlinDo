package com.blindo.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
public class TwoItemLayout extends LinearLayout{
	LinearLayout ll1;
	TextView[] txt = new TextView[2];

	String[] pageElement = new String[2];
	Context ctxt;

	public TwoItemLayout(Context context){
		super(context);
		ctxt = context;
		init();
	}
	public TwoItemLayout(Context context, AttributeSet attrs){
		super(context, attrs);
		ctxt = context;

	}
	public void setThisPage(String[] pageElement){
		this.pageElement = pageElement;
		init();
	}

	private void init(){
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infService);

		addView(inflater.inflate(R.layout.two_item_layout, null));

		ll1 = (LinearLayout) findViewById(R.id.ll1);
		int[] txtId = {R.id.txt1, R.id.txt2};
		for(int i = 0; i<txtId.length ; i++) {
			txt[i] = (TextView) findViewById(txtId[i]);
			txt[i].setText(pageElement[i]);
		}

	}

}
