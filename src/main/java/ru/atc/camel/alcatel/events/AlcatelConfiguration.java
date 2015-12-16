package ru.atc.camel.alcatel.events;


public class AlcatelConfiguration {	
    
	private String username;
	
	private String password;
	
	private String source;
	
	private String remotelogpath;
	
	private String remotelogfile;
	
	private String locallogpath;
	
	private String locallogfile;
	
	private int offset;
	
	private int lastid;
	
	private String site;
	
	private String adaptername;
	

    private String postgresql_db;
    

    private String postgresql_host;
    

    private String table_prefix;
    
    public String getTable_prefix() {
		return table_prefix;
	}

	public void setTable_prefix(String table_prefix) {
		this.table_prefix = table_prefix;
	}

	
    private String postgresql_port;
    
    
    private String query;
    
    private int delay = 60000;

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPostgresql_port() {
		return postgresql_port;
	}

	public void setPostgresql_port(String postgresql_port) {
		this.postgresql_port = postgresql_port;
	}

	public String getPostgresql_host() {
		return postgresql_host;
	}

	public void setPostgresql_host(String postgresql_host) {
		this.postgresql_host = postgresql_host;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getPostgresql_db() {
		return postgresql_db;
	}

	public void setPostgresql_db(String postgresql_db) {
		this.postgresql_db = postgresql_db;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAdaptername() {
		return adaptername;
	}

	public void setAdaptername(String adaptername) {
		this.adaptername = adaptername;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLastid() {
		return lastid;
	}

	public void setLastid(int lastid) {
		this.lastid = lastid;
	}

	public String getRemotelogpath() {
		return remotelogpath;
	}

	public void setRemotelogpath(String remotelogpath) {
		this.remotelogpath = remotelogpath;
	}

	public String getRemotelogfile() {
		return remotelogfile;
	}

	public void setRemotelogfile(String remotelogfile) {
		this.remotelogfile = remotelogfile;
	}

	public String getLocallogpath() {
		return locallogpath;
	}

	public void setLocallogpath(String locallogpath) {
		this.locallogpath = locallogpath;
	}

	public String getLocallogfile() {
		return locallogfile;
	}

	public void setLocallogfile(String locallogfile) {
		this.locallogfile = locallogfile;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}


}