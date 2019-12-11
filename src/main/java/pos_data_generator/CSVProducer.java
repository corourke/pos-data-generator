package pos_data_generator;

import com.google.gson.Gson;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.JMX;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Generates transactions
 * TODO: make generic by passing in a data producer function and an output function
 */
public class CSVProducer implements Runnable {
    private CountDownLatch latch;
    private Logger logger = LoggerFactory.getLogger(CSVProducer.class.getName());
    private ArrayList<String> items = new ArrayList<>(32000);
    private ControllerMBean controller;


    CSVProducer(CountDownLatch latch) {
        this.latch = latch;

        // Read in the UPC codes from the item_master file
        load_csv(logger, items);

        // Get the Controller bean
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            controller = JMX.newMBeanProxy(mbs,
                    new ObjectName("pos_data_generator:type=basic,name=Controller"),
                    ControllerMBean.class, true);
        } catch ( MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        Gson gson = new Gson();
        long lastTimestamp;
        long lastFileTimestamp;
        long rowCount;
        long priorRowCount;
        List<POS_Scan> recordList;
        String fileName;

        try {
            lastTimestamp = Instant.now().toEpochMilli();
            lastFileTimestamp = Instant.now().toEpochMilli();
            rowCount = 0L;
            priorRowCount=0L;
            recordList = new ArrayList<POS_Scan>();

            while (controller.getState() != 0) { // 0 = EXIT

                // Don't waste CPU if we are waiting
                if(controller.getState() == 2) { // 2 = WAITING
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Auto-generated catch block
                    }
                }

                if(controller.getState() == 1) { // 1 = RUNNING
                    long timestamp = Instant.now().toEpochMilli();

                    // Create new scan record
                    POS_Scan pos_scan = new POS_Scan(items);

                    // Creates a JSON representation
                    String value = gson.toJson(pos_scan);
                    logger.info("Record: " + value);

                    // Add to array of records
                    recordList.add(pos_scan);
                    rowCount++;

                    // Pause if we have met or exceeded the rate limit
                    int time_elapsed = (int) (timestamp - lastTimestamp);
                    if (time_elapsed >= 1000 || (rowCount - priorRowCount) >= controller.getRate()) {
                        if ((time_elapsed) < 1000) {
                            Thread.sleep(1000 - (time_elapsed));
                        }
                        lastTimestamp = timestamp;
                        priorRowCount = rowCount;
                    }
                    // If we have a lot of rows or haven't output a file in a while, create file
                    if (recordList.size() >= 25000 || (timestamp - lastFileTimestamp) >= 60000 ) {
                        // Figure out the file name
                        fileName = "scans_" + timestamp;
                        File file = new File(fileName + ".tmp");
                        // Open the file and write the rows
                        Writer writer = new FileWriter(file.toString());
                        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
                        beanToCsv.write(recordList);
                        writer.close();
                        // Rename .tmp to .csv now that file is written and log it
                        file.renameTo(new File(fileName + ".csv"));
                        logger.info("Wrote: " + recordList.size() + " rows to: " + file.toString());

                        lastFileTimestamp = timestamp;
                        recordList = new ArrayList<POS_Scan>();
                    }
                } // IF RUNNING
            } // WHILE NOT EXIT
        } catch (Exception e) {
            logger.error("Exception in Producer thread: " + e);
        } finally {
            // TODO: Flush and close output
            latch.countDown();
        }
    }

    public void shutdown() {
        logger.info("Stopping producer");
        controller.setState(0); // EXIT
    }

    private static void load_csv(Logger logger, AbstractList list) {
        try {
            // TODO: Take output file from command line
            Reader reader = new FileReader("./src/main/resources/seed-data/item_master.csv");
            CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(reader);

            // Read in each category from the CSV file
            Map csvLine;
            while((csvLine = csvReader.readMap()) != null) {
                list.add(csvLine.get("ITEM_UPC"));
            }
            csvReader.close();
            reader.close();

        } catch (FileNotFoundException e) {
            logger.error("Can not find input CSV file. ", e);
        } catch (IOException e) {
            logger.error("IOException: ", e);
            e.printStackTrace();
        } catch (CsvValidationException e) {
            logger.error("CsvValidationException", e);
        }
    }

}
