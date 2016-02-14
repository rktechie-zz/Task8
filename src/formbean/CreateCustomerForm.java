package formbean;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.mybeans.form.FormBean;

public class CreateCustomerForm extends FormBean{
	private String firstname;
	private String lastname;
	private String username;
	private String password;
	private String addr_line1;
	private String addr_line2;
	private String city;
	private String state;
	private String zip;

	public CreateCustomerForm(){
	}
	
	public CreateCustomerForm(HttpServletRequest request){
		this.firstname = request.getParameter("firstname");
		this.lastname = request.getParameter("lastname");
		this.username = request.getParameter("username");
		this.password = request.getParameter("password");
		this.addr_line1 = request.getParameter("addr_line1");
		this.addr_line2 = request.getParameter("addr_line2");
		this.city = request.getParameter("city");
		this.state = request.getParameter("state");
		this.zip = request.getParameter("zip");
	}
	public boolean isPresent() {
		if(this.firstname == null || this.lastname == null || this.username == null || this.password == null || this.addr_line1 == null || 
				this.addr_line2 == null || this.city == null || this.state == null || this.zip == null )
			return false;
		else return true;
	}
	
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = sanitize(firstname);
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = sanitize(lastname);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = sanitize(username);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = sanitize(password);
	}

	public String getAddr_line1() {
		return addr_line1;
	}

	public void setAddr_line1(String addr_line1) {
		this.addr_line1 = sanitize(addr_line1);
	}

	public String getAddr_line2() {
		return addr_line2;
	}

	public void setaddr_line2(String addr_line2) {
		this.addr_line2 = sanitize(addr_line2);
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = sanitize(city);
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = sanitize(state);
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = sanitize(zip);
	}

	public List<String> getValidationErrors() {
		List<String> errors = new ArrayList<String>();

		if (username == null || username.trim().length() == 0)
			errors.add("User Name is required.");

		if (firstname == null || firstname.trim().length() == 0)
			errors.add("First Name is required.");

		if (lastname == null || lastname.trim().length() == 0)
			errors.add("Last Name is required.");

		if (password == null || password.trim().length() == 0)
			errors.add("Password is required.");

		if (addr_line2 == null || addr_line1.trim().length() == 0)
			errors.add("Address Line 1 is required.");

		if (addr_line2 == null || addr_line2.trim().length() == 0)
			errors.add("Address Line 2 is required.");

		if (city == null || city.trim().length() == 0)
			errors.add("City is required.");

		if (state == null || state.trim().length() == 0)
			errors.add("State is required.");

		return errors;
	}

	private String sanitize(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}

