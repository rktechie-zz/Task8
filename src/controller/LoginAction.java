package controller;

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
import formbean.LoginForm;
import model.CustomerDAO;
import model.EmployeeDAO;
import model.Model;

public class LoginAction extends Action {
	private FormBeanFactory<LoginForm> formBeanFactory = FormBeanFactory.getInstance(LoginForm.class);

	private EmployeeDAO employeeDAO;
	private CustomerDAO customerDAO;

	public LoginAction(Model model) {
		employeeDAO = model.getEmployeeDAO();
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return "login";
	}

	public String perform(HttpServletRequest request) {
		HttpSession session = request.getSession();
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		Gson gson = new Gson();
		ReturnGson returnGson = new ReturnGson();
		
		try {
			
			LoginForm loginForm = formBeanFactory.create(request);
			request.setAttribute("form", loginForm);

			if (!loginForm.isPresent()) {
				returnGson.message = "The username/password combination that you entered is not correct";
				return gson.toJson(returnGson);
			}
			errors.addAll(loginForm.getValidationErrors());
			if ( (errors.size() != 0)) {
				returnGson.message = "The username/password combination that you entered is not correct";
				return gson.toJson(returnGson);
			}
			
			Menu menu = new Menu();
			EmployeeBean eb = employeeDAO.read(loginForm.getUsername());
			if (eb == null) {
				CustomerBean cb = customerDAO.read(loginForm.getUsername());
				if (cb == null) {
					returnGson.message = "The username/password combination that you entered is not correct";
					return gson.toJson(returnGson);
				} else {
					if (!cb.getPassword().equals(loginForm.getPassword())) {
						returnGson.message = "The username/password combination that you entered is not correct";
						return gson.toJson(returnGson);
					}
					returnGson.message = "Welcome " + cb.getFirstName();
					returnGson.menu = menu.customerMenu(); 
					session.setAttribute("user", cb);
					return gson.toJson(returnGson);
				}
			} else {
				if (!eb.getPassword().equals(loginForm.getPassword())) {
					returnGson.message = "The username/password combination that you entered is not correct";
					return gson.toJson(returnGson);
				}
				returnGson.message = "Welcome " + eb.getFirstName();
				returnGson.menu = menu.employeeMenu();
				session.setAttribute("user", eb);
				return gson.toJson(returnGson);
			}
			
		} catch (RollbackException e) {
			errors.add(e.getMessage());
			returnGson.message = "The username/password combination that you entered is not correct";
			return gson.toJson(returnGson);
		} catch (FormBeanException e) {
			errors.add(e.getMessage());
			returnGson.message = "The username/password combination that you entered is not correct";
			return gson.toJson(returnGson);
		}
	}

	private class ReturnGson {
		String message;
		List<Menu> menu;
	}
	
	private class Menu {
		private String link;
		private String function;
		
		public Menu(){}
		
		public Menu (String link, String function) {
			this.link = link;
			this.function = function;
		}
		
		public List<Menu> customerMenu() {
			List<Menu> customerMenu = new ArrayList<Menu>();
			customerMenu.add(new Menu("/viewPortfolio", "View portfolio"));
			customerMenu.add(new Menu("/buyFund", "Buy Fund"));
			customerMenu.add(new Menu("/sellFund", "Sell Fund"));
			customerMenu.add(new Menu("/requestCheck", "Request Check"));
			customerMenu.add(new Menu("/logout", "Logout"));
			
			return customerMenu;
		}
		
		public List<Menu> employeeMenu() {
			List<Menu> employeeMenu = new ArrayList<Menu>();
			employeeMenu.add(new Menu("/createCustomerAccount", "Create a customer account"));
			employeeMenu.add(new Menu("/depositCheck", "Deposit Check"));
			employeeMenu.add(new Menu("/transitionDay", "Transition Day"));
			employeeMenu.add(new Menu("/createFund", "Create Fund"));
			employeeMenu.add(new Menu("/logout", "Logout"));
			
			return employeeMenu;
		}
	}
}
