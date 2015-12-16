package ru.atc.camel.alcatel.events;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.regex.Pattern;

//import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
//import org.apache.camel.Producer;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.component.cache.CacheConstants;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.model.ModelCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.thoughtworks.xstream.io.json.JsonWriter.Format;

//import com.apc.stdws.xsd.isxcentral._2009._10.ISXCDevice;
//import com.apc.stdws.xsd.isxcentral._2009._10.ISXCAlarmSeverity;
//import com.apc.stdws.xsd.isxcentral._2009._10.ISXCAlarm;
//import com.apc.stdws.xsd.isxcentral._2009._10.ISXCDevice;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;

//import ru.at_consulting.itsm.device.Device;
import ru.at_consulting.itsm.event.Event;
//import ru.atc.camel.alcatel.events.api.OVMMDevices;
//import ru.atc.camel.alcatel.events.api.OVMMEvents;

//import javax.sql.DataSource;
//import org.apache.commons.dbcp.BasicDataSource;
//import org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;

//import com.mysql.jdbc.Connection;
//import com.mysql.jdbc.Driver;

public class AlcatelConsumer extends ScheduledPollConsumer {

	//private String[] openids = {};

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static AlcatelEndpoint endpoint;

	public static ModelCamelContext context;

	public enum PersistentEventSeverity {
		OK, INFO, WARNING, MINOR, MAJOR, CRITICAL;

		public String value() {
			return name();
		}

		public static PersistentEventSeverity fromValue(String v) {
			return valueOf(v);
		}
	}

	public AlcatelConsumer(AlcatelEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		// this.afterPoll();
		this.setTimeUnit(TimeUnit.MINUTES);
		this.setInitialDelay(0);
		this.setDelay(endpoint.getConfiguration().getDelay());
	}

	public static ModelCamelContext getContext() {
		// TODO Auto-generated method stub
		return context;
	}

	public static void setContext(ModelCamelContext context1) {
		context = context1;

	}

	@Override
	protected int poll() throws Exception {

		String operationPath = endpoint.getOperationPath();

		if (operationPath.equals("events"))
			return processSearchEvents();

		// only one operation implemented for now !
		throw new IllegalArgumentException("Incorrect operation: " + operationPath);
	}

	@Override
	public long beforePoll(long timeout) throws Exception {

		logger.info("*** Before Poll!!!");
		// only one operation implemented for now !
		// throw new IllegalArgumentException("Incorrect operation: ");

		// send HEARTBEAT
		// genHeartbeatMessage();

		return timeout;
	}

