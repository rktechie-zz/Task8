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
import databean.EmployeeBean;
import formbean.CreateCustomerForm;
import model.CustomerDAO;
import model.Model;

public class CreateCustomerAction extends Action {
	private FormBeanFactory<CreateCustomerForm> formBeanFactory = FormBeanFactory.getInstance(CreateCustomerForm.class);
	private CustomerDAO customerDAO;

	public CreateCustomerAction(Model model) {
		customerDAO = model.getCustomerDAO();
	}

	public String getName() {
		return "createCustomerAccount";
	}

	public String perform(HttpServletRequest request) {
		HttpSession session = request.getSession();

		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		Gson gson = new Gson();
		ReturnJSON returnclass = new ReturnJSON();

		try {
			if (session.getAttribute("user") != null && session.getAttribute("user") instanceof EmployeeBean) {
				CreateCustomerForm form = formBeanFactory.create(request);

				if (!form.isPresent()) {
					returnclass.Message = "Input Parameters could not be read.";
					return gson.toJson(returnclass);
				}

				errors.addAll(form.getValidationErrors());
				if (errors.size() > 0) {
					for(String output : errors) returnclass.Message += output;
					return gson.toJson(returnclass);
				}

				if (customerDAO.read(form.getUsername()) != null) {
					errors.add("I'm sorry, there was a problem creating the account.");
					for(String output : errors) returnclass.Message += output;
					return gson.toJson(returnclass);
				}
				Transaction.begin();
				CustomerBean newUser = new CustomerBean();
				newUser.setUserName(form.getUsername());
				newUser.setLastName(form.getLastname());
				newUser.setFirstName(form.getFirstname());
				newUser.setPassword(form.getPassword());
				newUser.setAddress1(form.getAddr_line1());
				newUser.setAddress2(form.getAddr_line2());
				newUser.setCity(form.getCity());
				newUser.setState(form.getState());
				newUser.setZipcode(form.getZip());
				newUser.setCash(0);

				customerDAO.create(newUser);
				Transaction.commit();

				returnclass.Message = "The account has been successfully created.";
				return gson.toJson(returnclass);
			} else {
				returnclass.Message = "I'm sorry you are not authorized to preform that action";
				return gson.toJson(returnclass);
			}
		} catch (NumberFormatException e) {
			//SHOULD NOT REACH HERE, Cash Field is Hardcoded to 0.
			returnclass.Message = "Number Format Exception.";
			return gson.toJson(returnclass);
		}catch (RollbackException e) {
			returnclass.Message = "I'm sorry, there was a problem creating the account. Issue in insertion of record in database.";
			return gson.toJson(returnclass);
		} catch (FormBeanException e) {
			returnclass.Message = "I'm sorry, there was a problem creating the account. Issue in reading the Form.";
			return gson.toJson(returnclass);
		}
	}
}

class ReturnJSON {
	String Message = "";
}

