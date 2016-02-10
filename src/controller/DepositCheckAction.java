package controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.Gson;

import databean.CustomerBean;
import databean.TransactionBean;
import formbean.DepositCheckForm;
import model.CustomerDAO;
import model.Model;
import model.TransactionDAO;

public class DepositCheckAction extends Action {
	private FormBeanFactory<DepositCheckForm> formBeanFactory = FormBeanFactory.getInstance(DepositCheckForm.class);
	private TransactionDAO transactionDAO;
	private CustomerDAO customerDAO;
	Gson gson;

	public DepositCheckAction(Model model) {
		transactionDAO = model.getTransactionDAO();
		customerDAO = model.getCustomerDAO();
		gson = new Gson();
		
	}

	public String getName() {
		return "depositCheck.do";
	}
	
	public String perform(HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		HttpSession session = request.getSession();
		
		try {
			DepositCheckForm depisitCheckForm = formBeanFactory.create(request);
			request.setAttribute("form",depisitCheckForm);
			if (session.getAttribute("user") == null) {
				ReturenGson returnGson = new ReturnGson();
				returnGson.message = "You must log in prior to making this request";
				return gson.toJson(returnGson.message);
			}
			if (session.getAttribute("user") instanceof CustomerBean) {
				ReturenGson returnGson = new ReturnGson();
				returnGson.message = "I’m sorry you are not authorized to preform that action";
				return gson.toJson(returnGson.message);
			}
			
//			if (!depisitCheckForm.isPresent()) {
//				return "depositCheck.jsp";
//			}
			errors.addAll(depisitCheckForm.getValidationErrors());
			if (errors.size() != 0) {
				ReturenGson returnGson = new ReturnGson();
				returnGson.message = "I’m sorry, there was a problem depositing the money";
				return gson.toJson(returnGson.message);
			}
			
			String s =  depisitCheckForm.getDepositAmount();
			CustomerBean customerBean = customerDAO.read(depisitCheckForm.getUserName());
			double d = Double.parseDouble(s);
			d = d * 100.0;
			long l = (long) d;
			
			if (customerBean == null) {
				errors.add("No such user! ");
				ReturenGson returnGson = new ReturnGson();
				returnGson.message = "I’m sorry, there was a problem depositing the money";
				return gson.toJson(returnGson.message);
			}
			if ((d - l) > 0) {
				errors.add("We only allow at most two decimal places");
				ReturenGson returnGson = new ReturnGson();
				returnGson.message = "I’m sorry, there was a problem depositing the money";
				return gson.toJson(returnGson.message); 
			}
			Transaction.begin();
			TransactionBean tBean = new TransactionBean();
			tBean.setCustomerId(customerBean.getCustomerId());
			tBean.setTransactionType("1");
			tBean.setAmount(l);
			tBean.setUserName(customerBean.getUserName());
			transactionDAO.create(tBean);
			Transaction.commit();
			request.removeAttribute("form");
			ReturenGson returnGson = new ReturnGson();
			returnGson.message = "The account has been successfully updated";
			return gson.toJson(returnGson.message);
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			errors.add(e.getMessage());
			ReturenGson returnGson = new ReturnGson();
			returnGson.message = "I’m sorry, there was a problem depositing the money";
			return gson.toJson(returnGson.message);
		} catch (FormBeanException e) {
			// TODO Auto-generated catch block
			errors.add(e.getMessage());
			ReturenGson returnGson = new ReturnGson();
			returnGson.message = "I’m sorry, there was a problem depositing the money";
			return gson.toJson(returnGson.message);
		}
		
		private class ReturnGson {
			String message;
		}
	}

}
