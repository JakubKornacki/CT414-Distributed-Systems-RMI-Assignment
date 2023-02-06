import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class ShareServerImplementation implements ShareServer{

    private String login = new String("login123");
    private String password = new String("password123");
    private long token;
    private float userFunds = 0.0f;
    private ArrayList<Share> listOfShares = new ArrayList<Share>();




    public ShareServerImplementation() throws RemoteException {
        super();
        readShareData();
    }

    public static void main(String args[]) {
        try {
            if(System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
                System.out.println("Setting the new security manager");
            }

            String serverName = "ShareServer";
            // create the share server stub and the share
            ShareServer shareServer = new ShareServerImplementation();
            ShareServer stub = (ShareServer) UnicastRemoteObject.exportObject(shareServer, 0);
            // use the default port for rmi registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(serverName, stub);
            System.out.println("Share Server has started");
        } catch (Exception exception) {
            System.out.println("Exception in Share Server");
            exception.printStackTrace();
        }
    }


    @Override
    public Long login(String login, String password) throws AuthenticationFailed, RemoteException {
        if(this.login.equals(login) && this.password.equals(password)) {
            token = new Random().nextLong();
            return token;
        } else {
            throw new AuthenticationFailed("Provided login or password does not match our records.");
        }

    }


    @Override
    public Share downloadListOfAllShares(Long token) throws AuthenticationFailed, RemoteException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }
        return null;
    }


    @Override
    public void deposit(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }
        if(amount <= 0.0f) {
            throw new NegativeNumberException("The amount you wish to deposit needs to be greater than zero!");
        }
        userFunds += amount;
    }


    @Override
    public void withdraw(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException, InsufficientFundsException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }
        if(amount < 0.0f) {
            throw new NegativeNumberException("The amount you wish to withdraw needs to be greater than zero!");
        }
        if(amount > userFunds) {
            throw new InsufficientFundsException("Your account balance is too low to make this withdraw this amount of money!");
        }
        userFunds -= amount;
    }

    @Override
    public void purchase(Long token, int amount) throws AuthenticationFailed, RemoteException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }
    }


    @Override
    public void sell(Long token, int amount) throws AuthenticationFailed, RemoteException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }
    }
    @Override
    public ShareHolding downloadListOfAllShareHoldings(Long token) throws AuthenticationFailed, RemoteException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Authentication failed or sessions has expired!");
        }

        return null;
    }

    private void readShareData() {
        try (BufferedReader br = new BufferedReader(new FileReader("book.csv"))) {
            String line;
            String delimiter = ",";
            String[] data;
            while ((line = br.readLine()) != null) {
                data = line.split(delimiter);
            }

         }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
