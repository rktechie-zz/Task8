package controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.CustomerDAO;
import model.FundDAO;
import model.Model;
import model.PositionDAO;
import model.TransactionDAO;

import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import databean.CustomerBean;
import databean.EmployeeBean;
import databean.FundBean;
import databean.PositionBean;
import databean.TransactionBean;
import formbean.SellFundForm;

public class SellFundAction extends Action {
	private FormBeanFactory<SellFundForm> formBeanFactory = FormBeanFactory.getInstance(SellFundForm.class);

	private TransactionDAO transactionDAO;
	private FundDAO fundDAO;
	private PositionDAO positionDAO;
	private CustomerDAO customerDAO;

	public SellFundAction(Model model) {
		transactionDAO = model.getTransactionDAO();
		fundDAO = model.getFundDAO();
		positionDAO = model.getPositionDAO();
		model.getFundPriceHistoryDAO();
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return "sellFund";
	}

	public String perform(HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		HttpSession session = request.getSession();
		GsonBuilder builder = new GsonBuilder();
		builder.disableHtmlEscaping();
		Gson gson = builder.create();
		ReturnJson returnJson = new ReturnJson();

		try {
			SellFundForm sellFundForm = formBeanFactory.create(request);
			request.setAttribute("form", sellFundForm);

			if (session.getAttribute("user") == null) {
				returnJson.message = "You must log in prior to making this request. ";
				return gson.toJson(returnJson);
			}
			
			if (!(session.getAttribute("user") instanceof CustomerBean) ){
				returnJson.message = "I'm sorry you are not authorized to preform that action";
				return gson.toJson(returnJson);
			}

			if (!sellFundForm.isPresent()) {
				returnJson.message = "Input Parameters could not be read.";
				return gson.toJson(returnJson);
			}

			new DecimalFormat("#,##0.000");
			new DecimalFormat("###,##0.00");
			CustomerBean customerBean = (CustomerBean) session.getAttribute("user");

			positionDAO
					.match(MatchArg.equals("customerId", customerBean.getCustomerId()));

			// if(positionList != null) {
			// List<PositionInfo> positionInfoList = new
			// ArrayList<PositionInfo>();
			// for(PositionBean a: positionList) {
			// double shares = ((double)(a.getShares())/1000.0);
			//
			// double price =
			// ((double)(historyDAO.getLatestFundPrice(a.getFundId()).getPrice()
			// / 100.0));
			// double value = shares * price;
			// String name=fundDAO.read(a.getFundId()).getName();
			//
			// String sharesString = df3.format(shares);
			// String priceString = df2.format(price);
			// String valueString = df2.format(value);
			//
			// PositionInfo aInfo = new
			// PositionInfo(name,sharesString,priceString,"$" + valueString);
			// positionInfoList.add(aInfo);
			// }
			// session.setAttribute("positionInfoList",positionInfoList);
			// }

			errors.addAll(sellFundForm.getValidationErrors());
			if (errors.size() != 0) {
				returnJson.message = errors.get(0);
				return gson.toJson(returnJson);
			}
			String userName = customerBean.getUserName();
			int customerId = customerBean.getCustomerId();

			// Get the fund ID of the fund name in form
			FundBean fundBean = fundDAO.read(sellFundForm.getFundSymbol());
			if (fundBean == null) {
				// errors.add("Fund does not exist");
				returnJson.message = "Fund does not exist";
				return gson.toJson(returnJson);
			}
			int fundId = fundBean.getFundId();
			// How to determine whether this customer own this fund or not
			PositionBean position = positionDAO.read(customerId, fundId);
			if (position == null) {
				// errors.add("You do not own this fund!");
				returnJson.message = "You do not own this fund";
				return gson.toJson(returnJson);
			}
			double curShares = (double) position.getShares() / 1000;
			double shares = Double.parseDouble(sellFundForm.getNumShares());
			if (shares == 0) {
				// errors.add("You can not sell zero shares");
				returnJson.message = "You can not sell zero shares";
				return gson.toJson(returnJson);
			}
			if ((shares * 1000.0 - (long) (shares * 1000.0)) > 0) {
				// errors.add("We only allow at most three decimal for shares");
				returnJson.message = "We only allow at most three decimal for shares";
				return gson.toJson(returnJson);
			}

			// Check valid shares
			if (curShares < shares) {
				// errors.add("You do not have enough shares!");
				returnJson.message = "I'm sorry, you don’t have enough shares of that fund in your portfolio. ";
				return gson.toJson(returnJson);
			}
			// double validShares = transactionDAO.getValidShares(customerId,
			// fundId, curShares);
			// if (shares > validShares) {
			//// errors.add("You do not have enough shares! (including pending
			// transaction)");
			// returnJson.message = "You do not have enough shares! (including
			// pending transaction)";
			// return gson.toJson(returnJson.message);
			// }

			double amount = shares * (fundBean.getLatestPrice() / 100.0);
			// Create a transaction bean
			Transaction.begin();
			TransactionBean transactionBean = new TransactionBean();
			transactionBean.setCustomerId(customerId);
			transactionBean.setFundId(fundId);
			transactionBean.setUserName(userName);
			transactionBean.setShares((long) (shares * 1000l));
			transactionBean.setTransactionType("4");
			Date currDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
			dateFormat.setLenient(false);
			String currDateString = dateFormat.format(currDate);
			transactionBean.setExecuteDate(currDateString);
			transactionDAO.create(transactionBean);
			customerDAO.setBalance(customerBean.getUserName(), (long) (amount * 100l), "sell");
			PositionBean[] positionBean = positionDAO.match(MatchArg.equals("customerId", customerBean.getCustomerId()),
					MatchArg.equals("fundId", fundId));

			positionBean[0].setCustomerId(customerId);
			positionBean[0].setFundId(fundId);
			positionBean[0].setShares(positionBean[0].getShares() - (long) (shares * 1000l));
			positionDAO.update(positionBean[0]);

			Transaction.commit();
			returnJson.message = "The sale was successfully completed. ";
			return gson.toJson(returnJson.message);

		} catch (NumberFormatException e) {
			errors.add(e.getMessage());
			returnJson.message = "I'm sorry, there was a problem selling funds";
			return gson.toJson(returnJson.message);
		} catch (RollbackException e) {
			errors.add(e.getMessage());
			returnJson.message = "I'm sorry, there was a problem selling funds";
			return gson.toJson(returnJson.message);
		} catch (FormBeanException e) {
			errors.add(e.getMessage());
			returnJson.message = "I'm sorry, there was a problem selling funds";
			return gson.toJson(returnJson.message);
		}
	}

	private class ReturnJson {
		String message;
	}
}
