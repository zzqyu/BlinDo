package com.blindo.app;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class GestureActivity extends ActionBarActivity implements View.OnTouchListener, View.OnLongClickListener {
	private GestureDetector mGestureDetector = new GestureDetector(new CustomGesture());
	private String TAG = "GestureActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(TAG);
	}
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
	public boolean onLongClick(View v) {
		return false;
	}
	public void whenSwipeLeft() {
		Log.i("whenSwipeLeft", "");
	}
	public void whenSwipeRight() {
		Log.i("whenSwipeRight", "");
	}
	public void whenSwipeUp() {
		Log.i("whenSwipeUp", "");

	}
	public void whenSwipeDown() {
		Log.i("whenSwipeDown", "");

	}
	public void whenLongPressed(){
		Log.i("whenLongPressed", "");
	}
	public void whenDoubleTap(){
		Log.i("whenDoubleTap", "");
	}

	private class CustomGesture extends GestureDetector.SimpleOnGestureListener {
		private int dpWidth;
		private int dpHeight;
		private int swipe_Min_Distance = 200;
		private int swipe_Max_Distance_X = 1000;
		private int swipe_Max_Distance_Y = 1000;
		private int swipe_Min_Velocity = 50;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		                       float velocityY) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
			dpWidth = dm.widthPixels;
			dpHeight = dm.heightPixels;
			swipe_Min_Distance = (int) (dpWidth * 5 / 39);
			swipe_Max_Distance_X = (int) (dpWidth * 4 / 9);
			swipe_Max_Distance_Y = (int) (dpHeight * 13 * 48);
			final float xDistance = Math.abs(e1.getX() - e2.getX());
			final float yDistance = Math.abs(e1.getY() - e2.getY());

			if(xDistance > this.swipe_Max_Distance_X || yDistance > this.swipe_Max_Distance_Y)
				return false;

			velocityX = Math.abs(velocityX);
			velocityY = Math.abs(velocityY);
			boolean result = false;

			if(velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance){
				if(e1.getX() > e2.getX()) {// right to left
					vibe.vibrate(35);
					whenSwipeLeft();
				}

				else {
					vibe.vibrate(35);
					whenSwipeRight();
				}
				result = true;
			}
			else if(velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance){
				if(e1.getY() > e2.getY()) { // bottom to up
					vibe.vibrate(35);
					whenSwipeUp();
				}
				else {
					vibe.vibrate(35);
					whenSwipeDown();
				}
				result = true;
			}

			return result;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(35);
			whenDoubleTap();
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(35);
			whenLongPressed();
		}

	}
}
