package controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.RollbackException;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.Gson;


import databean.CustomerBean;
import databean.EmployeeBean;
import databean.TransactionBean;
import formbean.RequestCheckForm;
import model.CustomerDAO;
import model.Model;
import model.TransactionDAO;

public class RequestCheckAction extends Action {
	private FormBeanFactory<RequestCheckForm> formBeanFactory = FormBeanFactory.getInstance(RequestCheckForm.class);
	private TransactionDAO transactionDAO;
	private CustomerDAO customerDAO;

	public RequestCheckAction(Model model) {
		transactionDAO = model.getTransactionDAO();
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return "requestCheck.do";
	}

	public String perform(HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		HttpSession session = request.getSession();
		Gson gson = new Gson();
		ReturnGson returnGson = new ReturnGson();
		try {
			if (session.getAttribute("user") == null) {
				returnGson.message = "You must log in prior to making this request";
				return gson.toJson(returnGson.message);
			}
			if (session.getAttribute("user") instanceof EmployeeBean) {
				returnGson.message = "I’m sorry you are not authorized to preform that action";
				return gson.toJson(returnGson.message);
			}
			RequestCheckForm form = formBeanFactory.create(request);
			request.setAttribute("form",form);
			
			CustomerBean customerBean = (CustomerBean) session.getAttribute("user");
			Double cash = (double) (customerDAO.read(customerBean.getUserName()).getCash()/100.0);
			DecimalFormat df2 = new DecimalFormat(	"###,##0.00");
			request.setAttribute("avai_cash",df2.format(transactionDAO.getValidBalance(customerBean.getUserName(), cash)));
			
//			if (!form.isPresent()) {
//				return "requestCheck.jsp";
//			}
			
			errors.addAll(form.getValidationErrors());
			if (errors.size() != 0) {
				returnGson.message = "I’m sorry, there was a problem withdrawl the money";
				return gson.toJson(returnGson.message);
			}

			CustomerBean user = (CustomerBean) request.getSession().getAttribute("user");
			String s = form.getRequestAmount();
			double d = Double.parseDouble(s);
			d = d * 100.00;
			long l = (long) d;
			if ((d - l) > 0) {
				returnGson.message = "I’m sorry, there was a problem withdrawl the money";
				return gson.toJson(returnGson.message);
			}
			d = l / 100.00;
			if (d > transactionDAO.getValidBalance(user.getUserName(),customerDAO.read(user.getUserName()).getCash()/100.0)) {
				errors.add("Balance is not enough to proceed the request");
				returnGson.message = "I’m sorry, the amount requested is greater than the balance of your account";
				return gson.toJson(returnGson.message);
			} 
			TransactionBean tBean = new TransactionBean();
			tBean.setCustomerId(user.getCustomerId());
			tBean.setTransactionType("2");
			tBean.setAmount(l);
			tBean.setUserName(user.getUserName());
			transactionDAO.create(tBean);
			request.removeAttribute("form");
			returnGson.message = "The withdrawal was successfully completed";
			return gson.toJson(returnGson.message);
			
		} catch (RollbackException e) {
			errors.add("System roll back");
			returnGson.message = "I’m sorry, there was a problem withdrawl the money";
			return gson.toJson(returnGson.message);
		} catch (FormBeanException e1) {
			errors.add("Form data wrong");
			returnGson.message = "I’m sorry, there was a problem withdrawl the money";
			return gson.toJson(returnGson.message);
		}
	}
	
	private class ReturnGson {
		String message;
	}
}

