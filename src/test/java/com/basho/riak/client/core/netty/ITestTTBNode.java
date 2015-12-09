package com.basho.riak.client.core.netty;

import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.operations.ts.StoreOperation;
import com.basho.riak.client.core.query.timeseries.Cell;
import com.basho.riak.client.core.query.timeseries.Row;
import com.basho.riak.client.core.util.BinaryValue;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by srg on 12/9/15.
 */
public class ITestTTBNode {

    protected final static String tableName = "GeoCheckin";

    protected final static long now = 1443806900000l; // "now"
    protected final static long fiveMinsInMS = 5l * 60l * 1000l;
    protected final static long fiveMinsAgo = now - fiveMinsInMS;
    protected final static long tenMinsAgo = fiveMinsAgo - fiveMinsInMS;
    protected final static long fifteenMinsAgo = tenMinsAgo - fiveMinsInMS;
    protected final static long fifteenMinsInFuture = now + (fiveMinsInMS * 3l);

    private RiakNode riakNode;


    @Before
    public void setup() throws UnknownHostException {
        riakNode = new RiakNode.Builder()
                .useTTB()
                .withRemotePort(10017)
                .build();

        riakNode.start();
    }


    @After
    public void shutdonw(){
        if (riakNode != null) {
            riakNode.shutdown();
        }
    }

    @Test
    public void test() throws InterruptedException, ExecutionException {
        final List<Row> rows = Arrays.asList(
                // "Normal" Data
                new Row(new Cell("hash1"), new Cell("user1"), Cell.newTimestamp(fifteenMinsAgo), new Cell("cloudy"), new Cell(79.0), new Cell(1), new Cell(true))
//            new Row(new Cell("hash1"))
        );

        final StoreOperation store = new StoreOperation.Builder(BinaryValue.create(tableName + 112)).withRows(rows).build();

        if (!riakNode.execute(store)) {
            Assert.fail("Cant store data");
        }

        store.get();
        assert store != null;

    }
}