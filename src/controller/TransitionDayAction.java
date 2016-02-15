package controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;

import com.google.gson.Gson;

import databean.EmployeeBean;
import databean.FundBean;
import databean.FundPriceHistoryBean;
import model.FundDAO;
import model.FundPriceHistoryDAO;
import model.Model;

public class TransitionDayAction extends Action {

	private FundDAO fundDAO;
	private FundPriceHistoryDAO fundPriceHistoryDAO;
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	Random random = new Random();
	ReturnJSON returnclass = new ReturnJSON();


	public TransitionDayAction(Model model) {
		model.getCustomerDAO();
		model.getTransactionDAO();
		fundDAO = model.getFundDAO();
		fundPriceHistoryDAO = model.getFundPriceHistoryDAO();
		model.getPositionDAO();
	}

	public String getName() {
		return "transitionDay";
	}

	public String perform(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Gson gson = new Gson();

		sdf.setLenient(false);

		if (session.getAttribute("user") == null) {
			returnclass.Message = "You must log in prior to making this request.";
			return gson.toJson(returnclass);
		}

		if (!(session.getAttribute("user") instanceof EmployeeBean)) {
			returnclass.Message = "I'm sorry you are not authorized to preform that action.";
			return gson.toJson(returnclass);
		}

		try {
			Transaction.begin();
			// read all customers into list
			FundBean[] fundList = fundDAO.match();

			String lastTradingDayString = fundPriceHistoryDAO.getLatestTradingDayDateString();
			Date lastTradingDay= fundPriceHistoryDAO.getLatestTradingDayDate();
			String newTradingDay;
			
			if(lastTradingDay != null){
				System.out.println("checkpoint1");
				Calendar cal = Calendar.getInstance();
				cal.setLenient(false);
				cal.setTime(lastTradingDay);
				cal.add(Calendar.DATE, 1);  // Incrementing 1 day to the last traded day.
				System.out.println("checkpoint2");
				newTradingDay = sdf.format(cal.getTime());
				System.out.println(lastTradingDay);
			} else {
				returnclass.Message = "I'm sorry, you need to create atleast one fund before running Transition Day. ";
				return gson.toJson(returnclass);
			}


			for (FundBean fb : fundList) {
				FundPriceHistoryBean tmpHistBean = new FundPriceHistoryBean();
				tmpHistBean.setFundId(fb.getFundId());
				tmpHistBean.setExecuteDate(newTradingDay);
				double newPrice;
				do{
					int multiplier = random.nextBoolean() ? 1 : -1;
					newPrice = (fb.getLatestPrice() / 100.0) * (1 + multiplier*0.1);
				} while (newPrice < 0.01 || newPrice > 1000);
				long newPriceLong = (long)newPrice*100;
				tmpHistBean.setPrice(newPriceLong);
				fundPriceHistoryDAO.create(tmpHistBean);
				fb.setLatestPrice(newPriceLong);
				fundDAO.update(fb);

			}
			Transaction.commit();
			returnclass.Message = "The fund prices have been recalculated. ";
			return gson.toJson(returnclass); 

		} catch (RollbackException e) {
			returnclass.Message = "I'm sorry, there was a problem recalculating the fund. ";
			return gson.toJson(returnclass);
		} catch (Exception e) {
			returnclass.Message = "I'm sorry, there was a problem parsing dates of funds. " + e.getMessage();
			return gson.toJson(returnclass);
		} finally {
			if (Transaction.isActive()) {
				Transaction.rollback();
			}
		}

	}
}
