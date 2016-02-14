package controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.FundDAO;
import model.FundPriceHistoryDAO;
import model.Model;
import model.PositionDAO;
import model.TransactionDAO;
import model.CustomerDAO;

import org.genericdao.MatchArg;
import org.genericdao.RollbackException;

import com.google.gson.Gson;

import databean.CustomerBean;
import databean.PositionBean;
import databean.PositionInfo;

public class ViewPortfolioAction extends Action {
        private FundDAO fundDAO;
        private PositionDAO positionDAO;
        private FundPriceHistoryDAO historyDAO;
        private TransactionDAO transactionDAO;
        private CustomerDAO customerDAO;

        public ViewPortfolioAction(Model model) {
                fundDAO = model.getFundDAO();
                positionDAO = model.getPositionDAO();
                historyDAO = model.getFundPriceHistoryDAO();
                transactionDAO = model.getTransactionDAO();
                customerDAO = model.getCustomerDAO();
        }

        public String getName() {
                return "viewPortfolio";
        }

        public String perform(HttpServletRequest request) {
                List<String> errors = new ArrayList<String>();
                request.setAttribute("errors", errors);
                HttpSession session = request.getSession();
                Gson gson = new Gson();
                ReturnJson returnJson = new ReturnJson();

                try {
                        if (session.getAttribute("user") == null) {
                                returnJson.message = "You must log in prior to making this request";
                                return gson.toJson(returnJson.message);
                        }

                        if (!(session.getAttribute("user") instanceof CustomerBean)) {
                                errors.add("Not an authorized customer");
                                returnJson.message = "I’m sorry you are not authorized to preform that action";
                                return gson.toJson(returnJson.message);
                        }

                        DecimalFormat df3 = new DecimalFormat("#,##0.000");
                        DecimalFormat df2 = new DecimalFormat("###,##0.00");
                        
                        CustomerBean customerBean = (CustomerBean) session.getAttribute("user");
                        Double cash = (double) (customerDAO.read(customerBean.getUserName()).getCash() / 100);
                        DecimalFormat df4 = new DecimalFormat("###,##0.00");
                        returnJson.cash = df4.format(transactionDAO.getValidBalance(customerBean.getUserName(), cash));
                        
                        PositionBean[] positionList = positionDAO
                                        .match(MatchArg.equals("customerId", customerBean.getCustomerId()));

                        if (positionList == null) {
                                errors.add("Empty fund list.");
                                returnJson.message = "“You don’t have any funds at this time.";
                                return gson.toJson(returnJson.message);
                        } else {
                                List<PositionInfo> positionInfoList = new ArrayList<PositionInfo>();
                                for (PositionBean a : positionList) {
                                        double shares = ((double) (a.getShares()) / 1000.0);
                                        double price = ((double) (historyDAO.getLatestFundPrice(a.getFundId())
                                                        .getPrice() / 100.0));
                                        String name = fundDAO.read(a.getFundId()).getName();

                                        String sharesString = df3.format(shares);
                                        String priceString = df2.format(price);

                                        PositionInfo aInfo = new PositionInfo(name, sharesString, priceString);
                                        positionInfoList.add(aInfo);
                                        returnJson.funds = positionInfoList
                                                        .toArray(new PositionInfo[positionInfoList.size()]);
                                        return gson.toJson(returnJson.funds);
                                }
                        }

                        return gson.toJson(returnJson.cash);
                } catch (NumberFormatException e) {
                        errors.add(e.getMessage());
                        returnJson.message = "I’m sorry, there was a problem viewing the portfolio.";
                        return gson.toJson(returnJson.message);
                } catch (RollbackException e) {
                        errors.add(e.getMessage());
                        returnJson.message = "I’m sorry, there was a problem viewing the portfolio.";
                        return gson.toJson(returnJson.message);
                }
        }

        private class ReturnJson {
                String message;
                String cash;
                PositionInfo[] funds;
        }
}
