package de.jlo.talendcomp.oracle.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.jlo.datamodel.SQLTable;
import de.jlo.talendcomp.dbmerge.DBMerge;

public class TestOracleMerge {
	
	private Connection connection = null;
	private static final String configFile = "/Data/Talend/testdata/oracle_test_db.properties";
	
	@Before
	public void connect() throws Exception {
		Properties props = new Properties();
		props.load(new BufferedInputStream(new FileInputStream(new File(configFile))));
		Class.forName(props.getProperty("DRIVER_CLASS"));
		connection = DriverManager.getConnection(props.getProperty("URL"), props.getProperty("USER"), props.getProperty("PW"));
	}
	
	@After
	public void disconnect() throws Exception {
		if (connection != null) {
			connection.close();
		}
	}
	
	@Test
	public void loadTable() throws Exception {
		DBMerge m = new DBMerge(connection);
		String expectedTableName = "XTEST";
		m.setTargetTableName(expectedTableName);
		m.init();
		SQLTable t = m.getTargetSQLTable();
		assertTrue(t != null);
		assertEquals("Table name wrong", expectedTableName, t.getName());
		assertEquals(6, t.getFieldCount());
		assertEquals(2, t.getPrimaryKeyFieldNames().size());
	}
	
	@Test
	public void testCreateMergeInsertOnly() throws Exception {
		DBMerge m = new DBMerge(connection);
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.setAllowInsert(true);
		m.setAllowUpdate(false);
		m.setAllowDelete(false);
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.init();
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when not matched then\n"
			    + "  insert (t.JOB_INSTANCE_ID,t.PROCESS_INSTANCE_NAME,t.JOB_NAME,t.JOB_PROJECT,t.JOB_INFO,t.JOB_GUID,t.JOB_EXT_ID,t.ROOT_JOB_GUID,t.WORK_ITEM,t.TIME_RANGE_START,t.TIME_RANGE_END,t.VALUE_RANGE_START,t.VALUE_RANGE_END,t.JOB_STARTED_AT,t.JOB_ENDED_AT,t.JOB_RESULT,t.COUNT_INPUT,t.COUNT_OUTPUT,t.COUNT_UPDATED,t.COUNT_REJECTED,t.COUNT_DELETED,t.RETURN_CODE,t.RETURN_MESSAGE,t.HOST_NAME,t.HOST_PID,t.HOST_USER,t.PROCESS_INSTANCE_ID)\n"
			    + "  values (s.JOB_INSTANCE_ID,s.PROCESS_INSTANCE_NAME,s.JOB_NAME,s.JOB_PROJECT,s.JOB_INFO,s.JOB_GUID,s.JOB_EXT_ID,s.ROOT_JOB_GUID,s.WORK_ITEM,s.TIME_RANGE_START,s.TIME_RANGE_END,s.VALUE_RANGE_START,s.VALUE_RANGE_END,s.JOB_STARTED_AT,s.JOB_ENDED_AT,s.JOB_RESULT,s.COUNT_INPUT,s.COUNT_OUTPUT,s.COUNT_UPDATED,s.COUNT_REJECTED,s.COUNT_DELETED,s.RETURN_CODE,s.RETURN_MESSAGE,s.HOST_NAME,s.HOST_PID,s.HOST_USER,?)";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeInsertOnly2() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setSourceSelectCode("select * from S_TEST");
		String expectedTableName = "XTEST";
		m.setAllowInsert(true);
		m.setAllowUpdate(false);
		m.setAllowDelete(false);
		m.setFixedColumnValue("JOB_INSTANCE_ID", 99);
		m.setFixedColumnValue("FILE_ID", null);
		m.setFixedColumnValue("MAPPING_ID", 2);
		m.setFixedColumnValue("AMOS_IMPORT_OK", 0);
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into XTEST t\n"
			    + "using (\n"
			    + "select * from S_TEST\n"
			    + ") s\n"
			    + "on (((t.CONCODE is null and s.CONCODE is null) or (t.CONCODE=s.CONCODE)) and ((t.ACTYPE is null and s.ACTYPE is null) or (t.ACTYPE=s.ACTYPE)))\n"
			    + "when not matched then\n"
			    + "  insert (t.CONCODE,t.ACTYPE,t.JOB_INSTANCE_ID,t.FILE_ID,t.MAPPING_ID,t.AMOS_IMPORT_OK)\n"
			    + "  values (s.CONCODE,s.ACTYPE,?,?,?,?)";
		assertEquals("merge statement wrong", expected, actual);
		m.setDoCommit(true);
		m.execute();
	}

