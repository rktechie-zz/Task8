package formbean;

import java.util.ArrayList;
import java.util.List;

import org.mybeans.form.FormBean;

public class CreateFundForm extends FormBean{
	private String 	symbol;
	private String 	name;
	private String initial_value;
	
	public String getSymbol() {
		return symbol;
	}
	public String getName() {
		return name;
	}
	
	public String getInitial_value(){
		return initial_value;
	}
	
	public void setInitial_value(String initial_value) {
		this.initial_value = sanitize(initial_value);
	}
	public void setSymbol(String symbol) {
		this.symbol = sanitize(symbol);
	}
	public void setName(String name) {
		this.name = sanitize(name);
	}
	
	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();

		if (name == null || name.trim().length() == 0)
			errors.add("Fund name is required. ");
		if (symbol == null || symbol.length() < 1
				|| symbol.length() >5)
			errors.add("The length of symbol should be between 1~5. ");
		if(initial_value == null || initial_value.trim().length() == 0)
			errors.add("Initial value is required. ");
		if (errors.size() > 0)
			return errors;
		if (name.matches(".*\\d.*")) 
			errors.add("Fund name should not contain number. ");
		
		
		try {
			double d = Double.parseDouble(initial_value);
			// 2 digit allowed!
			int lastDotIndex = initial_value.lastIndexOf(".");
			if (lastDotIndex != -1 && initial_value.substring(lastDotIndex + 1).length() > 2
					&& Integer.parseInt(initial_value.substring(lastDotIndex + 1)) != 0) {
				errors.add("Price format error! Initial Value dollar amount can be upto 2 decimal places only. ");
			} else if (d < 0.01 || d > 1000) {
				errors.add("Initial Value of shares must be between One cent ($0.01) and One thousand ($1,000.00) dollars. ");
			}
		} catch (NumberFormatException e) {
			errors.add("Price format error: Initial value is not a valid Dollar Amount. ");
		}
		
		return errors;
	}
	
	private String sanitize(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
