package com.nosfie.easyorg.DataStructures;

public class Daytime {
    public int hours;
    public int minutes;
    public Daytime() {
        this.hours = 0;
        this.minutes = 0;
    }

    public Daytime(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return String.format("%02d", this.hours) + "-" +
               String.format("%02d", this.minutes);
    }
}
