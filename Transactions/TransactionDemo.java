import java.sql.*;
import java.util.ResourceBundle;

public class TransactionDemo
{
	private static String url;
	private static String usr;
	private static String pass;

	static
	{
		ResourceBundle rb = ResourceBundle.getBundle("Info");
		url = rb.getString("url");
		usr = rb.getString("username");
		pass = rb.getString("password");
	}

	/**
	 * returns number of rows affected - should be two on success
	 */
	private static int transfer(String fromAccNo, String toAccNo, double amount)
	{
		// *INDENT-OFF*
		String sendSqlStr =
			"""
			UPDATE `t_bank_account`
			SET `balance` = `balance` - ?
			WHERE `account_no` = ?
			""";
		String recvSqlStr =
			"""
			UPDATE `t_bank_account`
			SET `balance` = `balance` + ?
			WHERE `account_no` = ?
			""";

		Connection conn = null;
		PreparedStatement psSend = null;
		PreparedStatement psRecv = null;
		int count = 0;
		try
		{
			conn = DriverManager.getConnection(url, usr, pass);
			conn.setAutoCommit(false);

			psSend = conn.prepareStatement(sendSqlStr);
			psSend.setDouble(1, amount);
			psSend.setString(2, fromAccNo);
			count += psSend.executeUpdate();

			// suppose an exception happening here
			String s = null;
			s.trim();
			// if we are on auto-commit,
			// fromAcc will be deducted $amount money
			// but toAcc will not receive it,
			// where the money is gone:
			// fromAcc: 900.000000 -> 800.000000
			// toAcc: 100.000000 -> 100.000000

			psRecv = conn.prepareStatement(recvSqlStr);
			psRecv.setDouble(1, amount);
			psRecv.setString(2, toAccNo);
			count += psRecv.executeUpdate();

			conn.commit();
		}
		catch (SQLException sqlExc)
		{
			sqlExc.printStackTrace();
			try { conn.rollback(); }
			catch (SQLException e) { e.printStackTrace(); }
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try { conn.rollback(); }
			catch (SQLException ee) { ee.printStackTrace(); }
		}
		finally
		{
			if (conn != null)
			{
				try { conn.close(); }
				catch (SQLException e) { e.printStackTrace(); }
			}

			if (psSend != null)
			{
				try { psSend.close(); }
				catch (SQLException e) { e.printStackTrace(); }
			}

			if (psRecv != null)
			{
				try { psRecv.close(); }
				catch (SQLException e) { e.printStackTrace(); }
			}
		}

		return count;

		// *INDENT-ON*
	}

	public static void main(String[] args)
	{
		String fromAccNo = "87654321";
		String toAccNo = "00001111";
		double amount = 100.0;
		String queryBalanceByAccountNoSqlStr = "SELECT `balance` FROM `t_bank_account` WHERE `account_no` = ?";

		ResultSet rs = null;

		try
			(
				Connection conn = DriverManager.getConnection(url, usr, pass);
				PreparedStatement ps = conn.prepareStatement(queryBalanceByAccountNoSqlStr);
			)
		{
			ps.setString(1, fromAccNo);
			rs = ps.executeQuery();
			rs.next();
			double fromAccPreBalance = rs.getDouble("balance");
			rs.close();

			ps.setString(1, toAccNo);
			rs = ps.executeQuery();
			rs.next();
			double toAccPreBalance = rs.getDouble("balance");
			rs.close();

			transfer(fromAccNo, toAccNo, amount);


			ps.setString(1, fromAccNo);
			rs = ps.executeQuery();
			rs.next();
			double fromAccAfterBalance = rs.getDouble("balance");
			rs.close();

			ps.setString(1, toAccNo);
			rs = ps.executeQuery();
			rs.next();
			double toAccAfterBalance = rs.getDouble("balance");
			rs.close();

			System.out.printf("fromAcc: %f -> %f\n", fromAccPreBalance, fromAccAfterBalance);
			System.out.printf("toAcc: %f -> %f\n", toAccPreBalance, toAccAfterBalance);
		}
		catch (SQLException e) { e.printStackTrace(); }
		finally
		{
			if (rs != null)
			{
				try { rs.close(); }
				catch (SQLException e) { e.printStackTrace(); }
			}
		}
	}
}
