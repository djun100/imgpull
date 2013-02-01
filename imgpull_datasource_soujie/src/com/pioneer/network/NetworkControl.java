package com.pioneer.network;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:NetworkControl.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-10-10 Create
 */


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;

public class NetworkControl
{
	public static NetType getNetType(Context paramContext)
	{
		ConnectivityManager localConnectivityManager = (ConnectivityManager) paramContext.getSystemService("connectivity");
		if (localConnectivityManager == null)
		{
			return null;
		}

		NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
		if (localNetworkInfo == null)
		{
			return null;
		}

		String str1 = localNetworkInfo.getTypeName();
		if (str1.equalsIgnoreCase("WIFI"))
		{
			return null;
		}
		
		if (str1.equalsIgnoreCase("MOBILE"))
		{
			String defaultHost = Proxy.getDefaultHost();
			if ((defaultHost != null) && (!defaultHost.equals("")))
			{
				NetType localNetType = new NetType();
				localNetType.setProxy(defaultHost);
				int port = Proxy.getDefaultPort();
				localNetType.setPort(port);
				localNetType.setWap(true);

				return localNetType;
			}
		}

		return null;
	}

	public static class NetType
	{
		private String	apn			= "";
		private boolean	isWap		= false;
		private int		port		= 0;
		private String	proxy		= "";
		private String	typeName	= "";

		public String getApn()
		{
			return this.apn;
		}

		public int getPort()
		{
			return this.port;
		}

		public String getProxy()
		{
			return this.proxy;
		}

		public String getTypeName()
		{
			return this.typeName;
		}

		public boolean isWap()
		{
			return this.isWap;
		}

		public void setApn(String paramString)
		{
			this.apn = paramString;
		}

		public void setPort(int paramInt)
		{
			this.port = paramInt;
		}

		public void setProxy(String paramString)
		{
			this.proxy = paramString;
		}

		public void setTypeName(String paramString)
		{
			this.typeName = paramString;
		}

		public void setWap(boolean paramBoolean)
		{
			this.isWap = paramBoolean;
		}
	}
}
