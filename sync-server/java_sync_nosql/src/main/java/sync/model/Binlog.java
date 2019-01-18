package sync.model;

import java.util.Date;

/**
 * Binlog
 */
public class Binlog{
    String binlog;
    String db;
    String table;
    String eventType;
    String before;
    String after;
    Date time;
	/**
	 * @return the binlog
	 */
	public String getBinlog() {
		return binlog;
	}
	/**
	 * @param binlog the binlog to set
	 */
	public void setBinlog(String binlog) {
		this.binlog = binlog;
	}
	/**
	 * @return the db
	 */
	public String getDb() {
		return db;
	}
	/**
	 * @param db the db to set
	 */
	public void setDb(String db) {
		this.db = db;
	}
	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}
	/**
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}
	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	/**
	 * @return the before
	 */
	public String getBefore() {
		return before;
	}
	/**
	 * @param before the before to set
	 */
	public void setBefore(String before) {
		this.before = before;
	}
	/**
	 * @return the after
	 */
	public String getAfter() {
		return after;
	}
	/**
	 * @param after the after to set
	 */
	public void setAfter(String after) {
		this.after = after;
	}
	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}


}