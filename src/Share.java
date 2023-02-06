import java.rmi.*;

public interface Share extends Remote {
    String getShareName();
    int getVolumeOfSharesAvailable();
    float getSharePrice();
    float getTimeRemainingToPriceUpdate();
}
