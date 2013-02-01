package com.pioneer.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.pioneer.constant.AppString;
import com.pioneer.engine.AppEngine;
import com.pioneer.silver.R;
import com.pioneer.util.FileUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:ImgSaveTask.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-9-17 Create
 */

public class ImageSaveTask extends Task
{
	private Bitmap	mBitmap;
	private String	mFileName;

	private boolean	mNotify	= true;

	public ImageSaveTask(Bitmap pic, final String fileName)
	{
		super();
		mBitmap = pic;
		mFileName = fileName;

		if (mBitmap != null && !TextUtils.isEmpty(fileName))
		{
			new Thread(this).start();
		}
	}

	public ImageSaveTask(Bitmap pic, final String fileName, boolean notify)
	{
		this(pic, fileName);
		mNotify = notify;
	}

	@Override
	public void run()
	{
		mStatus = STATUS_DOING;
		mProgress = 0.1f;
		notifyObserver();
		
		boolean isOK = true;
		File file = new File(FileUtil.getDayDir(""), mFileName);
		FileOutputStream fOut = null;
		mStatus = STATUS_FAILED;
		
		try
		{
			file.createNewFile();
			fOut = new FileOutputStream(file);
		}
		catch (FileNotFoundException e)
		{
			mErrorMsg = e.getClass().getSimpleName() + AppString.COLON + e.getMessage();
			
			e.printStackTrace();
			isOK = false;
		}
		catch (IOException e)
		{
			mErrorMsg = e.getClass().getSimpleName() + AppString.COLON + e.getMessage();
			
			e.printStackTrace();
			isOK = false;
		}

		try
		{
			if (fOut != null)
			{
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();
				
				mStatus = STATUS_COMPLEMENT;
			}
		}
		catch (IOException e)
		{
			isOK = false;
			e.printStackTrace();
			
			mErrorMsg = e.getClass().getSimpleName() + AppString.COLON + e.getMessage();
		}
		notifyObserver();

		class ShowToast implements Runnable
		{
			private String	mText	= "";

			public ShowToast(String text)
			{
				if (!TextUtils.isEmpty(text))
				{
					mText = text;
					AppEngine.getInstance().getHandler().post(this);
				}
			}

			@Override
			public void run()
			{
				Toast.makeText(AppEngine.getInstance().getContext(), mText, Toast.LENGTH_SHORT).show();
			}
		}

		if (mNotify)
		{
			if (!isOK)
			{
				new ShowToast(mFileName + AppEngine.getInstance().getContext().getString(R.string.save_failed));
			}
		}
		
		Uri data = Uri.parse("file://" + mFileName);
		AppEngine.getInstance().getContext().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));

	}
}
