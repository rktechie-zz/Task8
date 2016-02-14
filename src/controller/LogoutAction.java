package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import model.CustomerDAO;
import model.EmployeeDAO;
import model.Model;

public class LogoutAction extends Action{
	private EmployeeDAO employeeDAO;
	private CustomerDAO customerDAO;
	
	public LogoutAction(Model model) {
		employeeDAO = model.getEmployeeDAO();
		customerDAO = model.getCustomerDAO();
	}
	
	public String getName() {
		return "logout";
	}
	
	public String perform(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		session.setAttribute("user", null);
		session.invalidate();

		Gson gson = new Gson();
		ReturnGson returnGson = new ReturnGson();
		returnGson.message = "You’ve been logged out";
		
		return gson.toJson(returnGson);
	}
	
	private class ReturnGson {
		String message;
	}
}