package com.google.gwt.sample.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.sample.stockwatcher.client.domain.LoginInfo;
import com.google.gwt.sample.stockwatcher.client.domain.StockPrice;
import com.google.gwt.sample.stockwatcher.client.service.LoginService;
import com.google.gwt.sample.stockwatcher.client.service.LoginServiceAsync;
import com.google.gwt.sample.stockwatcher.client.service.StockService;
import com.google.gwt.sample.stockwatcher.client.service.StockServiceAsync;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StockWatcher implements EntryPoint {

	private static final int REFRESH_INTERVAL = 5000;

	// Stock watcher objects
	private VerticalPanel mainPanel = new VerticalPanel();

	private FlexTable stocksFlexTable = new FlexTable();

	private HorizontalPanel addPanel = new HorizontalPanel();

	private TextBox newSymbolTextBox = new TextBox();

	private Button addStockButton = new Button("Add");

	private Label lastUpdatedLabel = new Label();

	private ArrayList<String> stocks = new ArrayList<String>();

	// Login objects
	private LoginInfo loginInfo = null;

	private VerticalPanel loginPanel = new VerticalPanel();

	private Label loginLabel = new Label(
			"Please sign in to you Google Account to acces the StockWatcher application.");

	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");

	private final StockServiceAsync stockServiceAsync = GWT
			.create(StockService.class);

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {

					@Override
					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							loadStockWatcher();
						} else {
							loadLogin();
						}

					}

					@Override
					public void onFailure(Throwable caught) {
						handleError(caught);

					}
				});
	}

	private void loadLogin() {
		signInLink.setHref(loginInfo.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get("stockList").add(loginPanel);
	}

	private void loadStockWatcher() {

		// Set up sign out hyperlink
		signOutLink.setHref(loginInfo.getLogoutUrl());

		// Create table for stock data.
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");

		// stocksFlexTable.setBorderWidth(10);

		loadStock();

		// Assemble Add Stock panel.
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);

		// addPanel.setBorderWidth(10);

		// Assemble Main panel.
		mainPanel.add(signOutLink);
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("stockList").add(mainPanel);

		// Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		// Listen for mouse button
		addStockButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addStock();

			}
		});

		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}

			}
		});
	}

	private void loadStock() {
		stockServiceAsync.getStocks(new AsyncCallback<String[]>() {

			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);

			}

			@Override
			public void onSuccess(String[] symbols) {
				displayStocks(symbols);

			}

			private void displayStocks(String[] symbols) {
				for (String symbol : symbols) {
					displayStock(symbol);
				}

			}
		});

	}

	private void addStock() {
		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters,
		// or dots.
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newSymbolTextBox.selectAll();
			return;
		}

		// Don't add the stock if it's already in the table.
		if (stocks.contains(symbol)) {
			return;
		}

		// displayStock(symbol);
		addStock(symbol);

	}

	private void addStock(final String symbol) {
		stockServiceAsync.addStock(symbol, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);

			}

			@Override
			public void onSuccess(Void result) {
				displayStock(symbol);

			}
		});

	}

	private void displayStock(final String symbol) {
		// Add the stock to the table.
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);

		// Add a button to remove this stock from the table.
		Button removeStockButton = new Button("x");
		removeStockButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// int removedIndex = stocks.indexOf(symbol);
				// stocks.remove(removedIndex);
				// stocksFlexTable.removeRow(removedIndex + 1);
				removeStock(symbol);
			}
		});
		stocksFlexTable.setWidget(row, 3, removeStockButton);

		// Get the stock price.
		refreshWatchList();
	}

	private void removeStock(final String symbol) {
		stockServiceAsync.removeStock(symbol, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				handleError(caught);

			}

			@Override
			public void onSuccess(Void result) {
				undisplayStock(symbol);

			}
		});

	}

	private void undisplayStock(String symbol) {
		int removedIndex = stocks.indexOf(symbol);
		stocks.remove(removedIndex);
		stocksFlexTable.removeRow(removedIndex + 1);
	}

	private void refreshWatchList() {
		final double MAX_PRICE = 100.0; // $100.00
		final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

		StockPrice[] prices = new StockPrice[stocks.size()];
		for (int i = 0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE
					* (Random.nextDouble() * 2.0 - 1.0);

			prices[i] = new StockPrice(stocks.get(i), price, change);
		}

		updateTable(prices);

	}

	private void updateTable(StockPrice[] prices) {
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}
	}

	private void updateTable(StockPrice stockPrice) {
		// Make sure the stock is still in the stock table.
		if (!stocks.contains(stockPrice.getSymbol())) {
			return;
		}

		int row = stocks.indexOf(stockPrice.getSymbol()) + 1;

		// Format the data in the Price and Change fields.
		String priceText = NumberFormat.getFormat("#,##0.00").format(
				stockPrice.getPrice());
		NumberFormat changeFormat = NumberFormat
				.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(stockPrice.getChange());
		String changePercentText = changeFormat.format(stockPrice
				.getChangePercent());

		// Populate the Price and Change fields with new data.
		stocksFlexTable.setText(row, 1, priceText);
		stocksFlexTable.setText(row, 2, changeText + " (" + changePercentText
				+ "%)");

		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
		// + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
				+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM)
						.format(new Date()));

	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof NotLoggedInException) {
			Window.Location.replace(loginInfo.getLogoutUrl());
		}
	}
}
