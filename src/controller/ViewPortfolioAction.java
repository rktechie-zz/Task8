package controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import databean.CustomerBean;
import databean.FundBean;
import databean.PositionBean;
import databean.PositionInfo;
import model.CustomerDAO;
import model.FundDAO;
import model.Model;
import model.PositionDAO;

public class ViewPortfolioAction extends Action {
	private FundDAO fundDAO;
	private PositionDAO positionDAO;
	private CustomerDAO customerDAO;

	public ViewPortfolioAction(Model model) {
		fundDAO = model.getFundDAO();
		positionDAO = model.getPositionDAO();
		model.getFundPriceHistoryDAO();
		model.getTransactionDAO();
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return "viewPortfolio";
	}

	public String perform(HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		HttpSession session = request.getSession();
		GsonBuilder builder = new GsonBuilder();
		builder.disableHtmlEscaping();
		Gson gson = builder.create();
		ReturnJson returnJson = new ReturnJson();
		ArrayList<PositionInfo> list = new ArrayList<PositionInfo>();


		if (session.getAttribute("user") == null) {
			returnJson.Message = "You must log in prior to making this request. ";
			return gson.toJson(returnJson);
		}

		if (!(session.getAttribute("user") instanceof CustomerBean)) {
			returnJson.Message = "I'm sorry you are not authorized to preform that action. ";
			return gson.toJson(returnJson);
		}

		DecimalFormat df3 = new DecimalFormat("#,##0.000");
		DecimalFormat df2 = new DecimalFormat("###,##0.00");

		try {
			Transaction.begin();
			CustomerBean customerBean = (CustomerBean) session.getAttribute("user");
			Double cash = (double) (customerDAO.read(customerBean.getUserName()).getCash() / 100.0);
			returnJson.cash = df2.format(cash);

			PositionBean[] positionList = positionDAO.match(MatchArg.equals("customerId", customerBean.getCustomerId()));

			if (positionList.length == 0) {
				errors.add("Empty fund list.");
				returnJson.Message = "You don't have any funds at this time. ";
				return gson.toJson(returnJson);
			} else {
				for (PositionBean pb : positionList) {
					double shares = ((double) (pb.getShares()) / 1000.0);
					FundBean fb = fundDAO.read(pb.getFundId());
					double price = ((double) (fb.getLatestPrice() / 100.0));
					String name = fb.getName();

					String sharesString = df3.format(shares);
					String priceString = df2.format(price);

					PositionInfo aInfo = new PositionInfo(name, sharesString, priceString);
					list.add(aInfo);
				}
			}
			Transaction.commit();
			returnJson.funds = list.toArray(new PositionInfo[list.size()]);
			return gson.toJson(returnJson);

		} catch (NumberFormatException e) {
			errors.add(e.getMessage());
			returnJson.Message = "I'm sorry, there was a problem viewing the portfolio.";
			return gson.toJson(returnJson.Message);
		} catch (RollbackException e) {
			errors.add(e.getMessage());
			returnJson.Message = "I'm sorry, there was a problem viewing the portfolio.";
			return gson.toJson(returnJson.Message);
		} finally {
			if(Transaction.isActive())
				Transaction.rollback();
		}
	}

	private class ReturnJson {
		String Message = "";
		String cash = "";
		PositionInfo[] funds = {};
	}
}
