package io.smartcat.cassandra.diagnostics.report;

/**
 * This class represents a query report.
 */
public class QueryReport {
  /**
   * Query execution start time (epoch in nano seconds).
   */
  public long startTime;

  /**
   * Query execution time (in nano seconds).
   */
  public long executionTime;

  /**
   * The originating client's address.
   */
  public String clientAddress;

  /**
   * CQL statement.
   */
  public String statement;

  /**
   * Constructor.
   *
   * @param startTime query execution start time (epoch in nanoseconds)
   * @param executionTime query execution time (in nanoseconds)
   * @param clientAddress client address
   * @param statement query statement
   */
  public QueryReport(long startTime, long executionTime, String clientAddress, String statement) {
    super();
    this.startTime = startTime;
    this.executionTime = executionTime;
    this.clientAddress = clientAddress;
    this.statement = statement;
  }

  @Override
  public String toString() {
    return "QueryReport [startTime=" + startTime + ", executionTime=" + executionTime + ", clientAddress="
        + clientAddress + ", statement=" + statement + "]";
  }

}
