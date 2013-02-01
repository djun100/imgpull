package com.pioneer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pioneer.constant.AppString;
import com.pioneer.k.DataK;
import com.pioneer.k.DataKSequenceManager;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：FileUtil.java
 * Description：
 * History：
 * 1.0 Denverhan 2012-9-17 Create
 */

public class FileUtil
{

	private static final String	TAG					= "FileUtil";

	// 在SD卡上的文件夹的名称
	public static final String	DOWNLOAD_DIR		= "Silver Download";

	public static final String	DATAKSEQUENCE_DIR	= "DataK";


	public static FileOutputStream openOutputStream(File file) throws IOException
	{
		if (file.exists())
		{
			if (file.isDirectory())
			{
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false)
			{
				throw new IOException("File '" + file + "' cannot be written to");
			}
		}
		else
		{
			File parent = file.getParentFile();
			if (parent != null && parent.exists() == false)
			{
				if (parent.mkdirs() == false)
				{
					throw new IOException("File '" + file + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file);
	}


	public static FileOutputStream openOutputStream(String filePath) throws IOException
	{
		return openOutputStream(new File(filePath));
	}


	/**
	 * 获得下载目录
	 * 
	 * @return
	 */
	public static File getDownloadRootDir()
	{
		File childDir = null;
		childDir = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_DIR);
		if (!childDir.exists())
			childDir.mkdirs();

		return childDir;
	}

	/**
	 * 获得日期目录
	 * 
	 * @param date
	 * @return
	 */
	public static File getDayDir(String date)
	{

		if (TextUtils.isEmpty(date))
		{

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
			date = sdf.format(new Date());
		}

		File childDir = null;
		childDir = new File(getDownloadRootDir(), date);
		if (!childDir.exists())
			childDir.mkdirs();

		return childDir;
	}

	/**
	 * 获得K线管理器文件所在目录。
	 * 
	 * @param date
	 * @return
	 */
	public static File getDataKSequenceDir(String date)
	{

		File childDir = null;
		childDir = new File(getDownloadRootDir(), date);
		if (!childDir.exists())
			childDir.mkdirs();

		return childDir;
	}

	/**
	 * 保存现有的K线集合。
	 * 
	 * @param k1
	 * @param fileName
	 * @return 是否保存成功
	 */
	public static boolean saveDataKSequence(DataKSequenceManager k1, File fileName)
	{
		try
		{
			final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
			oos.writeObject(k1);
			oos.close();
			Log.d(TAG, "saveDataKSequence() successfully k=" + k1.toString());
			return true;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		Log.d(TAG, "saveDataKSequence() failed");
		return false;
	}

	/**
	 * 从文件加载k线管理器
	 * 
	 * @param filename文件名称
	 * @return管理器 加载失败则返回null。
	 */
	public static DataKSequenceManager loadDataKSequenceManager(String filename)
	{
		DataKSequenceManager result = null;
		try
		{
			ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File(FileUtil.getDataKSequenceDir(FileUtil.DATAKSEQUENCE_DIR),
					filename)));
			result = (DataKSequenceManager) oos.readObject();
			oos.close();
			result.initialAllDataKSequence();
			Log.d(TAG, "loadDataKSequenceManager(" + filename + ") successfully result=" + result.toString());
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace(); 
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		if (result == null)
		{
			Log.d(TAG, "loadDataKSequenceManager(" + filename + ") failed");
			result = new DataKSequenceManager();
		}

		return result;
	}
}
