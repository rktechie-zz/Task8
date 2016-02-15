package formbean;

import java.util.ArrayList;
import java.util.List;

import org.mybeans.form.FormBean;

public class SellFundForm extends FormBean{
	private String 	fundSymbol;
	private String 	numShares;
	
	public String getFundSymbol() {
		return fundSymbol;
	}

	public String getNumShares() {
		return numShares;
	}

	public void setFundSymbol(String name) {
		this.fundSymbol = sanitize(name);
	}

	public void setNumShares(String shares) {
		this.numShares = sanitize(shares);
	}

	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();

		if (fundSymbol == null || fundSymbol.trim().length() == 0)
			errors.add("Fund name is required. ");
		if (numShares == null || !numShares.matches(".*\\d.*"))
			errors.add("Shares should be numeric. ");
		if (numShares != null) {
			if (numShares.trim().length() == 0) {
				errors.add("You should put the number of shares. ");
			} else if (Double.parseDouble(numShares) < 0){
				errors.add("Shares should not be negative. ");
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
