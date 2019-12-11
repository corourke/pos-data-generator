package pos_data_generator;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.lang.String;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

/**
 * Generates data until program is killed or MBean state is set to exit.
 */
public class DataGenerator {

    public static void main(String[] args) throws Exception {
        new DataGenerator().run();
    }

    private DataGenerator() throws Exception {

        // Register our Controller MBean with the Agent
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = null;
        try {
            objectName = new ObjectName("pos_data_generator:type=basic,name=Controller");
            Controller mbean = new Controller();
            mbs.registerMBean(mbean, objectName);
        } catch (MalformedObjectNameException
                | InstanceAlreadyExistsException
                | MBeanRegistrationException
                | NotCompliantMBeanException e) {
            e.printStackTrace();
        }

    }

    private void run() throws Exception {
        final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

        // Latch to wait for the producer thread to flush and return
        CountDownLatch latch = new CountDownLatch(1);

        // Create the producer runnable
        Runnable scanProducer = new CSVProducer(latch);

        // Start the producer thread
        Thread myThread = new Thread(scanProducer);
        logger.info("Starting producer thread");
        myThread.start();

        // Add a shutdown hook to stop the producer in an orderly fashion
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            ((CSVProducer) scanProducer).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("Error in shutdown hook: " + e);
            }
            logger.info("Application is closing");
        }));
    }

    private String getenv(String var, String default_value) {
        String value = System.getenv(var);
        if (value == null) {
            return default_value;
        } else {
            return value;
        }
    }

}





