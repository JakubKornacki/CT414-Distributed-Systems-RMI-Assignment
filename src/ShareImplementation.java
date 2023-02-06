public class ShareImplementation implements Share {

    private String shareName;
    private int noOfSharesAvailable;
    private float sharePrice;

    public ShareImplementation(String shareName, int noOfSharesAvailable, float sharePrice) {
        this.shareName = shareName;
        this. noOfSharesAvailable = noOfSharesAvailable;
        this.sharePrice = sharePrice;
    }
    @Override
    public String getShareName() {
        return shareName;
    }

    @Override
    public int getVolumeOfSharesAvailable() {
        return noOfSharesAvailable;
    }

    @Override
    public float getSharePrice() {
        return sharePrice;
    }

    @Override
    public float getTimeRemainingToPriceUpdate() {
        return 0;
    }
}
