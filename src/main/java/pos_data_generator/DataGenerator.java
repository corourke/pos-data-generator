package pos_data_generator;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.lang.String;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.management.*;

/**
 * Generates data until program is killed or MBean state is set to exit.
 */
@CommandLine.Command(
        name="data-generator",
        description="Generates large volumes of test data"
)
public class DataGenerator implements Runnable {
    @CommandLine.Option(names = {"-o", "--output"}, required=true)
    private String outputPath;
    
    @CommandLine.Option(names = {"-r", "--rate"})
    private int transactionRate = 100;

    public static void main(String[] args) throws Exception {
        new CommandLine(new DataGenerator()).execute(args);
    }

    private DataGenerator() throws Exception {
        // TODO: Check that the output path is valid and writable
    }

    @Override
    public void run() {
        final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

        // Register our Controller MBean with the Agent
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Controller controller = new Controller();
        ObjectName objectName = null;
        try {
            objectName = new ObjectName("pos_data_generator:type=basic,name=Controller");
            mbs.registerMBean(controller, objectName);
            controller.setRate(transactionRate);
        } catch (MalformedObjectNameException
                | InstanceAlreadyExistsException
                | MBeanRegistrationException
                | NotCompliantMBeanException e) {
            e.printStackTrace();
        }

        // Latch to allow the producer thread to flush and shutdown
        CountDownLatch latch = new CountDownLatch(1);

        // Create the producer runnable
        Runnable scanProducer = new CSVProducer(latch, outputPath);

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





