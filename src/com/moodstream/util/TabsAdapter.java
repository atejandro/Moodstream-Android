package com.moodstream.util;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * The TabsAdapter Class implements the methods of The ActionBar.TabListener 
 * and ViewPager.OnPageChangeListener to coordinate the tabs with the swipe
 * gesture of the ViewPager
 * */
public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener , ViewPager.OnPageChangeListener{

	private final Context mContext;
	private final ActionBar mActionBar;//Reference to the ActionBar
	private final ViewPager mViewPager;//Reference to the ViewPager
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();//List of current tabs
	private final String TAG = "TabsAdapter:";
	
	
	static final class TabInfo{
		private final Class<?> clss;//Templated Class
		private final Bundle args;//Saved Instance State
		
		TabInfo(Class<?> _class, Bundle _args){
			clss = _class;
			args = _args;
		}
	}
	
	
	/**
	 * TabsAdapter Constructor
	 * */
	public TabsAdapter(SherlockFragmentActivity fa, ViewPager pager) {
		super(fa.getSupportFragmentManager());
		mContext =fa.getBaseContext(); 
		mActionBar = fa.getSupportActionBar();
		mViewPager = pager;
		mViewPager.setAdapter(this);
		mViewPager.setOnPageChangeListener(this);
	}
	
	
	/**
	 * The addTab method allows to modify the mTabs list making it dynamic
	 * */
	public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args){
		TabInfo info = new TabInfo(clss, args);
		tab.setTag(info);
		tab.setTabListener(this);
		mTabs.add(info);
		mActionBar.addTab(tab);
		notifyDataSetChanged();
	}

	
	//************** ViewPager Listener Methods  ***************//
	
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	
	//When a new Page is selected, we have to update the ActionBar Tab to match a consistent flow
	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
	}
	
	
	//************** ActionBar TabListener Methods  ***************//

	//When a Tab is selected we have to update the ViewPager to match a consistent flow
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		//Set the current Fragment depending on the tab position
		mViewPager.setCurrentItem(tab.getPosition());
		Log.v(TAG, "clicked");
		Object tag = tab.getTag();
		for (int i = 0; i<mTabs.size(); i++){
			if (mTabs.get(i) == tag){
				mViewPager.setCurrentItem(i);
			}
		}
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// Nothing to do. Can make a Toast
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		//Nothing to do
	}

	
	//Retrieves the Fragment selected
	@Override
	public Fragment getItem(int position) {
		//Get the Specific Tab Fragment
		TabInfo info=mTabs.get(position);
		//Return the tab in Fragment form
		return Fragment.instantiate(mContext, info.clss.getName(),info.args);
	}

	@Override
	public int getCount() {
		//Returns the amount of tabs
		return mTabs.size();
	}

}
