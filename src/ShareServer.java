import java.rmi.*;

public interface ShareServer extends Remote {
    Long login(String username, String password) throws AuthenticationFailed, RemoteException;
    Share downloadListOfAllShares(Long token) throws AuthenticationFailed, RemoteException;
    void deposit(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException;
    void withdraw(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException, InsufficientFundsException;
    void purchase(Long token, int amount) throws AuthenticationFailed, RemoteException;
    void sell(Long token, int amount) throws AuthenticationFailed, RemoteException;
    ShareHolding downloadListOfAllShareHoldings(Long token) throws AuthenticationFailed, RemoteException;

}
