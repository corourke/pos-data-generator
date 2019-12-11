package pos_data_generator;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvNumber;
import net.andreinc.mockneat.MockNeat;

import java.time.Instant;
import java.util.ArrayList;

// This bean is required by the gson library to produce JSON output
public class POS_Scan {

    @CsvBindByName(column="store_id")
    private Integer storeId;

    @CsvBindByName(column="scan_ts")
    private Long scanTs;

    @CsvBindByName(column="item_upc")
    private String itemUpc;

    @CsvBindByName(column="unit_qty")
    @CsvNumber("####")
    private Integer unitQty;

    public POS_Scan() {
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Long getScanTs() {
        return scanTs;
    }

    public void setScanTs(Long scanTs) {
        this.scanTs = scanTs;
    }

    public String getItemUpc() {
        return itemUpc;
    }

    public void setItemUpc(String itemUpc) {
        this.itemUpc = itemUpc;
    }

    public Integer getUnitQty() {
        return unitQty;
    }

    public void setUnitQty(Integer unitQty) {
        this.unitQty = unitQty;
    }

    POS_Scan(ArrayList<String> items) {
        // I anticipate more uses for this mocking library
        MockNeat mock = MockNeat.threadLocal();

        // Store number
        // TODO: should check the store seed data rather than assume
        // TODO: Store master needs probabilities
        this.storeId = mock.ints().range(1234, 4233).get();

        // Timestamp
        this.scanTs = Instant.now().toEpochMilli();

        // Random UPC number from item_master
        // TODO: Should use the probability number in the item_master table
        int index = (int)(Math.random() * items.size());
        this.itemUpc = items.get(index);

        // How many of this item were sold? Roughly geometric progression.
        this.unitQty = mock.probabilites(Integer.class)
                .add(.5, 1)
                .add(.25, 2)
                .add(.15, 3)
                .add(.10, 4).get();
    }

}
