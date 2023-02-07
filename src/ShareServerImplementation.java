import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;

public class ShareServerImplementation implements ShareServer {

    private String login = new String("login123");
    private String password = new String("password123");
    private static long token;
    private float userFunds = 0.0f;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    HashMap<String, Share> listOfAllShares = new HashMap<String, Share>();
    HashMap<String, ShareHolding> listOfUserOwnedShares = new HashMap<String, ShareHolding>();

    // check the user balance
    @Override
    public float checkCurrentBalance(Long token) throws RemoteException, AuthenticationFailed {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        return userFunds;
    }

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
            scheduler();
            System.out.println("Share Server has started");
        } catch (Exception exception) {
            System.out.println("Exception in Share Server");
            exception.printStackTrace();
        }
    }

    // generate a token if the user credentials are correct
    @Override
    public Long login(String login, String password) throws AuthenticationFailed, RemoteException {
        if(this.login.equals(login) && this.password.equals(password)) {
            return token;
        } else {
            throw new AuthenticationFailed("Provided login or password does not match our records.");
        }
    }

    // check for exceptions and pass the list of all shares to the user
    @Override
    public HashMap<String, Share> downloadListOfAllShares(Long token) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(listOfAllShares.isEmpty()) {
            throw new ShareDoesNotExistException("Currently there are no shares available to buy on the market!");
        }
        return listOfAllShares;
    }

    // check for exceptions and deposit money to the user account
    @Override
    public void deposit(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(amount <= 0.0f) {
            throw new NegativeNumberException("The amount you wish to deposit needs to be greater than zero!");
        }
        userFunds += amount;
    }

    // check for exceptions and withdraw money from the user account

    @Override
    public void withdraw(Long token, float amount) throws AuthenticationFailed, RemoteException, NegativeNumberException, InsufficientFundsException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(amount < 0.0f) {
            throw new NegativeNumberException("The amount you wish to withdraw needs to be greater than zero!");
        }
        if(amount > userFunds) {
            throw new InsufficientFundsException("Your account balance is too low to make this withdraw this amount of money!");
        }
        userFunds -= amount;
    }

    // check for exceptions and carry out a purchase of shares
    @Override
    public void purchase(Long token, String shareName, int amount) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException, NegativeNumberException, NotEnoughSharesAvailable, InsufficientFundsException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(!(listOfAllShares.containsKey(shareName))) {
            throw new ShareDoesNotExistException("There are currently not shares with this name on the market!");
        }
        if(amount <= 0) {
            throw new NegativeNumberException("Cannot specify a negative number of shares to purchase!");
        }
        if(listOfAllShares.get(shareName).getVolumeOfSharesAvailable() < amount) {
            throw new NotEnoughSharesAvailable("The specified amount exceeds the number of shares that can be bought for this company on the market.");
        }
        if(listOfAllShares.get(shareName).getSharePrice() > userFunds*amount) {
            throw new InsufficientFundsException("Your account balance is too low to buy this amount of shares!");
        }

        // check if the user has already bought some shares from the company
        // if no, create a new shareholding for this company
        // if yes, overwrite the shareholding with updated number of shares and price at the same key
        Share temp = listOfAllShares.get(shareName);
        if(listOfUserOwnedShares.get(shareName) == null) {
            listOfUserOwnedShares.put(shareName, new ShareHoldingImplementation(temp.getShareName(), amount, temp.getSharePrice()));
        } else {
            ShareHolding temp2 = listOfUserOwnedShares.get(shareName);
            listOfUserOwnedShares.replace(shareName, new ShareHoldingImplementation(temp2.getShareHoldingName(),temp2.getNumberOfOwnedShareHoldings()+amount, temp.getSharePrice()));
        }
        // check if user decided to buy all shares
        // remove the share from the market if the user bought all shares for this company
        // otherwise adjust the share object to reflect the deducted amount of shares
        if((temp.getVolumeOfSharesAvailable() - amount) != 0) {
            listOfAllShares.replace(shareName, new ShareImplementation(temp.getShareName(),temp.getVolumeOfSharesAvailable()-amount, temp.getSharePrice()));
        } else {
            listOfAllShares.remove(shareName);
        }
        userFunds -= temp.getSharePrice()*amount;
    }

    // check for exceptions and sell shares
    public void sell(Long token, String shareName, int amount) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException, NegativeNumberException, NotEnoughSharesAvailable {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(!(listOfUserOwnedShares.containsKey(shareName))) {
            throw new ShareDoesNotExistException("You do not own shares from this company!");
        }
        if(amount <= 0) {
            throw new NegativeNumberException("Cannot specify a negative number of shares to sell!");
        }
        if(listOfUserOwnedShares.get(shareName).getNumberOfOwnedShareHoldings() < amount) {
            throw new NotEnoughSharesAvailable("The specified amount exceeds the number of shares that you own for this company!");
        }
        // check if the user has already bought some shares from the company
        // if no, create a new shareholding for this company
        // if yes, overwrite the shareholding with updated number of shares and price at the same key
        ShareHolding temp = listOfUserOwnedShares.get(shareName);
        if(listOfAllShares.get(shareName) == null) {
            listOfAllShares.put(shareName, new ShareImplementation(shareName, amount, temp.getShareHoldingPrice()));
        } else {
            Share temp2 = listOfAllShares.get(shareName);
            listOfAllShares.replace(shareName, new ShareImplementation(shareName, temp2.getVolumeOfSharesAvailable()+amount, temp.getShareHoldingPrice()));
        }
        // check if user decided to sell all shares
        // remove the share from user shares list if the user sold all shares for this company
        // otherwise adjust the share object to reflect the deducted amount of shares
        if((temp.getNumberOfOwnedShareHoldings() - amount) != 0) {
            listOfUserOwnedShares.replace(shareName, new ShareHoldingImplementation(shareName, temp.getNumberOfOwnedShareHoldings()-amount, temp.getShareHoldingPrice()));
        } else {
            listOfUserOwnedShares.remove(shareName);
        }
        userFunds += temp.getShareHoldingPrice()*amount;

    }
    // check for exceptions and pass the user the list of all shares that he owns
    @Override
    public HashMap<String, ShareHolding> downloadListOfAllShareHoldings(Long token) throws AuthenticationFailed, RemoteException, ShareDoesNotExistException {
        if(!(token.equals(this.token)) || token == null) {
            throw new AuthenticationFailed("Your session has expired!");
        }
        if(listOfUserOwnedShares.isEmpty()) {
            throw new ShareDoesNotExistException("You do not own any shares at the moment!");
        }
        return listOfUserOwnedShares;
    }

    // read data from the Shares.csv file
    private void readShareData() {
        try (BufferedReader br = new BufferedReader(new FileReader("Shares.csv"))) {
            String line;
            String delimiter = ",";
            String[] data;
            while ((line = br.readLine()) != null) {
                data = line.split(delimiter);
                listOfAllShares.put(data[0], new ShareImplementation(data[0], Integer.parseInt(data[1]), Float.parseFloat(data[2])));
            }
         }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    // set up a runnable method to generate a new random token after 5 minutes pass
    private static void scheduler() {
        final Runnable tokenTimeout = new Runnable() {
            @Override
            public void run() {
                token = new Random().nextLong();
            }
        };

        // invoke the method every 5 minutes
        scheduler.scheduleAtFixedRate(tokenTimeout, 0, 5, MINUTES);

    }

}
