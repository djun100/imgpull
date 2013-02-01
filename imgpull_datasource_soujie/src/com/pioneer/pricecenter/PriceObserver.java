package com.pioneer.pricecenter;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PriceObserver.java
 * Description：
 * History：
 * 1.0 Administrator 2013-1-19 Create
 */

public interface PriceObserver
{
	/**
	 * 价格变化通知函数
	 * 
	 * @param latestPrice最新的价格。
	 * @param quotTime最新价格的引用时间。 单位为秒
	 */
	void onPriceChanged(float latestPrice, long quotTime);
}
