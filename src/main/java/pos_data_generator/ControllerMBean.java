package pos_data_generator;

/**
 * The MBean interface for controlling the data generator.
 */
public interface ControllerMBean {

    public void setRate(int newRate);

    public int getRate();

    public int getState();

    public void setState(int newState);

    public int getTps();

    public void setTps(int newTps);

}