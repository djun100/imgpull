package com.pioneer.k;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PeriodType.java
 * Description：
 * History：
 * 1.0 Denverhan 2013-1-18 Create
 */

public class PeriodType
{
	// 实时
	public static final int	REAL_TIME		= 5;

	// 一分钟
	public static final int	ONE_MINUTES		= 60;

	// 三分钟
	public static final int	THREE_MINUTES	= 180;

	// 五分钟
	public static final int	FIVE_MINUTES	= 300;

	// 十分钟
	public static final int	TEN_MINUTES		= 600;

	// 十五分钟
	public static final int	FIFTEEN_MINUTES	= 900;

	// 三十分钟
	public static final int	THIRTY_MINUTES	= 1800;

	// 一小时
	public static final int	ONE_HOUR		= 3600;
	
	

	public static String getPeriodName(int periodType)
	{
		String name = "";
		switch(periodType)
		{
			case REAL_TIME:
			{
				name = "实时";
				break;
			}
			case ONE_MINUTES:
			{
				name = "一分钟";
				break;
			}
			case THREE_MINUTES:
			{
				name = "三分钟";
				break;
			}
			case FIVE_MINUTES:
			{
				name = "五分钟";
				break;
			}
			case TEN_MINUTES:
			{
				name = "十分钟";
				break;
			}
			case FIFTEEN_MINUTES:
			{
				name = "十五分钟";
				break;
			}
			case THIRTY_MINUTES:
			{
				name = "三十分钟";
				break;
			}
			case ONE_HOUR:
			{
				name = "一小时";
				break;
			}
			default:
			{
				name ="未知";
			}
		}
		
		return name;
	}
}
