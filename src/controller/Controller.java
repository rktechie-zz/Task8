package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.genericdao.RollbackException;

import databean.CustomerBean;
import databean.EmployeeBean;
import model.CustomerDAO;
import model.EmployeeDAO;
import model.Model;

/**
 * Servlet implementation class Controller
 */
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	CustomerDAO cDAO;
	EmployeeDAO eDAO;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();
    }
    
	public void init() throws ServletException {
        Model model = new Model(getServletConfig());
        Action.add(new CreateCustomerAction(model));
//        Action.add(new DepositCheckAction(model));
//        Action.add(new RequestCheckAction(model));
}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json = performTheAction(request);
		
		returnJson(json, request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private String performTheAction(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String servletPath = request.getServletPath();
		String action = getActionName(servletPath);
		session.setAttribute("user", new EmployeeBean());
		
		return Action.perform(action, request);
	}

	private void returnJson(String json, HttpServletRequest request, HttpServletResponse response) throws IOException{
		if (json == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getServletPath());
			return;
		}
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.append(json);
			return;
	}

	private String getActionName(String path) {

		int slash = path.lastIndexOf('/');
		return path.substring(slash + 1);
	}	
}
