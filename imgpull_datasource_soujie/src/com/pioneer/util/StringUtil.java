package com.pioneer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.json.JSONObject;

import android.text.TextUtils;

import com.pioneer.constant.AppInt;
import com.pioneer.constant.AppString;
import com.pioneer.engine.AppEngine;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：StringUtil.java
 * Description：
 * History：
 * 1.0 Denverhan 2012-9-17 Create
 */

public class StringUtil
{
	// 存放上一次的引用时间
	private static long				LAST_QUOTE_TIME	= 0L;

	private static final String[]	KEY_NAME		= new String[] { AppString.STRING_MIDDLE_CURRENT, AppString.STRING_BANK_BUY,
			AppString.STRING_BANK_SELL, AppString.STRING_MIDDLE_MAX, AppString.STRING_MIDDLE_MIN };


	/**
	 * 获得当前时间。
	 * 
	 * @return
	 */
	public static String getCurrentTime()
	{
		return new SimpleDateFormat("HH_mm_ss").format(new Date());
	}


	public static HashMap<String, String> getJsonMap(String text)
	{
		HashMap<String, String> hash = new HashMap<String, String>();
		if (TextUtils.isEmpty(text))
		{
			return hash;
		}

		try
		{
			if (text.startsWith(AppString.BRACkET_LEFT))
			{
				text = text.substring(1);
			}

			if (text.endsWith(AppString.BRACkET_RIGHT))
			{
				text = text.substring(0, text.length() - 1);
			}

			JSONObject json = new JSONObject(text);
			for (String name : AppString.KEY_NAMES)
			{
				// 对最新价格进行约束处理。
				final String string = json.getString(name);
				if (name.equalsIgnoreCase(AppString.KEY_NAME_LAST))
				{
					final int value = Integer.parseInt(StringUtil.removeDot(string));
					hash.put(name, Integer.toString(StringUtil.getRound(value)));
				}
				else
				{
					hash.put(name, string);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return hash;
	}


	/**
	 * 解析键值对。
	 * 
	 * @param text
	 * @return
	 */
	public static HashMap<String, String> getValues(String text)
	{
		HashMap<String, String> hash = new HashMap<String, String>();
		if (TextUtils.isEmpty(text))
		{
			return hash;
		}

		StringTokenizer mainTokens = new StringTokenizer(text, AppString.SPLITER);

		while (mainTokens.hasMoreTokens())
		{
			String token = mainTokens.nextToken();

			if (!TextUtils.isEmpty(token))
			{
				if (token.toLowerCase().contains(AppString.STRING_TIME))
				{
					StringTokenizer time = new StringTokenizer(token, AppString.STRING_EQUALS);
					if (time.countTokens() == 2)
					{
						time.nextToken();
						hash.put(AppString.STRING_TIME, time.nextToken());
					}
				}
				else if (token.toLowerCase().contains(AppString.STRING_GOLD))
				{

					StringTokenizer time = new StringTokenizer(token, AppString.STRING_EQUALS);
					if (time.countTokens() == 2)
					{
						time.nextToken();
						StringTokenizer priceTokens = new StringTokenizer(time.nextToken(), AppString.STRING_PRICE_SPLITER);

						int i = 0;
						while (priceTokens.hasMoreTokens() && i < KEY_NAME.length)
						{
							String price = priceTokens.nextToken();

							if (!TextUtils.isEmpty(price))
							{

								hash.put(KEY_NAME[i], price);
								i++;
							}
						}
					}
				}
			}
		}

		return hash;
	}

	public static String getString(int resId)
	{
		return AppEngine.getInstance().getContext().getResources().getString(resId);
	}

	/**
	 * 去掉小数后面的字符串。
	 * 
	 * @return
	 */
	public static String removeDot(String text)
	{
		final String dot = ".";
		int pos = -1;

		if (text != null && (pos = text.indexOf(dot)) != -1)
		{
			text = text.substring(0, pos);
		}

		return text;
	}

	/**
	 * 尾数改为5的倍数。
	 * 
	 * @param value
	 * @return
	 */
	public static int getRound(int value)
	{
		int result = value;
		int remain = 0;
		int lowNumber = 0;
		if (value < 0)
		{
			return result;
		}
		lowNumber = value % 10;

		// 清理个位数
		value -= lowNumber;

		if (lowNumber >= 3 && lowNumber <= 7)
		{
			remain = 5;
		}
		else if (lowNumber > 7)
		{
			remain = 10;
		}

		return value + remain;
	}

	/**
	 * 将秒表示的时间转换为人类可以读时间
	 * 
	 * @param sec 单位为秒
	 * @return
	 */
	public static String seconds2Time(long sec)
	{
		String result = new SimpleDateFormat("HH:mm:ss").format(new Date(sec * 1000L));

		return result;
	}

	/**
	 * 将形如“18:36:04”的时间转换为秒表示法。
	 * 
	 * @param time
	 * @return
	 */
	public static long time2Seconds(String time)
	{
		// 秒表示法的本次时间
		long result = 0L;

		// 上一次时间和本次时间的差值，单位为秒
		long diff = 0L;

		int hour = 0;
		int min = 0;
		int seconds = 0;

		final Calendar instance = Calendar.getInstance();
		StringTokenizer mainTokens = new StringTokenizer(time, AppString.COLON);
		try
		{
			if (mainTokens.hasMoreTokens())
			{
				hour = Integer.parseInt(mainTokens.nextToken());
			}
			if (mainTokens.hasMoreTokens())
			{
				min = Integer.parseInt(mainTokens.nextToken());
			}
			if (mainTokens.hasMoreTokens())
			{
				seconds = Integer.parseInt(mainTokens.nextToken());
			}

			instance.set(Calendar.MINUTE, min);
			instance.set(Calendar.HOUR_OF_DAY, hour);
			instance.set(Calendar.SECOND, seconds);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		result = instance.getTimeInMillis() / 1000;


		if (LAST_QUOTE_TIME == 0L)
		{
			LAST_QUOTE_TIME = result;
		}
		else
		{
			diff = result - LAST_QUOTE_TIME;

			// 修正由于服务器时间和本地时间不同步引起的隔天差异。
			if (Math.abs(diff) > (AppInt.DAY_SECONDS >> 2))
			{
				if (diff > 0)
				{
					result -= AppInt.DAY_SECONDS;
				}
				else
				{
					result += AppInt.DAY_SECONDS;
				}
			}

			LAST_QUOTE_TIME = result;
		}

		return result;
	}

	/**
	 * 解析搜捷数据
	 * 
	 * @param text
	 * @return
	 */
	public static Float getSoujiePrice(String text)
	{
		Float result = null;
		if (TextUtils.isEmpty(text))
		{
			return result;
		}

		int sellStartPos = text.indexOf(AppString.SOUJIE_SELL);
		int sellEndPos = text.indexOf(AppString.SOUJIE_BUY, sellStartPos + AppString.SOUJIE_SELL.length());

		// 包含有需要的数据
		if (sellStartPos < sellEndPos && sellStartPos != -1)
		{
			String price = text.substring(sellStartPos + AppString.SOUJIE_SELL.length(), sellEndPos);
			result = Float.valueOf(price);
		}

		return result;
	}

	/**
	 * 返回搜捷数据源中的时间串，形如： "01-28 18:55:49"
	 */
	public static String getSoujieTime(String text)
	{
		String result = null;
		if (TextUtils.isEmpty(text))
		{
			return result;
		}

		int beginPos = text.indexOf(AppString.SOUJIE_TIME_BEGIN);
		int endPos = text.indexOf(AppString.SOUJIE_TIME_END, beginPos + AppString.SOUJIE_TIME_BEGIN.length());

		if (beginPos < endPos && beginPos > 0)
		{
			result = text.substring(beginPos + AppString.SOUJIE_TIME_BEGIN.length(), endPos);
		}

		return result;
	}


	/**
	 * 解析形如“01-28 18:55:49”的字符串为date对象，
	 * 
	 * @param text
	 * @return 解析失败返回null
	 */
	public static Date getSoujieDate(String text)
	{
		Date result = null;

		if (TextUtils.isEmpty(text))
		{
			return result;
		}

		text = text.trim();

		// 01-28 18:55:49
		final Calendar instance = Calendar.getInstance();
		int year = instance.get(Calendar.YEAR);
		int month = instance.get(Calendar.MONTH);
		final int dashPos = text.indexOf("-");
		if (dashPos != -1)
		{
			month = Integer.parseInt(text.substring(0, dashPos)) - 1;

			// 28 18:55:49
			text = text.substring(dashPos + 1);
		}

		int day = instance.get(Calendar.DAY_OF_MONTH);
		final int blankPos = text.indexOf(" ");
		if (blankPos != -1)
		{
			day = Integer.parseInt(text.substring(0, blankPos));

			// 18:55:49
			text = text.substring(blankPos + 1).trim();
		}

		String[] arg = text.split(":");

		int hour = instance.get(Calendar.HOUR_OF_DAY);
		int min = instance.get(Calendar.MINUTE);
		int sec = instance.get(Calendar.SECOND);

		if (arg != null && arg.length == 3)
		{
			hour = Integer.parseInt(arg[0]);
			min = Integer.parseInt(arg[1]);
			sec = Integer.parseInt(arg[2]);
		}

		result = new Date(System.currentTimeMillis());
		result.setYear(year - 1900);
		result.setMonth(month);
		result.setDate(day);
		result.setHours(hour);
		result.setMinutes(min);
		result.setSeconds(sec);

		System.out.println("y=" + result.getTime());
		return result;
	}
}
