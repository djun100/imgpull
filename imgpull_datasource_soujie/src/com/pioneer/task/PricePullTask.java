package com.pioneer.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.pioneer.constant.AppString;
import com.pioneer.network.NetworkControl;
import com.pioneer.silver.R;
import com.pioneer.util.StringUtil;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName：PricePullTask.java
 * Description：
 * History：
 * 1.0 Denverhan 2012-11-8 Create
 */

public class PricePullTask extends Task
{
	private static final String	TAG		= "PricePullTask";

	private String				mUrl;
	private static final int	HTTP_OK	= 200;
	private Context				mContext;

	public PricePullTask(Context c, String url, TaskObserver observer)
	{
		super();
		mUrl = url;
		mContext = c;
		addObserver(observer);
		notifyObserver();
		mErrorMsg = StringUtil.getString(R.string.price_pull_task_create_task) + url;
		new Thread(this, "PricePullTask url=" + url).start();
	}

	private boolean checkCanceled()
	{
		if (mHasCanceled)
		{
			mStatus = STATUS_CANCEL;
			mErrorMsg = "task canceled!";
			notifyObserver();
		}

		return mHasCanceled;
	}

	@Override
	public void run()
	{
		if (checkCanceled())
		{
			checkFreeObservers();
			return;
		}

		mStatus = STATUS_DOING;
		mProgress = 0.1f;
		notifyObserver();

		DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
		NetworkControl.NetType localNetType = NetworkControl.getNetType(mContext);

		if ((localNetType != null) && (localNetType.isWap()))
		{
			HttpHost localHttpHost;
			String proxy = localNetType.getProxy();
			int port = localNetType.getPort();
			localHttpHost = new HttpHost(proxy, port);

			HttpParams localHttpParams = localDefaultHttpClient.getParams();
			localHttpParams.setParameter(AppString.HTTP_ROUTE_DEFAULT_PROXY, localHttpHost);
		}

		if (checkCanceled())
		{
			checkFreeObservers();
			return;
		}

		HttpGet httpGet = new HttpGet(mUrl);

		try
		{
			mStatus = STATUS_FAILED;
			httpGet.setHeader(AppString.USER_AGENT, AppString.UA_VALUE);
			HttpResponse httpResponse = localDefaultHttpClient.execute(httpGet);
			if (checkCanceled())
			{
				checkFreeObservers();
				return;
			}

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			BufferedReader bufferedReader;
			StringBuffer stringBuffer = new StringBuffer();

			if (statusCode == HTTP_OK)
			{
				bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), AppString.UTF_8));

				for (String line = bufferedReader.readLine();; line = bufferedReader.readLine())
				{
					if (!TextUtils.isEmpty(line))
					{
						stringBuffer.append(line);
					}
					else
					{
						bufferedReader.close();
						break;
					}
				}
				if (checkCanceled())
				{
					checkFreeObservers();
					return;
				}

				final String content = stringBuffer.toString();
				mData = content;
				mStatus = STATUS_COMPLEMENT;
				Log.d(TAG, "result=" + content);
			}
			else
			{
				mErrorCode = ERROR_CODE_PROTOCOL;
				mErrorMsg = "the server did not return 200!";
			}
		}
		catch (ClientProtocolException e)
		{
			mErrorCode = ERROR_CODE_NETWORK;
			mErrorMsg = e.getClass().getSimpleName() + AppString.COLON + e.getMessage();
			e.printStackTrace();
		}
		catch (IOException e)
		{
			mErrorCode = ERROR_CODE_IO;
			mErrorMsg = e.getClass().getSimpleName() + AppString.COLON + e.getMessage();
			e.printStackTrace();
		}
		notifyObserver();

		checkFreeObservers();
	}

}
