package io.smartcat.cassandra_diagnostics.report;

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

	@Override
	public String toString() {
		return "QueryReport [startTime=" + startTime + ", executionTime=" + executionTime + ", clientAddress="
				+ clientAddress + ", statement=" + statement + "]";
	}
	
}
