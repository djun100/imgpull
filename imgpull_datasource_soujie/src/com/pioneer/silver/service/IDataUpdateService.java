package com.pioneer.silver.service;

import android.os.Handler;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：IDataUpdateService.java
 * Description：
 * History：
 * 1.0 Denverhan 2012-10-27 Create
 */

public interface IDataUpdateService
{
	/**
	 * 获得银行买入价格
	 * @return
	 */
	public abstract float getBankBuyPrice();

	/**
	 * 获得价格有效时间
	 * @return
	 */
	public abstract String getUpdateTime();

	/**
	 * 设置更新间隔，单位毫秒。
	 * @param interval
	 */
	public abstract void setUpdateInterval(int interval);

	/**
	 * 获取价格更新间隔，单位毫秒
	 * @return
	 */
	public abstract int getUpdateInterval();

	/**
	 * 获取服务出错信息。
	 * @return
	 */
	public abstract String getErrorMsg();

	/**
	 * 获取价格获取状态。
	 * @return
	 */
	public abstract int getServiceStatus();
	
	/**
	 * UI更新Handler
	 * @param handler
	 */
	public abstract void setUpdateHandler(Handler handler);
}
