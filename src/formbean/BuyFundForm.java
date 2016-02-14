package formbean;

import java.util.ArrayList;
import java.util.List;

import org.mybeans.form.FormBean;

public class BuyFundForm extends FormBean{
	private String 	fundSymbol;
	private String 	cashValue;
	
	public String getFundSymbol() {
		return fundSymbol;
	}

	public String getCashValue() {
		return cashValue;
	}

	public void setFundSymbol(String name) {
		this.fundSymbol = sanitize(name);
//		this.name = name;
	}

	public void setCashValue(String amount) {
		this.cashValue = sanitize(amount);
//		this.amount = amount;
	}

	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();

		if (fundSymbol == null || fundSymbol.trim().length() == 0)
			errors.add("Fund name is required. ");
		if (cashValue == null || !cashValue.matches(".*\\d.*")) 
			errors.add("Amount should be numeric. ");
		if (cashValue != null) {
			if (cashValue.trim().length() == 0) {
				errors.add("You should put the amount. ");
			} else if (Double.parseDouble(cashValue) < 0){
				errors.add("Amount should not be negative. ");
			}
		}

		if (errors.size() > 0)
			return errors;
		
		return errors;
	}
	
	private String sanitize(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
