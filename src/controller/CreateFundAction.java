package controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import com.google.gson.Gson;

import databean.EmployeeBean;
import databean.FundBean;
import formbean.CreateFundForm;
import model.FundDAO;
import model.Model;

public class CreateFundAction extends Action{
        private FormBeanFactory<CreateFundForm> formBeanFactory = FormBeanFactory.getInstance(CreateFundForm.class);
        
        private FundDAO fundDAO; 
        
        public CreateFundAction(Model model) {
                fundDAO = model.getFundDAO();
        }
        
        @Override
        public String getName() {
                return "createFund";
        }

        @Override
        public String perform(HttpServletRequest request) {
                HttpSession session = request.getSession();
                List<String> errors = new ArrayList<String>();
                request.setAttribute("errors", errors);
                
                Gson gson = new Gson();
                ReturnGson returnGson = new ReturnGson();
                
                if (session.getAttribute("user") == null) {
                        errors.add("Not log in before making the request");
                        returnGson.message = "You must log in prior to making this request";
                        return gson.toJson(returnGson.message);                  
                }
                
                if (! (session.getAttribute("user") instanceof EmployeeBean)) {
                        errors.add("Not an authorized employee");
                        returnGson.message = "I’m sorry you are not authorized to preform that action";
                        return gson.toJson(returnGson.message);    
                }
        
                try {
                        CreateFundForm createFundForm = formBeanFactory.create(request);
                        
                        if (!createFundForm.isPresent()) {
                                errors.add("Input Parameters could not be read");
                                returnGson.message = "I’m sorry, there was a problem creating the fund";
                                return gson.toJson(returnGson.message);
                        }
                        
                        FundBean fundBeanExist = fundDAO.read(createFundForm.getName());
                        if (fundBeanExist != null) {
                                errors.add("Fund already exists!");
                                returnGson.message = "I’m sorry, there was a problem creating the fund";
                                return gson.toJson(returnGson.message);
                        }
                        if (fundBeanExist == null && fundDAO.match(MatchArg.equalsIgnoreCase("name", createFundForm.getName() )).length != 0 ){
                                errors.add("Fund already exists! Check case of Fund name typed");
                                returnGson.message = "I’m sorry, there was a problem creating the fund";
                                return gson.toJson(returnGson.message);
                        }
                        
                        errors.addAll(createFundForm.getValidationErrors());
                        if (errors.size() != 0) {
                                returnGson.message = "I’m sorry, there was a problem creating the fund";
                                return gson.toJson(returnGson.message);
                        }
                        
                        try {
                                Transaction.begin();
                                
                                FundBean fundBean = new FundBean();
                                fundBean.setName(createFundForm.getName());
                                fundBean.setSymbol(createFundForm.getSymbol());
                                fundDAO.create(fundBean);
                                
                                Transaction.commit();

                                returnGson.message = "The fund has been successfully created";
                                return gson.toJson(returnGson.message);
                        } finally {
                                if (Transaction.isActive()) {
                                        Transaction.rollback();
                                }
                        }
                } catch (RollbackException e) {
                        errors.add(e.getMessage());
                        returnGson.message = "I’m sorry, there was a problem creating the fund";
                        return gson.toJson(returnGson.message);
                } catch (FormBeanException e) {
                        errors.add(e.getMessage());
                        returnGson.message = "I’m sorry, there was a problem creating the fund";
                        return gson.toJson(returnGson.message);
                }
        }
}

class ReturnGson {
        String message;
}
