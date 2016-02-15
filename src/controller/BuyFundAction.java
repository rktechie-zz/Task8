package controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.Gson;

import databean.CustomerBean;
import databean.FundBean;
import databean.FundInfoBean;
import databean.FundPriceHistoryBean;
import databean.PositionBean;
import databean.TransactionBean;
import formbean.BuyFundForm;
import model.CustomerDAO;
import model.FundDAO;
import model.FundPriceHistoryDAO;
import model.Model;
import model.PositionDAO;
import model.TransactionDAO;

public class BuyFundAction extends Action {
	private FormBeanFactory<BuyFundForm> formBeanFactory = FormBeanFactory.getInstance(BuyFundForm.class);

	private TransactionDAO transactionDAO;
	private CustomerDAO customerDAO;
	private FundDAO fundDAO;
	private FundPriceHistoryDAO fundPriceHistoryDAO;
	private PositionDAO positionDAO;

	public BuyFundAction(Model model) {
		transactionDAO = model.getTransactionDAO();
		fundDAO = model.getFundDAO();
		fundPriceHistoryDAO = model.getFundPriceHistoryDAO();
		customerDAO = model.getCustomerDAO();
		positionDAO = model.getPositionDAO();
	}

	public String getName() {
		return "buyFund";
	}

	public String perform(HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		HttpSession session = request.getSession();
		Gson gson = new Gson();
		ReturnJson returnJson = new ReturnJson();

		try {
			BuyFundForm buyFundForm = formBeanFactory.create(request);
			request.setAttribute("form", buyFundForm);

			if (session.getAttribute("user") == null) {
				returnJson.message = "You must log in prior to making this request";
				return gson.toJson(returnJson.message);
			}
			
			if (!buyFundForm.isPresent()) {
				returnJson.message = "Input Parameters could not be read.";
				return gson.toJson(returnJson);
			}


			DecimalFormat df = new DecimalFormat("###,##0.00");
			FundBean[] fundList = fundDAO.match();
			if(fundList != null) {
				List<FundInfoBean> fundInfoList = new ArrayList<FundInfoBean>();
				for(FundBean a: fundList) {
					String name = a.getName();
					FundPriceHistoryBean historyBean = fundPriceHistoryDAO.getLatestFundPrice(a.getFundId());
					if(historyBean != null) {
						double price = ((double)(fundPriceHistoryDAO.getLatestFundPrice(a.getFundId()).getPrice() / 100.0));
						String priceString = df.format(price);
						FundInfoBean aInfo = new FundInfoBean(name, "$" + priceString);
						fundInfoList.add(aInfo);
					}
				}
				session.setAttribute("fundListInfoList", fundInfoList);
			}
			CustomerBean customerBean = (CustomerBean) session.getAttribute("user");
			Double cash = (double) (customerDAO.read(customerBean.getUserName()).getCash()/100.00);
			DecimalFormat df2 = new DecimalFormat(	"###,##0.00");
			//System.out.println(cash);
//			request.setAttribute("avai_cash",df2.format(transactionDAO.getValidBalance(customerBean.getUserName(), cash)));
			
			errors.addAll(buyFundForm.getValidationErrors());
			if (errors.size() != 0) {
				returnJson.message = errors.get(0);
				return gson.toJson(returnJson.message);
			}
			
			// Current customer and the customer ID
			String userName = customerBean.getUserName();
			int customerId = customerBean.getCustomerId();
			long curCash = customerBean.getCash() / 100;
			
			// Calculate shares
			double amount = Double.parseDouble(buyFundForm.getCashValue());
			if (amount < 1) {
//				errors.add("Please enter an amount at least $1s");
				returnJson.message = "Please enter an amount at least $1s";
				return gson.toJson(returnJson.message); 
			}
			// Can't acceed 10,000,000
			if (amount > 1000000) {
//				errors.add("Please enter an amount less than or equal to $ 1,000,000");
				returnJson.message = "Please enter an amount less than or equal to $ 1,000,000";
				return gson.toJson(returnJson.message); 
			}
			if ((amount * 100.0 - (long) (amount * 100.0)) > 0) {
//				errors.add("We only allow at most two decimal for amount");
				returnJson.message = "We only allow at most two decimal for amount";
				return gson.toJson(returnJson.message); 
			}
			
			
			//Check valid balance
			double validBalance = cash;
			if (amount > validBalance) {
//				errors.add("You do not have enough money to proceed with the transaction");
				returnJson.message = "You do not have enough money to proceed with the transaction";
				return gson.toJson(returnJson.message); 
			}

			// Get the fund ID of the fund name in form
			FundBean fundBean = fundDAO.read(buyFundForm.getFundSymbol());
			if (fundBean == null) {
//				errors.add("Fund does not exist");
				returnJson.message = "Fund does not exist";
				return gson.toJson(returnJson.message); 
			}
			int fundId = fundBean.getFundId();
			
			long a = (long)(amount * 100l);
			if (fundBean.getLatestPrice() > a) {
				returnJson.message = "You do not have enough money to proceed with the transaction";
				return gson.toJson(returnJson.message);
			}
			
			int noOfShares = (int) (a / fundBean.getLatestPrice());
			a = noOfShares * (fundBean.getLatestPrice());
			
			// Create a transaction bean
			Transaction.begin();
			TransactionBean transactionBean = new TransactionBean();
			transactionBean.setCustomerId(customerId);
			transactionBean.setFundId(fundId);
			transactionBean.setUserName(customerBean.getUserName());
			transactionBean.setAmount(a);
			transactionBean.setTransactionType("8");
			Date currDate = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
			dateFormat.setLenient(false);
			String currDateString = dateFormat.format(currDate);
			transactionBean.setExecuteDate(currDateString);
			transactionDAO.create(transactionBean);
			customerDAO.setBalance(customerBean.getUserName(), a, "buy");
			
			PositionBean[] positionBean = positionDAO.match(MatchArg.equals("customerId",customerBean.getCustomerId()), MatchArg.equals("fundId", fundId));
			if (positionBean.length == 0) {
				PositionBean positionBeanTemp = new PositionBean(); 
				positionBeanTemp.setCustomerId(customerId);
				positionBeanTemp.setFundId(fundId);
				positionBeanTemp.setShares(noOfShares*1000);
				positionDAO.create(positionBeanTemp);
			} else {
				positionBean[0].setCustomerId(customerId);
				positionBean[0].setFundId(fundId);
				positionBean[0].setShares(positionBean[0].getShares() + noOfShares*1000);
				positionDAO.update(positionBean[0]);	
			}
			Transaction.commit();
			request.removeAttribute("form");
			returnJson.message = "The account has been successfully updated";
			return gson.toJson(returnJson.message);

		} catch (NumberFormatException e) {
//			errors.add(e.getMessage());
			System.out.println("number format");
			returnJson.message = "I'm sorry, there was a problem buying funds";
			return gson.toJson(returnJson.message); 
		} catch (RollbackException e) {
//			errors.add(e.getMessage());
			System.out.println("rollback");
			returnJson.message = "I'm sorry, there was a problem buying funds";
			return gson.toJson(returnJson.message); 
		} catch (FormBeanException e) {
//			errors.add(e.getMessage());
			System.out.println("formbean");
			returnJson.message = "I'm sorry, there was a problem buying funds";
			return gson.toJson(returnJson.message); 
		}
	}
	
	private class ReturnJson {
		String message;
	}
}