	@Test
	public void testCreateMergeUpdateOnly() throws Exception {
		DBMerge m = new DBMerge(connection);
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.setAllowInsert(false);
		m.setAllowUpdate(true);
		m.setAllowDelete(false);
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.init();
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.PROCESS_INSTANCE_NAME=s.PROCESS_INSTANCE_NAME,\n"
			    + "    t.JOB_NAME=s.JOB_NAME,\n"
			    + "    t.JOB_PROJECT=s.JOB_PROJECT,\n"
			    + "    t.JOB_INFO=s.JOB_INFO,\n"
			    + "    t.JOB_GUID=s.JOB_GUID,\n"
			    + "    t.JOB_EXT_ID=s.JOB_EXT_ID,\n"
			    + "    t.ROOT_JOB_GUID=s.ROOT_JOB_GUID,\n"
			    + "    t.WORK_ITEM=s.WORK_ITEM,\n"
			    + "    t.TIME_RANGE_START=s.TIME_RANGE_START,\n"
			    + "    t.TIME_RANGE_END=s.TIME_RANGE_END,\n"
			    + "    t.VALUE_RANGE_START=s.VALUE_RANGE_START,\n"
			    + "    t.VALUE_RANGE_END=s.VALUE_RANGE_END,\n"
			    + "    t.JOB_STARTED_AT=s.JOB_STARTED_AT,\n"
			    + "    t.JOB_ENDED_AT=s.JOB_ENDED_AT,\n"
			    + "    t.JOB_RESULT=s.JOB_RESULT,\n"
			    + "    t.COUNT_INPUT=s.COUNT_INPUT,\n"
			    + "    t.COUNT_OUTPUT=s.COUNT_OUTPUT,\n"
			    + "    t.COUNT_UPDATED=s.COUNT_UPDATED,\n"
			    + "    t.COUNT_REJECTED=s.COUNT_REJECTED,\n"
			    + "    t.COUNT_DELETED=s.COUNT_DELETED,\n"
			    + "    t.RETURN_CODE=s.RETURN_CODE,\n"
			    + "    t.RETURN_MESSAGE=s.RETURN_MESSAGE,\n"
			    + "    t.HOST_NAME=s.HOST_NAME,\n"
			    + "    t.HOST_PID=s.HOST_PID,\n"
			    + "    t.HOST_USER=s.HOST_USER,\n"
			    + "    t.PROCESS_INSTANCE_ID=?\n";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeUpdateOnlyWithWhere() throws Exception {
		DBMerge m = new DBMerge(connection);
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.setAllowInsert(false);
		m.setAllowUpdate(true);
		m.setAllowDelete(false);
		m.setUpdateWhereCondition("RETURN_CODE = 0");
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.init();
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.PROCESS_INSTANCE_NAME=s.PROCESS_INSTANCE_NAME,\n"
			    + "    t.JOB_NAME=s.JOB_NAME,\n"
			    + "    t.JOB_PROJECT=s.JOB_PROJECT,\n"
			    + "    t.JOB_INFO=s.JOB_INFO,\n"
			    + "    t.JOB_GUID=s.JOB_GUID,\n"
			    + "    t.JOB_EXT_ID=s.JOB_EXT_ID,\n"
			    + "    t.ROOT_JOB_GUID=s.ROOT_JOB_GUID,\n"
			    + "    t.WORK_ITEM=s.WORK_ITEM,\n"
			    + "    t.TIME_RANGE_START=s.TIME_RANGE_START,\n"
			    + "    t.TIME_RANGE_END=s.TIME_RANGE_END,\n"
			    + "    t.VALUE_RANGE_START=s.VALUE_RANGE_START,\n"
			    + "    t.VALUE_RANGE_END=s.VALUE_RANGE_END,\n"
			    + "    t.JOB_STARTED_AT=s.JOB_STARTED_AT,\n"
			    + "    t.JOB_ENDED_AT=s.JOB_ENDED_AT,\n"
			    + "    t.JOB_RESULT=s.JOB_RESULT,\n"
			    + "    t.COUNT_INPUT=s.COUNT_INPUT,\n"
			    + "    t.COUNT_OUTPUT=s.COUNT_OUTPUT,\n"
			    + "    t.COUNT_UPDATED=s.COUNT_UPDATED,\n"
			    + "    t.COUNT_REJECTED=s.COUNT_REJECTED,\n"
			    + "    t.COUNT_DELETED=s.COUNT_DELETED,\n"
			    + "    t.RETURN_CODE=s.RETURN_CODE,\n"
			    + "    t.RETURN_MESSAGE=s.RETURN_MESSAGE,\n"
			    + "    t.HOST_NAME=s.HOST_NAME,\n"
			    + "    t.HOST_PID=s.HOST_PID,\n"
			    + "    t.HOST_USER=s.HOST_USER,\n"
			    + "    t.PROCESS_INSTANCE_ID=?\n"
			    + "  where RETURN_CODE = 0\n";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeUpdateWithDelete() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setAllowInsert(false);
		m.setAllowUpdate(true);
		m.setAllowDelete(true);
		m.setUpdateWhereCondition("RETURN_CODE = 0");
		m.setDeleteWhereCondition("RETURN_CODE > 0");
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.PROCESS_INSTANCE_NAME=s.PROCESS_INSTANCE_NAME,\n"
			    + "    t.JOB_NAME=s.JOB_NAME,\n"
			    + "    t.JOB_PROJECT=s.JOB_PROJECT,\n"
			    + "    t.JOB_INFO=s.JOB_INFO,\n"
			    + "    t.JOB_GUID=s.JOB_GUID,\n"
			    + "    t.JOB_EXT_ID=s.JOB_EXT_ID,\n"
			    + "    t.ROOT_JOB_GUID=s.ROOT_JOB_GUID,\n"
			    + "    t.WORK_ITEM=s.WORK_ITEM,\n"
			    + "    t.TIME_RANGE_START=s.TIME_RANGE_START,\n"
			    + "    t.TIME_RANGE_END=s.TIME_RANGE_END,\n"
			    + "    t.VALUE_RANGE_START=s.VALUE_RANGE_START,\n"
			    + "    t.VALUE_RANGE_END=s.VALUE_RANGE_END,\n"
			    + "    t.JOB_STARTED_AT=s.JOB_STARTED_AT,\n"
			    + "    t.JOB_ENDED_AT=s.JOB_ENDED_AT,\n"
			    + "    t.JOB_RESULT=s.JOB_RESULT,\n"
			    + "    t.COUNT_INPUT=s.COUNT_INPUT,\n"
			    + "    t.COUNT_OUTPUT=s.COUNT_OUTPUT,\n"
			    + "    t.COUNT_UPDATED=s.COUNT_UPDATED,\n"
			    + "    t.COUNT_REJECTED=s.COUNT_REJECTED,\n"
			    + "    t.COUNT_DELETED=s.COUNT_DELETED,\n"
			    + "    t.RETURN_CODE=s.RETURN_CODE,\n"
			    + "    t.RETURN_MESSAGE=s.RETURN_MESSAGE,\n"
			    + "    t.HOST_NAME=s.HOST_NAME,\n"
			    + "    t.HOST_PID=s.HOST_PID,\n"
			    + "    t.HOST_USER=s.HOST_USER,\n"
			    + "    t.PROCESS_INSTANCE_ID=?\n"
			    + "  where RETURN_CODE = 0\n"
			    + "  delete where RETURN_CODE > 0\n";
		assertEquals("merge statement wrong", expected, actual);
	}
	
	@Test
	public void testCreateMergeInsertAndUpdateWithDelete() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setAllowInsert(true);
		m.setAllowUpdate(true);
		m.setAllowDelete(true);
		m.setUpdateWhereCondition("RETURN_CODE = 0");
		m.setDeleteWhereCondition("RETURN_CODE > 0");
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.PROCESS_INSTANCE_NAME=s.PROCESS_INSTANCE_NAME,\n"
			    + "    t.JOB_NAME=s.JOB_NAME,\n"
			    + "    t.JOB_PROJECT=s.JOB_PROJECT,\n"
			    + "    t.JOB_INFO=s.JOB_INFO,\n"
			    + "    t.JOB_GUID=s.JOB_GUID,\n"
			    + "    t.JOB_EXT_ID=s.JOB_EXT_ID,\n"
			    + "    t.ROOT_JOB_GUID=s.ROOT_JOB_GUID,\n"
			    + "    t.WORK_ITEM=s.WORK_ITEM,\n"
			    + "    t.TIME_RANGE_START=s.TIME_RANGE_START,\n"
			    + "    t.TIME_RANGE_END=s.TIME_RANGE_END,\n"
			    + "    t.VALUE_RANGE_START=s.VALUE_RANGE_START,\n"
			    + "    t.VALUE_RANGE_END=s.VALUE_RANGE_END,\n"
			    + "    t.JOB_STARTED_AT=s.JOB_STARTED_AT,\n"
			    + "    t.JOB_ENDED_AT=s.JOB_ENDED_AT,\n"
			    + "    t.JOB_RESULT=s.JOB_RESULT,\n"
			    + "    t.COUNT_INPUT=s.COUNT_INPUT,\n"
			    + "    t.COUNT_OUTPUT=s.COUNT_OUTPUT,\n"
			    + "    t.COUNT_UPDATED=s.COUNT_UPDATED,\n"
			    + "    t.COUNT_REJECTED=s.COUNT_REJECTED,\n"
			    + "    t.COUNT_DELETED=s.COUNT_DELETED,\n"
			    + "    t.RETURN_CODE=s.RETURN_CODE,\n"
			    + "    t.RETURN_MESSAGE=s.RETURN_MESSAGE,\n"
			    + "    t.HOST_NAME=s.HOST_NAME,\n"
			    + "    t.HOST_PID=s.HOST_PID,\n"
			    + "    t.HOST_USER=s.HOST_USER,\n"
			    + "    t.PROCESS_INSTANCE_ID=?\n"
			    + "  where RETURN_CODE = 0\n"
			    + "  delete where RETURN_CODE > 0\n"
			    + "when not matched then\n"
			    + "  insert (t.JOB_INSTANCE_ID,t.PROCESS_INSTANCE_NAME,t.JOB_NAME,t.JOB_PROJECT,t.JOB_INFO,t.JOB_GUID,t.JOB_EXT_ID,t.ROOT_JOB_GUID,t.WORK_ITEM,t.TIME_RANGE_START,t.TIME_RANGE_END,t.VALUE_RANGE_START,t.VALUE_RANGE_END,t.JOB_STARTED_AT,t.JOB_ENDED_AT,t.JOB_RESULT,t.COUNT_INPUT,t.COUNT_OUTPUT,t.COUNT_UPDATED,t.COUNT_REJECTED,t.COUNT_DELETED,t.RETURN_CODE,t.RETURN_MESSAGE,t.HOST_NAME,t.HOST_PID,t.HOST_USER,t.PROCESS_INSTANCE_ID)\n"
			    + "  values (s.JOB_INSTANCE_ID,s.PROCESS_INSTANCE_NAME,s.JOB_NAME,s.JOB_PROJECT,s.JOB_INFO,s.JOB_GUID,s.JOB_EXT_ID,s.ROOT_JOB_GUID,s.WORK_ITEM,s.TIME_RANGE_START,s.TIME_RANGE_END,s.VALUE_RANGE_START,s.VALUE_RANGE_END,s.JOB_STARTED_AT,s.JOB_ENDED_AT,s.JOB_RESULT,s.COUNT_INPUT,s.COUNT_OUTPUT,s.COUNT_UPDATED,s.COUNT_REJECTED,s.COUNT_DELETED,s.RETURN_CODE,s.RETURN_MESSAGE,s.HOST_NAME,s.HOST_PID,s.HOST_USER,?)";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeInsertAndUpdateOnlyColumnsWithDelete() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setAllowInsert(false);
		m.setAllowUpdate(true);
		m.addUpdateOnlyColumn("JOB_GUID");
		m.addUpdateOnlyColumn("WORK_ITEM");
		m.setAllowDelete(true);
		m.setUpdateWhereCondition("RETURN_CODE = 0");
		m.setDeleteWhereCondition("RETURN_CODE > 0");
		m.addExcludeColumn("JOB_DISPLAY_NAME");
		m.setFixedColumnValue("PROCESS_INSTANCE_ID", 99);
		m.setSourceSelectCode("select * from JOB_INSTANCE_STATUS_TEST");
		String expectedTableName = "JOB_INSTANCE_STATUS";
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into JOB_INSTANCE_STATUS t\n"
			    + "using (\n"
			    + "select * from JOB_INSTANCE_STATUS_TEST\n"
			    + ") s\n"
			    + "on (t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.JOB_GUID=s.JOB_GUID,\n"
			    + "    t.WORK_ITEM=s.WORK_ITEM,\n"
			    + "    t.PROCESS_INSTANCE_ID=?\n"
			    + "  where RETURN_CODE = 0\n"
			    + "  delete where RETURN_CODE > 0\n";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeInsertAndUpdateOnlyColumns() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setAllowInsert(false);
		m.setAllowUpdate(true);
		m.addUpdateOnlyColumn("AMOS_IMPORT_OK");
		m.setAllowDelete(false);
		m.setSourceSelectCode("select * from Z_XDHECT");
		String expectedTableName = "XDHECT";
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into XDHECT t\n"
			    + "using (\n"
			    + "select * from Z_XDHECT\n"
			    + ") s\n"
			    + "on (t.DOCNO=s.DOCNO and t.DOCTYPE=s.DOCTYPE and t.REVISION=s.REVISION and t.ISSUEDBY=s.ISSUEDBY)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.AMOS_IMPORT_OK=s.AMOS_IMPORT_OK\n";
		assertEquals("merge statement wrong", expected, actual);
	}

