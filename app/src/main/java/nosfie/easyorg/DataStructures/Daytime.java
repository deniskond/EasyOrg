package nosfie.easyorg.DataStructures;

public class Daytime {
    public int hours;
    public int minutes;
    Daytime() {
        this.hours = 0;
        this.minutes = 0;
    }

    @Override
    public String toString() {
        return String.format("%02d", this.hours) + "-" +
               String.format("%02d", this.minutes);
    }
}