	private void genErrorMessage(String message) {
		// TODO Auto-generated method stub
		long timestamp = System.currentTimeMillis();
		timestamp = timestamp / 1000;
		String textError = "Возникла ошибка при работе адаптера: ";
		Event genevent = new Event();
		genevent.setMessage(textError + message);
		genevent.setEventCategory("ADAPTER");
		genevent.setSeverity(PersistentEventSeverity.CRITICAL.name());
		genevent.setTimestamp(timestamp);

		genevent.setEventsource(String.format("%s", endpoint.getConfiguration().getAdaptername()));

		genevent.setStatus("OPEN");
		genevent.setHost("adapter");

		logger.info(" **** Create Exchange for Error Message container");
		Exchange exchange = getEndpoint().createExchange();
		exchange.getIn().setBody(genevent, Event.class);

		exchange.getIn().setHeader("EventIdAndStatus", "Error_" + timestamp);
		exchange.getIn().setHeader("Timestamp", timestamp);
		exchange.getIn().setHeader("queueName", "Events");
		exchange.getIn().setHeader("Type", "Error");

		try {
			getProcessor().process(exchange);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void genHeartbeatMessage(Exchange exchange) {
		// TODO Auto-generated method stub
		long timestamp = System.currentTimeMillis();
		timestamp = timestamp / 1000;
		// String textError = "Возникла ошибка при работе адаптера: ";
		Event genevent = new Event();
		genevent.setMessage("Сигнал HEARTBEAT от адаптера");
		genevent.setEventCategory("ADAPTER");
		genevent.setObject("HEARTBEAT");
		genevent.setSeverity(PersistentEventSeverity.OK.name());
		genevent.setTimestamp(timestamp);

		genevent.setEventsource(String.format("%s", endpoint.getConfiguration().getAdaptername()));

		logger.info(" **** Create Exchange for Heartbeat Message container");
		// Exchange exchange = getEndpoint().createExchange();
		exchange.getIn().setBody(genevent, Event.class);

		exchange.getIn().setHeader("Timestamp", timestamp);
		exchange.getIn().setHeader("queueName", "Heartbeats");
		exchange.getIn().setHeader("Type", "Heartbeats");
		exchange.getIn().setHeader("Source", String.format("%s", endpoint.getConfiguration().getAdaptername()));

		try {
			// Processor processor = getProcessor();
			// .process(exchange);
			// processor.process(exchange);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	// "throws Exception"
	private int processSearchEvents() throws Exception, Error {

		// String host = null,user, password, filesrc, filedsr, dirsrc, dirdst;
		// prepareSCP(host, user, password, filesrc, filedsr, dirsrc, dirdst);
		
		String host = endpoint.getConfiguration().getSite();
		String username = endpoint.getConfiguration().getUsername();
		String password = endpoint.getConfiguration().getPassword();
		String filedst = endpoint.getConfiguration().getLocallogfile();
		String filesrc = endpoint.getConfiguration().getRemotelogfile();
		String dirdst = endpoint.getConfiguration().getLocallogpath();
		String dirsrc = endpoint.getConfiguration().getRemotelogpath();
		
		
		
		try {
			prepareSCP(host, username, password, filesrc, filedst, dirsrc, dirdst);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			logger.error(String.format("Error while get Log file remotly from SSH: %s ", e1));
			genErrorMessage(e1.getMessage() + " " + e1.toString());
			return 0;
		}
	
		
		// Long timestamp;
		int lastid = endpoint.getConfiguration().getLastid();
		int offset = endpoint.getConfiguration().getOffset();
		String locallogfile = endpoint.getConfiguration().getLocallogfile();
		String locallogpath = endpoint.getConfiguration().getLocallogpath();

		List<HashMap<String, Object>> listOpenEvents = new ArrayList<HashMap<String, Object>>();
		//List<HashMap<String, Object>> listClosedEvents = new ArrayList<HashMap<String, Object>>();
		// List<HashMap<String, Object>> listVmStatuses = null;
		int events = 0;
		
		String source = endpoint.getConfiguration().getSource();

		logger.debug(String.format("***getSource: %s***", source));

		// int statuses = 0;
		try {

			/*
			 * // get All Closed events if (openids.length != 0) { logger.info(
			 * String.format("***Try to get Closed Events***"));
			 * listClosedEvents = getClosedEvents(dataSource); logger.info(
			 * String.format("***Received %d Closed Events from SQL***",
			 * listClosedEvents.size())); }
			 */
			// get All new (Open) events
			logger.info(String.format("***Try to get All Events***"));
			listOpenEvents = getAllEvents(lastid, offset, locallogpath, locallogfile);
			logger.info(String.format("***Received %d All Events from Logs***", listOpenEvents.size()));

			List<HashMap<String, Object>> listAllEvents = new ArrayList<HashMap<String, Object>>();
			listAllEvents.addAll(listOpenEvents);
			// listAllEvents.addAll(listClosedEvents);

			// List<HashMap<String, Object>> allevents = (List<HashMap<String,
			// Object>>) ArrayUtils.addAll(listOpenEvents);

			//String alarmtext, location;

			Event genevent = new Event();

			// logger.info( String.format("***Try to get VMs statuses***"));
			for (int i = 0; i < listAllEvents.size(); i++) {

				/*
				 * alarmtext = listAllEvents.get(i).get("alarmtext").toString();
				 * location = listAllEvents.get(i).get("location").toString();
				 * logger.debug("DB row " + i + ": " + alarmtext + " " +
				 * location);
				 */

				genevent = genEventObj(listAllEvents.get(i), host, source);

				logger.debug("*** Create Exchange ***");

				String key = genevent.getExternalid() + "_" + genevent.getStatus();

				Exchange exchange = getEndpoint().createExchange();
				exchange.getIn().setBody(genevent, Event.class);
				exchange.getIn().setHeader("EventUniqId", key);

				exchange.getIn().setHeader("Object", genevent.getObject());
				exchange.getIn().setHeader("Timestamp", genevent.getTimestamp());

				// exchange.getIn().setHeader("DeviceType",
				// vmevents.get(i).getDeviceType());

				try {
					getProcessor().process(exchange);
					events++;

					// File cachefile = new File("sendedEvents.dat");
					// removeLineFromFile("sendedEvents.dat",
					// "Tgc1-1Cp1_ping_OK");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(String.format("Error while process Exchange message: %s ", e));
				}

			}

			logger.debug(String.format("***Received %d Alcatel Events from Log *** ", listOpenEvents.size()));
			// logger.debug( String.format("***Received %d Alcatel Closed Events
			// from SQL*** ", listClosedEvents.size()));

			// logger.info(" **** Received " + events.length + " Opened Events
			// ****");

		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(String.format("Error while get Events from Log: %s ", e));
			genErrorMessage(e.getMessage() + " " + e.toString());
			// dataSource.close();
			return 0;
		} catch (Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(String.format("Error while get Events from Log: %s ", e));
			genErrorMessage(e.getMessage() + " " + e.toString());
			// dataSource.close();
			return 0;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(String.format("Error while get Events from Log: %s ", e));
			genErrorMessage(e.getMessage() + " " + e.toString());
			// dataSource.close();
			return 0;
		} finally {
			// dataSource.close();
			// return 0;
		}

		// dataSource.close();

		logger.info(String.format("***Sended to Exchange messages: %d ***", events));

		// removeLineFromFile("sendedEvents.dat", "Tgc1-1Cp1_ping_OK");

		return 1;
	}

	private void prepareSCP(String host, String username, String password, String filesrc, String filedst, String dirsrc,
			String dirdst) throws IOException{
		// TODO Auto-generated method stub
		SSHClient ssh = new SSHClient();
		// ssh.useCompression(); // Can lead to significant speedup (needs JZlib
		// in classpath)
		//File file = new File("c:\\users\\vgoryachev\\Documents\\MobaXterm\\home\\.ssh\\known_hosts");
		ssh.loadKnownHosts();
		ssh.connect(host);
		
		logger.debug(String.format("***dirdst + filedst: %s%s ***", dirdst, filedst));
		logger.debug(String.format("***dirsrc + filesrc: %s%s ***", dirsrc, filesrc));
		
		try {
			ssh.authPassword(username, password);
			ssh.newSCPFileTransfer().download(dirsrc + filesrc, new FileSystemFile(dirdst + filedst));
		} finally {
			ssh.disconnect();
		}

	}

	private List<HashMap<String, Object>> getAllEvents(int savedlastid, int offset, String locallogpath,
			String locallogfile) throws Throwable {
		// TODO Auto-generated method stub

		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		int lastid = 0;

		logger.info(" **** Try to receive Open Events ***** ");
		try {
			BufferedReader br = null;
			// PrintWriter pw = null;
			try {

				logger.debug(String.format("*** lastid: %d", savedlastid));
				logger.debug(String.format("*** offset: %d", offset));
				logger.debug(String.format("*** locallogpath: %s", locallogpath));
				logger.debug(String.format("*** locallogfile: %s", locallogfile));

				File inFile = new File(locallogpath + locallogfile);

				if (!inFile.isFile()) {
					System.out.println("Parameter is not an existing file");
					return null;
				}

				// Construct the new file that will later be renamed to the
				// original filename.
				// File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

				br = new BufferedReader(new FileReader(locallogpath + locallogfile));
				// pw = new PrintWriter(new FileWriter(tempFile));

				String line = null;
				int columns = 0;

				// String[] splited = {};

				// Read from the original file and write to the new
				// unless content matches data to be removed.

				Pattern p;
				Matcher matcher;
				String id = "";
				String message = "", host = "";
				while ((line = br.readLine()) != null) {

					// br.getClass()
					// if (!line.trim().equals(lineToRemove)) {
					logger.debug(String.format("*****1 %s", line.trim()));

					p = Pattern.compile("(\\d+.*)");
					matcher = p.matcher(line.trim());
					if (!matcher.matches()) {
						// genErrorMessage(String.format("Ошибка в формате
						// строки: %s", line));
						// logger.info(String.format("*****2 %s",
						// matcher.group(1).toString()));
						logger.debug(String.format("Ошибка в формате строки: %s", line));
						continue;
					}
					line = matcher.group(1).toString();

					String[] splited = line.split("\\s{2,}|\\t+");
					logger.debug(String.format("* TOTAL fields: %d", splited.length));

					columns = splited.length;

					if (splited.length < 10) {
						logger.debug(String.format("Ошибка в формате события: %s", splited[0]));
						genErrorMessage(String.format("Ошибка в формате события: %s", splited[0]));
						continue;
					}

					for (int i = 0; i < columns; i++) {

						logger.info(String.format("%d: %s", i, splited[i]));

						// System.out.println(matcher.group(2));

					}

					id = splited[0].replaceAll("[^0-9]", "");

					// check if lastid not less then current
					if (savedlastid >= Integer.parseInt(id)) {
						logger.debug(String.format("Skipping old ID: %s", id));
						continue;
					}

					p = Pattern.compile("\\d+");
					matcher = p.matcher(splited[1]);
					// String output = "";

					int ext = 0;
					if (!matcher.matches()) {
						ext = 1;
						logger.debug(String.format("!Using ext!"));
					}

					HashMap<String, Object> row = new HashMap<String, Object>(columns);

					message = "";
					host = "";

					row.put("id", id);
					row.put("object", splited[7 + ext]);

					if (ext == 0) {
						message = splited[8] + " " + splited[9];

					} else {
						message = splited[9];
						host = splited[1];
						// logger.info(String.format("%d: %s", i, splited[i]));
						// logger.info(String.format("Using ext"));
					}
					if (columns == 11)
						message = message + " " + splited[10];
					row.put("message", message);
					row.put("host", host);
					row.put("severity", splited[2 + ext]);
					row.put("eventtype", splited[3 + ext]);

					p = Pattern.compile("/");
					String[] objects = p.split(splited[5 + ext]);
					row.put("ciid", objects[2]);

					row.put("timestamp", splited[6 + ext]);

					logger.info(String.format(row.toString()));

					list.add(row);

					// get last int id
					lastid = Integer.parseInt(id);

					// pw.println(line);
					// pw.flush();

				}
				// pw.close();
				br.close();

				// save lastid in config var
				if (lastid == 0) {
					lastid = endpoint.getConfiguration().getLastid();
				}

				endpoint.getConfiguration().setLastid(lastid);

				return list;

			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
				throw ex;
			} catch (IOException ex) {
				ex.printStackTrace();
				throw ex;
			} finally {
				/*
				 * try { //pw.close(); br.close(); } catch (IOException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); }
				 */
			}
			// list = convertRStoList(resultset);

			// return null;

		} catch (Exception e) { // send error message to the same queue
			// TODO Auto-generated catch block
			logger.error(String.format("Error while execution: %s ", e));
			// genErrorMessage(e.getMessage());
			// 0;
			throw e;
		} catch (Throwable e) { // send error message to the same queue
			// TODO Auto-generated catch block
			logger.error(String.format("Error while execution: %s ", e));
			// genErrorMessage(e.getMessage());
			// 0;
			throw e;
		}

		finally {
			// if (con != null) con.close();

			// return list;
		}

	}

	private Event genEventObj(HashMap<String, Object> alarm, String host, String source) throws Exception, Throwable {
		// TODO Auto-generated method stub
		Event event;
		//boolean oldeventclosed = false;
		//List<HashMap<String, Object>> evlist = new ArrayList<HashMap<String, Object>>();

		event = new Event();

		logger.debug(String.format("ID: %s ", alarm.get("id").toString()));

		// evlist = getEventById(alarm.get("id").toString(), dataSource);

		Long eventtimestamp = (long) 0;
		eventtimestamp = (long) Integer.parseInt(alarm.get("timestamp").toString());
		// eventclosedate = (long)
		// Integer.parseInt(alarm.get("alarmofftime").toString());

		// logger.debug( String.format("alarmofftime: %s, %d ",
		// alarm.get("alarmofftime").toString(), eventclosedate ));

		if (alarm.get("host").toString() == "") {
			event.setHost(host);
		} else {
			event.setHost(alarm.get("host").toString());
		}

		// event.setHost(host);
		// event.setCi(vmtitle);
		// vmStatuses.get("ping_colour").toString())
		event.setObject(alarm.get("object").toString());
		event.setExternalid(alarm.get("id").toString());
		event.setCi(String.format("%s", alarm.get("ciid").toString()));
		event.setTimestamp(eventtimestamp);
		event.setEventCategory(alarm.get("eventtype").toString());
		event.setEventsource(source);
		// event.setParametr("Status");
		// String status = "OPEN";
		// event.setParametrValue(status);

		String severity = setRightSeverity(alarm.get("severity").toString());
		event.setSeverity(severity);

		event.setMessage(String.format("Ошибка на оборудовании Alcatel: %s", alarm.get("message").toString()));
		event.setCategory("NETWORK");

		if (severity == "OK") {
			event.setStatus("CLOSED");
		} else {
			event.setStatus("OPEN");
		}

		// System.out.println(event.toString());

		logger.debug(event.toString());

		return event;
	}

	public static String setRightSeverity(String string) {
		String newseverity = "";

		switch (string) {
		case "0":
			newseverity = PersistentEventSeverity.INFO.name();
			break;
		case "1":
			newseverity = PersistentEventSeverity.CRITICAL.name();
			break;
		case "2":
			newseverity = PersistentEventSeverity.MAJOR.name();
			break;
		case "3":
			newseverity = PersistentEventSeverity.MINOR.name();
			break;
		case "4":
			newseverity = PersistentEventSeverity.WARNING.name();
			break;
		case "5":
			newseverity = PersistentEventSeverity.OK.name();
			break;
		default:
			newseverity = PersistentEventSeverity.INFO.name();
			break;

		}
		/*
		 * System.out.println("***************** colour: " + colour);
		 * System.out.println("***************** newseverity: " + newseverity);
		 */
		return newseverity;
	}

}