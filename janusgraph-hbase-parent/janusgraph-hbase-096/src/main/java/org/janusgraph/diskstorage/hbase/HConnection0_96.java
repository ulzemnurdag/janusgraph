package org.janusgraph.diskstorage.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;

public class HConnection0_96 implements ConnectionMask
{

    private final HConnection cnx;

    public HConnection0_96(HConnection cnx)
    {
        this.cnx = cnx;
    }

    @Override
    public TableMask getTable(String name) throws IOException
    {
        return new HTable0_96(cnx.getTable(name));
    }

    @Override
    public AdminMask getAdmin() throws IOException
    {
        return new HBaseAdmin0_96(new HBaseAdmin(cnx));
    }

    @Override
    public void close() throws IOException
    {
        cnx.close();
    }
}
