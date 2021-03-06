package me.prettyprint.cassandra.connection;

import static org.junit.Assert.*;
import me.prettyprint.cassandra.BaseEmbededServerSetupTest;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HThriftClientTest extends BaseEmbededServerSetupTest {

  private HThriftClient hThriftClient;
  // cassandraHostConfigurator = new CassandraHostConfigurator("127.0.0.1:9170");
  private CassandraHost cassandraHost;
  
  @Before
  public void doSetup() {
    cassandraHost = new CassandraHost("127.0.0.1:9170");
    hThriftClient = new HThriftClient(cassandraHost);
  }
  
  @After
  public void doTeardown() {
    hThriftClient.close();
  }
  
  @Test
  public void testOpenAndClose() {
    assertTrue(hThriftClient.open().isOpen());
    assertFalse(hThriftClient.close().isOpen());
  }

  @Test(expected=IllegalStateException.class)
  public void testFailOnDoubleOpen() {
    hThriftClient.open().open();
  }
  
  @Test(expected=IllegalStateException.class)
  public void testGetCassandraNotOpen() {
    hThriftClient.getCassandra();
  }
  
  @Test
  public void testGetCassandraWithKeyspace() {
    hThriftClient.open();
    hThriftClient.getCassandra("Keyspace1");
    assertTrue(hThriftClient.isOpen());
  }
  
  @Test
  public void testGetCassandraWithNullKeyspace() {
    hThriftClient.open();
    hThriftClient.getCassandra("Keyspace1");
    hThriftClient.getCassandra(null);
    assertTrue(hThriftClient.isOpen());
  }
  
  // make sure that if a keyspace does not exist, we get exceptions,
  // and after the wanted ks is created, the client should work properly
  @Test
  public void testNonExistingKeyspace() {
      hThriftClient.open();
      
      // this keyspace won't exist
      String ksname = "test_ks_" + Thread.currentThread().getName() + Thread.currentThread().getId();
      Exception caughtException = null;
      try {
      hThriftClient.getCassandra(ksname);
      } catch (Exception e) {
          caughtException = e;
      }
      assertTrue("if you try to access a non-existent ks, hclient should throw exception",
                      (caughtException !=null) && (caughtException instanceof HInvalidRequestException )
                );
                      
      // now create the ks
      KeyspaceDefinition ksdef = HFactory.createKeyspaceDefinition(ksname);
      Cluster cluster = HFactory.getOrCreateCluster(clusterName, "127.0.0.1:9170");
      cluster.addKeyspace(ksdef);
      // now it should work
      hThriftClient.getCassandra(ksname);
      assertTrue(hThriftClient.isOpen());
  }
  
}
