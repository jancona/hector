package me.prettyprint.cassandra.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.HCfDef;
import me.prettyprint.hector.api.ddl.HKsDef;
import me.prettyprint.hector.api.exceptions.HectorException;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.TokenRange;
import org.apache.cassandra.thrift.Cassandra.Client;

public class ThriftCluster extends AbstractCluster implements Cluster {

  public ThriftCluster(String clusterName, CassandraHostConfigurator cassandraHostConfigurator) {
    super(clusterName, cassandraHostConfigurator);
  }


  public List<TokenRange> describeRing(final String keyspace) throws HectorException {
    Operation<List<TokenRange>> op = new Operation<List<TokenRange>>(OperationType.META_READ) {
      @Override
      public List<TokenRange> execute(Cassandra.Client cassandra) throws HectorException {
        try {
          return cassandra.describe_ring(keyspace);
        } catch (Exception e) {
          throw xtrans.translate(e);
        }
      }
    };
    connectionManager.operateWithFailover(op);
    return op.getResult();
  }



  @Override
  public String updateKeyspace(final HKsDef ksdef) throws HectorException {
    Operation<String> op = new Operation<String>(OperationType.META_WRITE) {
      @Override
      public String execute(Cassandra.Client cassandra) throws HectorException {
        try {
          return cassandra.system_update_keyspace(((ThriftKsDef) ksdef).toThrift());
        } catch (Exception e) {
          throw xtrans.translate(e);
        }
      }
    };
    connectionManager.operateWithFailover(op);
    return op.getResult();
  }

  @Override
  public String addColumnFamily(final HCfDef cfdef) throws HectorException {
    Operation<String> op = new Operation<String>(OperationType.META_WRITE, 
        FailoverPolicy.ON_FAIL_TRY_ALL_AVAILABLE, 
        cfdef.getKeyspace()) {
      @Override
      public String execute(Cassandra.Client cassandra) throws HectorException {
        try {
          return cassandra.system_add_column_family(((ThriftCfDef) cfdef).toThrift());
        } catch (Exception e) {
          throw xtrans.translate(e);
        }
      }
    };
    connectionManager.operateWithFailover(op);
    return op.getResult();
  }

  @Override
  public String addKeyspace(final HKsDef ksdef) throws HectorException {
    Operation<String> op = new Operation<String>(OperationType.META_WRITE) {
      @Override
      public String execute(Cassandra.Client cassandra) throws HectorException {
        try {
          return cassandra.system_add_keyspace(((ThriftKsDef) ksdef).toThrift());
        } catch (Exception e) {
          throw xtrans.translate(e);
        }
      }
    };
    connectionManager.operateWithFailover(op);
    return op.getResult();
  }

}