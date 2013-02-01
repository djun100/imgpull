package com.pioneer.setting;

import com.pioneer.k.PatternCompareFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：AppSetting.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-10-30 Create
 */

public class AppSetting
{
	public static final String	SETTING_FILE_NAME	= "setting";
	private Context				mContext;
	private SharedPreferences	mPreference;
	private static final String	KEY_INTERVAL		= "update_interval";
	private static final String	KEY_VIBRATE			= "vibrate";
	private static final String	KEY_PRICE_SOUND		= "price_volume";
	private static final String	KEY_THREE_POSITIVE	= PatternCompareFactory.CompareType.THREE_POSITIVE.name();
	private static final String	KEY_THREE_NEGTIVE	= PatternCompareFactory.CompareType.THREE_NEGTIVE.name();
	private static final String	KEY_FIVE_NEGTIVE	= PatternCompareFactory.CompareType.FIVE_NEGTIVE.name();
	private static final String	KEY_FIVE_POSITIVE	= PatternCompareFactory.CompareType.FIVE_POSITIVE.name();
	private static final String	KEY_STEP_DECREASE	= PatternCompareFactory.CompareType.STEP_DECREASE.name();
	private static final String	KEY_STEP_INCREASE	= PatternCompareFactory.CompareType.STEP_INCREASE.name();
	

	public AppSetting(Context c)
	{
		if (c != null)
		{
			mContext = c;
			mPreference = mContext.getSharedPreferences(SETTING_FILE_NAME, Context.MODE_PRIVATE);
		}
	}

	public void setUpdateInterval(int interval)
	{
		mPreference.edit().putInt(KEY_INTERVAL, interval).commit();
	}

	public int getUpdateInterval()
	{
		return mPreference.getInt(KEY_INTERVAL, 10000);
	}

	public void setEnableVibrate(boolean vibrate)
	{
		mPreference.edit().putBoolean(KEY_VIBRATE, vibrate).commit();
	}

	public boolean getEnableVibrate()
	{
		return mPreference.getBoolean(KEY_VIBRATE, true);
	}

	public void setEnablePriceSound(boolean sound)
	{
		mPreference.edit().putBoolean(KEY_PRICE_SOUND, sound).commit();
	}

	public boolean getEnablePriceSound()
	{
		return mPreference.getBoolean(KEY_PRICE_SOUND, true);
	}

	public void setThreePositive(boolean enable)
	{
		mPreference.edit().putBoolean(KEY_THREE_POSITIVE, enable).commit();
	}

	public boolean getThreePositive()
	{
		return mPreference.getBoolean(KEY_THREE_POSITIVE, true);
	}

	public void setThreeNegtive(boolean enable)
	{
		mPreference.edit().putBoolean(KEY_THREE_NEGTIVE, enable).commit();
	}

	public boolean getThreeNegtive()
	{
		return mPreference.getBoolean(KEY_THREE_NEGTIVE, true);
	}

	public void setFivePositive(boolean enable)
	{
		mPreference.edit().putBoolean(KEY_FIVE_POSITIVE, enable).commit();
	}

	public boolean getFivePositive()
	{
		return mPreference.getBoolean(KEY_FIVE_POSITIVE, true);
	}

	public void setFiveNegtive(boolean enable)
	{
		mPreference.edit().putBoolean(KEY_FIVE_NEGTIVE, enable).commit();
	}

	public boolean getFiveNegtive()
	{
		return mPreference.getBoolean(KEY_FIVE_NEGTIVE, true);
	}
	
	/**
	 * 通过参数查询分析方法是否启用，如果方法名称不存在默认返回true。
	 * @param analyseName 分析方法名称。
	 * @return 
	 */
	public boolean getPatternAnalyseSwitch(String analyseName)
	{
		if (TextUtils.isEmpty(analyseName))
		{
			return true;
		}
		
		return mPreference.getBoolean(analyseName, true);
	}
	
	/**
	 * 通过分析方法名称设置分析方法是否启用。
	 * @param analyseName 分析方法名称
	 * @param enable 启用状态
	 */
	public void setPatternAnalyseSwitch(String analyseName, boolean enable )
	{
		if (!TextUtils.isEmpty(analyseName))
		{
			mPreference.edit().putBoolean(analyseName, enable).commit();
		}
	}
}
