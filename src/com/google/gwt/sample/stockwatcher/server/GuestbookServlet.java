package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class GuestbookServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2414585529232701428L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user != null) {
			resp.setContentType("text/html");
			resp.getWriter().println("Hello, " + user.getNickname());
			resp.getWriter().println(
					"<a href=\""
							+ userService.createLogoutURL(req.getRequestURI())
							+ "\">Logout</a>");
		} else {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
		}

		// super.doGet(req, resp);
	}
}
