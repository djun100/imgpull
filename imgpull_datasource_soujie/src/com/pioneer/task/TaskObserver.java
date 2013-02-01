package com.pioneer.task;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.		
 * 
 * FileName£ºTaskObserver.java
 * 
 * Description£º
 * 
 * History£º
 * 1.0 Denverhan 2012-11-8 Create
 */

public interface TaskObserver
{
	public void onTaskComplement(Task task);
	public void onTaskFailed(Task task);
	public void onTaskProgress(Task task);
	public void onTaskCreate(Task task);
	public void onTaskCancel(Task task);
}
