import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

    private long token;

    private ShareServer shareServer;
    private Scanner scanner = new Scanner(System.in);

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
      String login, password;
      while(true) {
          System.out.println("Enter your login");
          login = scanner.next();
          System.out.println("Enter your password");
          password = scanner.next();

          try {
              token = shareServer.login(login, password);
              break;
          } catch (AuthenticationFailed e) {
              System.out.println(e.getMessage());
          } catch (RemoteException e) {
              e.printStackTrace();
          }
      }
      mainMenu();
    }

    public void mainMenu(){
        int input;
        System.out.println("Welcome to the main menu.");
        System.out.println("Select the following options to your liking: \n1 - Deposit\n2 - Withdraw\n 3 - Purchase\n 4 - Sell\n5 - Download the list of all shares\n6 - Download the list of all your share holdings");
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
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                default:
                    System.out.println("Unsupported action, please retry");
            }
        }
    }

    public void withdraw() {
        System.out.println("Enter the amount you want to deposit");
        float amount = scanner.nextFloat();
        try {
            shareServer.withdraw(token, amount);
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        }

    }

    public void deposit() {
        System.out.println("Enter the amount you want to deposit");
        float amount = scanner.nextFloat();
        try {
            shareServer.deposit(token, amount);
        } catch (AuthenticationFailed e) {
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NegativeNumberException e) {
            System.out.println(e.getMessage());
        }
    }
}
