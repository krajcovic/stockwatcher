package com.google.gwt.sample.stockwatcher.client.service;

import com.google.gwt.sample.stockwatcher.client.domain.LoginInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {

	void login(String requestUri, AsyncCallback<LoginInfo> callback);

}
