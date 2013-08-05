package com.google.gwt.sample.stockwatcher.client.service;

import com.google.gwt.sample.stockwatcher.client.domain.LoginInfo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	public LoginInfo login(String requestUri);
}
