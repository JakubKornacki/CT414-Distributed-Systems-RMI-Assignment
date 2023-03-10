import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    private long token;

    private ShareServer shareServer;
    private Scanner scanner = new Scanner(System.in);
    private HashMap<String, Share> listOfAllShares;
    private HashMap<String, ShareHolding> listOfSharesOwned;


    public Client (ShareServer shareServer) {
        this.shareServer = shareServer;
        signIn();
    }

    public static void main(String args[]) {
        // if the security manager is not set for this project set it with a new one
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String serverName = "ShareServer";
            // use the default port for rmi registry
            Registry registry = LocateRegistry.getRegistry();
            // lookup the registry and find the share server object
            ShareServer shareServer = (ShareServer) registry.lookup(serverName);
            System.out.println("Client has started");
            new Client(shareServer);
        } catch(Exception exception) {
            System.out.println("Exception in client");
            exception.printStackTrace();
        }
    }

    public void signIn() {
        // ask the user for login and password
      String login, password;
      while(true) {
          System.out.println("Enter your login");
          login = scanner.next();
          System.out.println("Enter your password");
          password = scanner.next();
          // attempt to sign in to obtain the token
          try {
              token = shareServer.login(login, password);
              break;
          } catch (AuthenticationFailed e) {
              System.out.println(e.getMessage());
          } catch (RemoteException e) {
              e.printStackTrace();
              return;
          }
      }
      mainMenu();
    }

    public void mainMenu(){
        int input;
        System.out.println("Welcome to the main menu.");
        System.out.println("Select the following options to your liking:\n1 - Deposit\n2 - Withdraw\n3 - Purchase\n4 - Sell\n5 - Download the list of all shares\n6 - Download the list of all your share holdings\n7 - Check your balance\n8 - Exit application");
        try {
            // different options available to the user
        while(true){
            input = scanner.nextInt();
            switch (input) {
                case 1:
                    deposit();
                    break;
                case 2:
                    withdraw();
                    break;
                case 3:
                    purchase();
                    break;
                case 4:
                    sell();
                    break;
                case 5:
                    printOutListOfShares();
                    break;
                case 6:
                    printOutListOfOwnedShares();
                    break;
                case 7:
                    checkUserBalance();
                    break;
                case 8:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unsupported action, please retry.");
            }
        }
        } catch (InputMismatchException e) {
            System.err.println(e.getMessage());
        } finally {
            return;
        }
    }
    // attempt to get the current user balance from the server
    public void checkUserBalance() {
        try {
            float balance = shareServer.checkCurrentBalance(token);
            System.out.println("Your current balance is: " + balance);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } finally {
            return;
        }
    }

    // attempt to sell the stock passed into the server by the user
    public void sell(){
        System.out.println("Please enter the name of the share you want to sell.");
        String shareName = scanner.next();
        System.out.println("Please enter the number of shares you want to sell.");
        int amount = scanner.nextInt();
        try {
            shareServer.sell(token, shareName, amount);
            System.out.println("Share sucessfully sold!.");
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        } catch(ShareDoesNotExistException e) {
            System.out.println(e.getMessage());
        } catch(NotEnoughSharesAvailable e) {
            System.out.println(e.getMessage());
        } catch (InputMismatchException e) {
        System.err.println(e.getMessage());
        }
        finally {
            return;
        }
    }
    // attempt to purchase the stock passed into the server by the user
    public void purchase() {
        System.out.println("Please enter the name of the share you want to buy.");
        String shareName = scanner.next();
        System.out.println("Please enter the number of shares you want to buy.");
        int amount = scanner.nextInt();
        try {
            shareServer.purchase(token, shareName, amount);
            System.out.println("Share sucessfully bought!.");
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        } catch(ShareDoesNotExistException e) {
            System.out.println(e.getMessage());
        } catch(NotEnoughSharesAvailable e) {
            System.out.println(e.getMessage());
        } catch (InputMismatchException e) {
            System.err.println(e.getMessage());
        }
        finally {
            return;
        }

    }

    // attempt to download the list of all shared owned by the user
    public void downloadListOfOwnedShares() {
        System.out.println("The list of owned shares is being downloaded... please wait.\n");
        try {
            listOfSharesOwned = shareServer.downloadListOfAllShareHoldings(token);
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ShareDoesNotExistException e) {
            System.out.println(e.getMessage());
        } finally {
            return;
        }
    }

    // download the list of all available shares from the server and
    public void downloadListOfAvailableShares(){
        System.out.println("The list of available shares is being downloaded... please wait.\n");
        try {
            listOfAllShares = shareServer.downloadListOfAllShares(token);
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ShareDoesNotExistException e) {
            System.out.println(e.getMessage());
        } finally {
            return;
        }
    }

    // download and print the list of all shares
    private void printOutListOfShares(){
        downloadListOfAvailableShares();
        if(listOfAllShares != null) {
            for (String key : listOfAllShares.keySet()) {
                Share temp = listOfAllShares.get(key);
                System.out.println("Share name: " + temp.getShareName());
                System.out.println("Share price: " + String.format("%.2f", temp.getSharePrice()));
                System.out.println("Shares available on the market: " + temp.getVolumeOfSharesAvailable());
                System.out.println("Time left for share price update: " + temp.getTimeRemainingToPriceUpdate() + " seconds");
                System.out.println("\n");
            }
        }
    }
    // download and print the list of all owned shares
    private void printOutListOfOwnedShares() {
        downloadListOfOwnedShares();
        if(listOfSharesOwned != null) {
            for (String key : listOfSharesOwned.keySet()) {
                ShareHolding temp = listOfSharesOwned.get(key);
                System.out.println("Share name: " + temp.getShareHoldingName());
                System.out.println("Share price: " + String.format("%.2f", temp.getShareHoldingPrice()));
                System.out.println("Number of owned shares: " + temp.getNumberOfOwnedShareHoldings());
                System.out.println("\n");
            }
        }
    }
    // attempt to deposit money from the user account
    public void withdraw() {
        System.out.println("Enter the amount you want to withdraw");
        float amount = scanner.nextFloat();
        try {
            shareServer.withdraw(token, amount);
            checkUserBalance();
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        } finally {
            return;
        }

    }

    // attempt to deposit money to the user account
    public void deposit() {
        System.out.println("Enter the amount you want to deposit");
        float amount = scanner.nextFloat();
        try {
            shareServer.deposit(token, amount);
            checkUserBalance();
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
            signIn();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        } finally {
            return;
        }
    }
}
