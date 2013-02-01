package com.pioneer.k;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PatternAnalyseResult.java
 * Description：K线形态分析结果。
 * History：
 * 1.0 Denverhan 2013-1-18 Create
 */

public class PatternAnalyseResult
{
	// 分析种类
	private String	mCompareName;

	// 分析结论
	private String	mAnalyseComment;

	// 分析用到的K线根数
	private int		mDataKCountUsed;

	// 分析所用K线的周期
	private int		mDataKPeriod;

	/**
	 * 构造一个分析结果
	 * 
	 * @param compareType分析种类
	 * @param comment分析结论
	 * @param count分析用到的K线根数
	 * @param KPeriod所分析的K线的周期
	 */
	public PatternAnalyseResult(String compareType, String comment, int count, int KPeriod)
	{
		mCompareName = compareType;
		mAnalyseComment = comment;
		mDataKCountUsed = count;
		mDataKPeriod = KPeriod;
	}

	/**
	 * 分析方法名称
	 * 
	 * @return
	 */
	public String getCompareType()
	{
		return mCompareName;
	}

	public int getDataKPeriod()
	{
		return mDataKPeriod;
	}

	/**
	 * 分析结论
	 * 
	 * @return
	 */
	public String getAnalyseComment()
	{
		return mAnalyseComment;
	}

	/**
	 * 分析用到的K线根数
	 * 
	 * @return
	 */
	public int getDataKCountUsed()
	{
		return mDataKCountUsed;
	}
}
