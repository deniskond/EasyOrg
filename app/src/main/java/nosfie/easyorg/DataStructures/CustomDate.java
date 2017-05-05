package nosfie.easyorg.DataStructures;

public class CustomDate {
    public int day;
    public int month;
    public int year;
    CustomDate() {
        this.day = 0;
        this.month = 0;
        this.year = 0;
    }

    @Override
    public String toString() {
        return String.format("%04d", this.year) + "."
            + String.format("%02d", this.month) + "."
            + String.format("%02d", this.day);
    }
}
