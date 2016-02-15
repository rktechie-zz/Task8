package formbean;

import java.util.ArrayList;
import java.util.List;

import org.mybeans.form.FormBean;

public class DepositCheckForm extends FormBean {
	private String username;
	private String cash;

	public String getCash() {
		return cash;
	}

	public void setCash(String depositAmount) {
		this.cash = sanitize(depositAmount);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String userName) {
		this.username = sanitize(userName);
	}

	public boolean isPresent() {
		return true;
	}
	
	public DepositCheckForm () {
	}

	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();
		
		try {
			Double.parseDouble(cash);
		} catch (NumberFormatException e1) {
			errors.add("Amount entered is not a valid Dollar amount. ");

			return errors;
		}
		
		if (Double.parseDouble(cash) > 1000000.0) {
			errors.add("The amount should be less than 1,000,000. ");
		}
		

		if (Double.parseDouble(cash) > 1000000) {
			errors.add("Please enter an amount less than $ 1,000,000. ");
			return errors;
		}
		
		if (Double.parseDouble(cash) <= 0) {
			errors.add("The amount of request should be positive. ");
		}

		if (errors.size() > 0)
			return errors;

		return errors;
	}
	
	private String sanitize(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

}