	@Test
	public void testCreateMergeInsertAndUpdateCheckKeywords() throws Exception {
		DBMerge m = new DBMerge(connection);
		m.setAllowInsert(true);
		m.setAllowUpdate(true);
//		m.addUpdateOnlyColumn("SIZE");
		m.setAllowDelete(false);
		m.addKeyword("weight");
		m.setSourceSelectCode("select * from Z_XPART");
		String expectedTableName = "XPART";
		m.setTargetTableName(expectedTableName);
		m.init();
		String actual = m.buildMergeStatement();
		System.out.println(actual);
		String expected = "merge into XPART t\n"
			    + "using (\n"
			    + "select * from Z_XPART\n"
			    + ") s\n"
			    + "on (t.PARTNO=s.PARTNO)\n"
			    + "when matched then\n"
			    + "  update set\n"
			    + "    t.DESCRIPTION=s.DESCRIPTION,\n"
			    + "    t.ATACHAPTER=s.ATACHAPTER,\n"
			    + "    t.MATERIALCLASS=s.MATERIALCLASS,\n"
			    + "    t.FAACTYPE=s.FAACTYPE,\n"
			    + "    t.ADDRESS=s.ADDRESS,\n"
			    + "    t.STORETIME=s.STORETIME,\n"
			    + "    t.\"WEIGHT\"=s.\"WEIGHT\",\n"
			    + "    t.ALERTQTY=s.ALERTQTY,\n"
			    + "    t.REORDERLEVEL=s.REORDERLEVEL,\n"
			    + "    t.MAXPURCHQTY=s.MAXPURCHQTY,\n"
			    + "    t.MEASUREUNIT=s.MEASUREUNIT,\n"
			    + "    t.TOOL=s.TOOL,\n"
			    + "    t.REPAIRABLE=s.REPAIRABLE,\n"
			    + "    t.\"SIZE\"=s.\"SIZE\",\n"
			    + "    t.DOCUMENTREF=s.DOCUMENTREF,\n"
			    + "    t.REMARKS=s.REMARKS,\n"
			    + "    t.DEFAULTSUPPLIER=s.DEFAULTSUPPLIER,\n"
			    + "    t.DEFAULTREPAIR=s.DEFAULTREPAIR,\n"
			    + "    t.MANUFACTURER=s.MANUFACTURER,\n"
			    + "    t.REORDERMODEL=s.REORDERMODEL,\n"
			    + "    t.STATUS=s.STATUS,\n"
			    + "    t.ATA200CONVERT=s.ATA200CONVERT,\n"
			    + "    t.MATTYPE=s.MATTYPE,\n"
			    + "    t.FIXEDASSET=s.FIXEDASSET,\n"
			    + "    t.ORIGIN=s.ORIGIN,\n"
			    + "    t.MAXSHOPVISIT=s.MAXSHOPVISIT,\n"
			    + "    t.SWRESETCOND=s.SWRESETCOND,\n"
			    + "    t.SPECIFICATIONS=s.SPECIFICATIONS,\n"
			    + "    t.EXTSTATE=s.EXTSTATE,\n"
			    + "    t.SAFETYSTOCK=s.SAFETYSTOCK,\n"
			    + "    t.PMA=s.PMA,\n"
			    + "    t.SPECIALMEASUREUNIT=s.SPECIALMEASUREUNIT,\n"
			    + "    t.RESOURCETYPE=s.RESOURCETYPE,\n"
			    + "    t.JOB_INSTANCE_ID=s.JOB_INSTANCE_ID,\n"
			    + "    t.FILE_ID=s.FILE_ID,\n"
			    + "    t.MAPPING_ID=s.MAPPING_ID,\n"
			    + "    t.AMOS_IMPORT_OK=s.AMOS_IMPORT_OK\n"
			    + "when not matched then\n"
			    + "  insert (t.PARTNO,t.DESCRIPTION,t.ATACHAPTER,t.MATERIALCLASS,t.FAACTYPE,t.ADDRESS,t.STORETIME,t.\"WEIGHT\",t.ALERTQTY,t.REORDERLEVEL,t.MAXPURCHQTY,t.MEASUREUNIT,t.TOOL,t.REPAIRABLE,t.\"SIZE\",t.DOCUMENTREF,t.REMARKS,t.DEFAULTSUPPLIER,t.DEFAULTREPAIR,t.MANUFACTURER,t.REORDERMODEL,t.STATUS,t.ATA200CONVERT,t.MATTYPE,t.FIXEDASSET,t.ORIGIN,t.MAXSHOPVISIT,t.SWRESETCOND,t.SPECIFICATIONS,t.EXTSTATE,t.SAFETYSTOCK,t.PMA,t.SPECIALMEASUREUNIT,t.RESOURCETYPE,t.JOB_INSTANCE_ID,t.FILE_ID,t.MAPPING_ID,t.AMOS_IMPORT_OK)\n"
			    + "  values (s.PARTNO,s.DESCRIPTION,s.ATACHAPTER,s.MATERIALCLASS,s.FAACTYPE,s.ADDRESS,s.STORETIME,s.\"WEIGHT\",s.ALERTQTY,s.REORDERLEVEL,s.MAXPURCHQTY,s.MEASUREUNIT,s.TOOL,s.REPAIRABLE,s.\"SIZE\",s.DOCUMENTREF,s.REMARKS,s.DEFAULTSUPPLIER,s.DEFAULTREPAIR,s.MANUFACTURER,s.REORDERMODEL,s.STATUS,s.ATA200CONVERT,s.MATTYPE,s.FIXEDASSET,s.ORIGIN,s.MAXSHOPVISIT,s.SWRESETCOND,s.SPECIFICATIONS,s.EXTSTATE,s.SAFETYSTOCK,s.PMA,s.SPECIALMEASUREUNIT,s.RESOURCETYPE,s.JOB_INSTANCE_ID,s.FILE_ID,s.MAPPING_ID,s.AMOS_IMPORT_OK)";
		assertEquals("merge statement wrong", expected, actual);
	}
}
