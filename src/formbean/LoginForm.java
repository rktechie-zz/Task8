package formbean;

import java.util.ArrayList;
import java.util.List;

import org.mybeans.form.FormBean;

public class LoginForm extends FormBean {
	public String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String userName) {
		this.username = sanitize(userName);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = sanitize(password);
	}

	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();

		
		if (password == null || password.trim().length() == 0){
			errors.add("Password is required");
		}
		//System.out.println("name in errors:" + userNm);
		if (username == null || username.trim().length() == 0){
			errors.add("Username is required");
		}

		if (errors.size() > 0)
			return errors;
		
		return errors;
	}
	
	private String sanitize(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
