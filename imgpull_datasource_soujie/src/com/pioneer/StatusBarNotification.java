package com.pioneer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.pioneer.silver.ImgPull;


/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：Notification.java
 * Description：状态栏通知。
 * History：
 * 1.0 Denverhan 2012-11-5 Create
 */


public class StatusBarNotification
{
	public static final int	NOTIFY_ID			= 100;
	public static int		EXCEPTION_NOTIFY_ID	= 101;
	public static int		ALARM_NOTIFY_ID		= 102;
	private Context			mContext;
	NotificationManager		mNotifimanager;

	public StatusBarNotification(Context c)
	{
		mContext = c;
		mNotifimanager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void clearNotification()
	{
		mNotifimanager.cancel(NOTIFY_ID);
	}

	public void showNotification(int icon, CharSequence tickertext, String title, String content, boolean hasSound, int id)
	{
		// 设置一个唯一的ID，随便设置

		// Notification管理器
		Notification notification = new Notification(icon, tickertext, System.currentTimeMillis());
		// 后面的参数分别是显示在顶部通知栏的小图标，小图标旁的文字（短暂显示，自动消失）系统当前时间（不明白这个有什么用）

		notification.defaults = hasSound ? Notification.DEFAULT_ALL : Notification.DEFAULT_LIGHTS;
		// 这是设置通知是否同时播放声音或振动，声音为Notification.DEFAULT_SOUND
		// 振动为Notification.DEFAULT_VIBRATE;
		// Light为Notification.DEFAULT_LIGHTS，在我的Milestone上好像没什么反应
		// 全部为Notification.DEFAULT_ALL
		// 如果是振动或者全部，必须在AndroidManifest.xml加入振动权限
		PendingIntent pt = PendingIntent.getActivity(mContext, 0, new Intent(mContext, ImgPull.class), 0);
		// 点击通知后的动作，这里是转回main 这个Acticity
		notification.setLatestEventInfo(mContext, title, content, pt);
		mNotifimanager.notify(id, notification);
	}

}
