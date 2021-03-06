package Instance;

public class InstanceRequest {
    private int ID;
    private int unit;
    private int activity;
    private int day;
    private int timeslot;
    private int gain;
    private Double penalty_A;
    private Double penalty_D;
    private Double penalty_T;
    private int proxy;


    public InstanceRequest(int ID, int unit, int activity, int day, int timeslot, int gain, Double penalty_A, Double penalty_D, Double penalty_T, int proxy) {
        this.ID = ID;
        this.unit = unit;
        this.activity = activity;
        this.day = day;
        this.timeslot = timeslot;
        this.gain = gain;
        this.penalty_A = penalty_A;
        this.penalty_D = penalty_D;
        this.penalty_T = penalty_T;
        this.proxy = proxy;

    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public int getProxy() {
        return proxy;
    }

    public void setProxy(int proxy) {
        this.proxy = proxy;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getGain() {
        return gain;
    }

    public void setGain(int gain) {
        this.gain = gain;
    }

    public Double getPenalty_A() {
        return penalty_A;
    }

    public void setPenalty_A(Double penalty_A) {
        this.penalty_A = penalty_A;
    }

    public Double getPenalty_D() {
        return penalty_D;
    }

    public void setPenalty_D(Double penalty_D) {
        this.penalty_D = penalty_D;
    }

    public Double getPenalty_T() {
        return penalty_T;
    }

    public void setPenalty_T(Double penalty_T) {
        this.penalty_T = penalty_T;
    }

    @Override
    public String toString() {
        return "Request{" + "ID=" + ID + ", unit=" + unit + ", activity=" + activity + ", day=" + day + ", timeslot=" + timeslot + ", gain=" + gain + ", penalty_A=" + penalty_A + ", penalty_D=" + penalty_D + ", penalty_T=" + penalty_T + ", proxy=" + proxy + '}';
    }


}
