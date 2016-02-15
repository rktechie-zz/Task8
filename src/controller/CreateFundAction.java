package controller;

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

import databean.EmployeeBean;
import databean.FundBean;
import databean.FundPriceHistoryBean;
import formbean.CreateFundForm;
import model.FundDAO;
import model.FundPriceHistoryDAO;
import model.Model;

public class CreateFundAction extends Action {
	private FormBeanFactory<CreateFundForm> formBeanFactory = FormBeanFactory.getInstance(CreateFundForm.class);
	private FundDAO fundDAO;
	private FundPriceHistoryDAO fundPriceHistoryDAO;

	public CreateFundAction(Model model) {
		fundDAO = model.getFundDAO();
		fundPriceHistoryDAO = model.getFundPriceHistoryDAO();
	}

	@Override
	public String getName() {
		return "createFund";
	}

	@Override
	public String perform(HttpServletRequest request) {
		HttpSession session = request.getSession();
		List<String> errors = new ArrayList<String>();

		Gson gson = new Gson();
		ReturnGson returnGson = new ReturnGson();

		if (session.getAttribute("user") == null) {
			returnGson.Message = "You must log in prior to making this request";
			return gson.toJson(returnGson);
		}

		if (!(session.getAttribute("user") instanceof EmployeeBean)) {
			returnGson.Message = "I'm sorry you are not authorized to preform that action. ";
			return gson.toJson(returnGson);
		}

		try {
			CreateFundForm createFundForm = formBeanFactory.create(request);

			if (!createFundForm.isPresent()) {
				errors.add("I'm sorry, there was a problem creating the fund: Input Parameters missing. ");
				for(String output : errors) returnGson.Message += output;
				return gson.toJson(returnGson);
			}

			errors.addAll(createFundForm.getValidationErrors());
			if (errors.size() != 0) {
				returnGson.Message = "I'm sorry, there was a problem creating the fund. ";
				for(String output : errors) returnGson.Message += output;
				return gson.toJson(returnGson);
			}


			try {
				Transaction.begin();

				FundBean fundBeanExist = fundDAO.read(createFundForm.getName());
				if (fundBeanExist != null) {
					errors.add("I'm sorry, there was a problem creating the fund: Fund already exists. ");
					for(String output : errors) returnGson.Message += output;
					return gson.toJson(returnGson);
				}
				if (fundBeanExist == null
						&& fundDAO.match(MatchArg.equalsIgnoreCase("name", createFundForm.getName())).length != 0) {
					errors.add("I'm sorry, there was a problem creating the fund: Fund already exists! Check case of Fund name typed. ");
					for(String output : errors) returnGson.Message += output;
					return gson.toJson(returnGson);
				}
				Double init_value = Double.parseDouble(createFundForm.getInitial_value());
				FundBean fundBean = new FundBean();
				fundBean.setName(createFundForm.getName());
				fundBean.setSymbol(createFundForm.getSymbol());
				fundBean.setLatestPrice((long)(init_value*100));
				fundDAO.create(fundBean);
				FundPriceHistoryBean fundHistoryBean = new FundPriceHistoryBean();
				Date currDate = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
				dateFormat.setLenient(false);
				String currDateString = dateFormat.format(currDate);
				fundHistoryBean.setExecuteDate(currDateString);
				int fundId = fundDAO.read(createFundForm.getName()).getFundId();
				fundHistoryBean.setFundId(fundId);
				fundHistoryBean.setPrice((long)(init_value*100));
				fundPriceHistoryDAO.create(fundHistoryBean);

				Transaction.commit();

				returnGson.Message = "The fund has been successfully created. ";
				return gson.toJson(returnGson); 
			} catch (RollbackException e) {
				errors.add(e.getMessage());
				returnGson.Message = "I'm sorry, there was a problem creating the fund. ";
				return gson.toJson(returnGson);
			} finally {
				if (Transaction.isActive()) {
					Transaction.rollback();
				}
			}
		} catch (FormBeanException e) {
			errors.add(e.getMessage());
			returnGson.Message = "I'm sorry, there was a problem creating the fund. ";
			return gson.toJson(returnGson);
		} 

	}
}

class ReturnGson {
	String Message = "";
}
