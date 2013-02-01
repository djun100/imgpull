package com.pioneer.constant;

/*
 * Copyright (C) 2005-2010 PIONEER Inc.All Rights Reserved.
 * FileName:AppString.java
 * Description:
 * History:
 * 1.0 Denverhan 2012-10-27 Create
 */

public interface AppString
{
	String		SERVICE_ACTION						= "com.pioneer.silver.pull";
	String		SERVICE_NAME						= "com.pioneer.silver.service.DataUpdateService";

	String		STRING_PRICE_SPLITER				= "|";
	String		STRING_EQUALS						= "=";
	String		STRING_GOLD							= "gold";
	String		STRING_TIME							= "time";
	String		STRING_BANK_SELL					= "bank_sell";
	String		STRING_BANK_BUY						= "bank_buy";
	String		STRING_MIDDLE_MAX					= "max_middle";
	String		STRING_MIDDLE_MIN					= "min_middle";
	String		STRING_MIDDLE_CURRENT				= "current_middle";
	String		SPLITER								= "&";
	String		UTF_8								= "utf-8";
	String		UA_VALUE							= "Mozilla/5.0 (Linux; U; Android 1.6; zh-cn; generic) AppleWebKit/533.1 (KHTML, likeGecko) Version/4.0 Mobile Safari/533.1";
	String		USER_AGENT							= "User-Agent";
	String		HTTP_ROUTE_DEFAULT_PROXY			= "http.route.default-proxy";
	String		COLON								= ":";
	String		BRACkET_LEFT						= "[";
	String		BRACkET_RIGHT						= "]";
	String		AG_URL_FX678						= "http://m.fx678.com/diy.aspx?code=xagusd";
	String		AG_URL_ICBC							= "http://goldprice.sinaapp.com/ag.php";
	String		AG_URL_TIANTONG						= "http://42.121.237.19/FinanceQuoteServer/client.action?uid=&id=65&rtp=GetQuotesDetail";

	String		KEY_NAME_CODE						= "Code";
	String		KEY_NAME_PRODUCT_NAME				= "Name";
	String		KEY_NAME_QUOTE_TIME					= "QuoteTime";
	String		KEY_NAME_LAST						= "Last";
	String		KEY_NAME_OPEN						= "Open";
	String		KEY_NAME_HIGH						= "High";
	String		KEY_NAME_LOW						= "Low";
	String		KEY_NAME_LAST_CLOSE					= "LastClose";
	String		KEY_NAME_UPDOWN						= "UpDown";
	String		KEY_NAME_UPDOWN_RATE				= "UpDownRate";
	String		KEY_NAME_VOLUME						= "Volume";
	String		KEY_NAME_LAST_SETTILE				= "LastSettle";
	String		KEY_NAME_AVERAGE					= "Average";
	String		KEY_NAME_TURNOVER					= "TurnOver";

	/**
	 * {"desc":"OK","status":1000,"data":{"position":{"avg":0,"pos":0},"quotes":
	 * {"id":"65","open":6184,"time":"01-28 18:55:49","mp":"-0.01","sell":6236,
	 * "buy"
	 * :6246,"name":"天通银","falg":0,"margin":-62,"low":6232,"top":6325},"vote"
	 * :{"total":140,"mid":0,"down":135,"up":5},"tips":"","comment":
	 * "127.0.0.1(127.0.0.1):50681 ; 127.0.0.1:8082"}}
	 */
	String		SOUJIE_SELL							= "\"sell\":";
	String		SOUJIE_BUY							= ",\"buy\":";
	String		SOUJIE_TIME_BEGIN					= "\"time\":\"";
	String		SOUJIE_TIME_END						= "\",\"";

	String		FILE_NAME_DATAK_SEQUENCE_MANAGER	= "datak_sequences.dat";


	String[]	KEY_NAMES							= { KEY_NAME_CODE, KEY_NAME_PRODUCT_NAME, KEY_NAME_QUOTE_TIME, KEY_NAME_LAST, KEY_NAME_OPEN,
			KEY_NAME_HIGH, KEY_NAME_LOW, KEY_NAME_LAST_CLOSE, KEY_NAME_UPDOWN, KEY_NAME_UPDOWN_RATE, KEY_NAME_VOLUME, KEY_NAME_LAST_SETTILE,
			KEY_NAME_AVERAGE, KEY_NAME_TURNOVER	};
}
