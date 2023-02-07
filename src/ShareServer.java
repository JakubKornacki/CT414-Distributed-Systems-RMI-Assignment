import java.rmi.*;
import java.util.HashMap;

public interface ShareServer extends Remote {
    Long login(String username, String password) throws AuthenticationFailed, RemoteException;
    HashMap<String, Share> downloadListOfAllShares(Long token) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException;
    void deposit(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException;
    void withdraw(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException, InsufficientFundsException;
    void purchase(Long token, String shareName, int amount) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException, NegativeNumberException, NotEnoughSharesAvailable, InsufficientFundsException;
    void sell(Long token, String shareName, int amount) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException, NegativeNumberException, NotEnoughSharesAvailable;
    HashMap<String, ShareHolding> downloadListOfAllShareHoldings(Long token) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException;
    float checkCurrentBalance(Long token) throws RemoteException, AuthenticationFailed;

}
