package model;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;

import databean.CustomerBean;
import databean.TransactionBean;

public class CustomerDAO extends GenericDAO<CustomerBean> {
	public CustomerDAO(ConnectionPool cp, String tableName) throws DAOException {
		super(CustomerBean.class, tableName, cp);
	}

	public CustomerBean read(String userName) {

		try {
			CustomerBean[] arr = this.match(MatchArg.equals("userName", userName));
			if (arr.length == 0) {
				return null;
			} else {
				return arr[0];
			}

		} catch (RollbackException e) {
			return null;
		}

	}

	public void setPassword(String userName, String password) throws RollbackException {
		try {
			Transaction.begin();
			CustomerBean dbUser = read(userName);

			if (dbUser == null) {
				throw new RollbackException("User Name " + userName + " no longer exists");
			}

			dbUser.setPassword(password);

			update(dbUser);
			Transaction.commit();
		} finally {
			if (Transaction.isActive())
				Transaction.rollback();
		}
	}

	public void setBalance(String userName, long amount, String type) throws RollbackException {
		CustomerBean cb = read(userName);
		
		if (cb == null) {
			throw new RollbackException("User Name " + userName + " no longer exists");
		}
		
		if (type.equals("deposit") || type.equals("sell")) {
			cb.setCash(cb.getCash() + amount);	
		} else if (type.equals("request") || type.equals("buy")) {
			cb.setCash(cb.getCash() - amount);
		}
		update(cb);
	}
}
