package org.janusgraph.hadoop.formats.util;

import org.janusgraph.diskstorage.Entry;
import org.janusgraph.diskstorage.StaticBuffer;
import org.janusgraph.diskstorage.configuration.ModifiableConfiguration;
import org.janusgraph.hadoop.config.ModifiableHadoopConfiguration;
import org.janusgraph.hadoop.config.TitanHadoopConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.tinkerpop.gremlin.hadoop.structure.io.HadoopPoolsConfigurable;

public abstract class AbstractBinaryInputFormat extends InputFormat<StaticBuffer, Iterable<Entry>> implements HadoopPoolsConfigurable {

    protected Configuration hadoopConf;
    protected ModifiableHadoopConfiguration mrConf;
    protected ModifiableConfiguration titanConf;

    @Override
    public void setConf(final Configuration config) {
        HadoopPoolsConfigurable.super.setConf(config);
        this.mrConf = ModifiableHadoopConfiguration.of(TitanHadoopConfiguration.MAPRED_NS, config);
        this.hadoopConf = config;
        this.titanConf = mrConf.getTitanGraphConf();
    }

    @Override
    public Configuration getConf() {
        return hadoopConf;
    }
}
