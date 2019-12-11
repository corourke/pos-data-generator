package pos_data_generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The MBean for controlling the data generator.
 */
public class Controller extends NotificationBroadcasterSupport implements ControllerMBean {

    // Thread safe state variables
    private AtomicInteger state = new AtomicInteger(1);
    private AtomicInteger rate = new AtomicInteger(1);
    private AtomicInteger tps = new AtomicInteger(0);
    private long sequenceNumber;
    private Logger logger = LoggerFactory.getLogger(CSVProducer.class);

    Controller() {
        sequenceNumber = 1;
    }

    /**
     * Changes the rate at which transactions are generated.
     * @param newRate The data generation rate in transactions per second.
     */
    @Override
    public void setRate(int newRate) {
        int oldRate = rate.get();
        rate.set(newRate);

        Notification n = new AttributeChangeNotification(this,
                sequenceNumber++, System.currentTimeMillis(),
                "Trx Rate Changed", "Rate", "int",
                oldRate, rate.get());

        sendNotification(n);
        logger.info("Rate changed");
    }

    /**
     * Changes the run state of the data generator.
     * @param newState The new run state of the data generator.
     *                 0 will cause the generator to exit.
     *                 1 will start transactions
     *                 2 will pause transactions
     */
    @Override
    public void setState(int newState) {
        int oldState = state.get();
        state.set(newState);

        Notification n = new AttributeChangeNotification(this,
                sequenceNumber++, System.currentTimeMillis(),
                "Trx State Changed", "State", "int",
                oldState, state.get());

        sendNotification(n);
        logger.info("State changed");
    }

    public void setTps(int newTps) {
        int oldTps = tps.get();
        tps.set(newTps);

        Notification n = new AttributeChangeNotification(this,
                sequenceNumber++, System.currentTimeMillis(),
                "TPS Changed", "TPS", "int",
                oldTps, tps.get());

        sendNotification(n);
    }

    @Override
    public int getRate() {
        return rate.get();
    }

    @Override
    public int getState() {
        return state.get();
    }

    @Override
    public int getTps() { return tps.get(); }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{
                AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        logger.info("MBeanNotificationInfo called ");
        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }


}