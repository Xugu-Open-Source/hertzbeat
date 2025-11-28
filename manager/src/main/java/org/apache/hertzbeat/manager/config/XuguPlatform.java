package org.apache.hertzbeat.manager.config;

import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.databaseaccess.DatasourcePlatform;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.expressions.*;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.queries.DataReadQuery;
import org.eclipse.persistence.queries.StoredProcedureCall;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class XuguPlatform extends DatabasePlatform {

    private boolean isConnectionDataInitialized;
    public XuguPlatform(){
        super();
        this.pingSQL = "SELECT 1";
        this.startDelimiter = "`";
        this.endDelimiter = "`";
        this.supportsReturnGeneratedKeys = true;
    }


    @Override
    public void initializeConnectionData(Connection connection) throws SQLException {
        if (this.isConnectionDataInitialized) {
            return;
        }
        this.isConnectionDataInitialized = true;
    }


    public boolean isFractionalTimeSupported() {
        return true;
    }


    @Override
    protected void appendDate(java.sql.Date date, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printDate(date));
            writer.write("'");
        } else {
            super.appendDate(date, writer);
        }
    }
    @Override
    protected void appendTime(java.sql.Time time, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printTime(time));
            writer.write("'");
        } else {
            super.appendTime(time, writer);
        }
    }

    @Override
    protected void appendTimestamp(java.sql.Timestamp timestamp, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printTimestampWithoutNanos(timestamp));
            writer.write("'");
        } else {
            super.appendTimestamp(timestamp, writer);
        }
    }

    @Override
    protected void appendCalendar(Calendar calendar, Writer writer) throws IOException {
        if (usesNativeSQL()) {
            writer.write("'");
            writer.write(Helper.printCalendarWithoutNanos(calendar));
            writer.write("'");
        } else {
            super.appendCalendar(calendar, writer);
        }
    }

    @Override
    protected Hashtable<Class<?>, FieldTypeDefinition> buildFieldTypes() {
        Hashtable<Class<?>, FieldTypeDefinition> fieldTypeMapping = new Hashtable<>();
        fieldTypeMapping.put(Boolean.class, new FieldTypeDefinition("BOOLEAN", false));

        fieldTypeMapping.put(Integer.class, new FieldTypeDefinition("INTEGER", false));
        fieldTypeMapping.put(Long.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(Float.class, new FieldTypeDefinition("FLOAT", false));
        fieldTypeMapping.put(Double.class, new FieldTypeDefinition("DOUBLE", false));
        fieldTypeMapping.put(Short.class, new FieldTypeDefinition("SMALLINT", false));
        fieldTypeMapping.put(Byte.class, new FieldTypeDefinition("TINYINT", false));
        fieldTypeMapping.put(java.math.BigInteger.class, new FieldTypeDefinition("BIGINT", false));
        fieldTypeMapping.put(java.math.BigDecimal.class, new FieldTypeDefinition("DECIMAL",38));
        fieldTypeMapping.put(Number.class, new FieldTypeDefinition("DECIMAL",38));

        if(getUseNationalCharacterVaryingTypeForString()){
            fieldTypeMapping.put(String.class, new FieldTypeDefinition("NVARCHAR", DEFAULT_VARCHAR_SIZE));
        } else {
            fieldTypeMapping.put(String.class, new FieldTypeDefinition("VARCHAR", DEFAULT_VARCHAR_SIZE));
        }
        fieldTypeMapping.put(Character.class, new FieldTypeDefinition("CHAR", 1));

        fieldTypeMapping.put(Byte[].class, new FieldTypeDefinition("BLOB", false));
        fieldTypeMapping.put(Character[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(byte[].class, new FieldTypeDefinition("BLOB", false));
        fieldTypeMapping.put(char[].class, new FieldTypeDefinition("TEXT", false));
        fieldTypeMapping.put(java.sql.Blob.class, new FieldTypeDefinition("BLOB", false));
        fieldTypeMapping.put(java.sql.Clob.class, new FieldTypeDefinition("CLOB", false));

        // Mapping for JSON type.
        getJsonPlatform().updateFieldTypes(fieldTypeMapping);

        fieldTypeMapping.put(java.sql.Date.class, new FieldTypeDefinition("DATE", false));
        FieldTypeDefinition fd = new FieldTypeDefinition("TIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        }
        fieldTypeMapping.put(java.sql.Time.class, fd);
        fd = new FieldTypeDefinition("DATETIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        }
        fieldTypeMapping.put(java.sql.Timestamp.class, fd);

        fieldTypeMapping.put(java.time.LocalDate.class, new FieldTypeDefinition("DATE"));

        fd = new FieldTypeDefinition("DATETIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        } else {
            fd.setDefaultSize(6);
            fd.setIsSizeRequired(true);
        }
        fieldTypeMapping.put(java.time.LocalDateTime.class,fd); //no timezone info

        fd = new FieldTypeDefinition("TIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        } else {
            fd.setDefaultSize(6);
            fd.setIsSizeRequired(true);
        }
        fieldTypeMapping.put(java.time.LocalTime.class, fd);

        fd = new FieldTypeDefinition("DATETIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        } else {
            fd.setDefaultSize(6);
            fd.setIsSizeRequired(true);
        }
        fieldTypeMapping.put(java.time.OffsetDateTime.class, fd); //no timezone info

        fd = new FieldTypeDefinition("TIME");
        if (!isFractionalTimeSupported()) {
            fd.setIsSizeAllowed(false);
        } else {
            fd.setDefaultSize(6);
            fd.setIsSizeRequired(true);
        }
        fieldTypeMapping.put(java.time.OffsetTime.class, fd);
        return fieldTypeMapping;
    }

    @Override
    public int getJDBCType(Class<?> javaType) {
        if (javaType == ClassConstants.TIME_ODATETIME) {
            return Types.TIMESTAMP;
        } else if (javaType == ClassConstants.TIME_OTIME) {
            return Types.TIME;
        }
        return super.getJDBCType(javaType);
    }

    @Override
    public ValueReadQuery buildSelectQueryForIdentity() {
        ValueReadQuery selectQuery = new ValueReadQuery();
        StringWriter writer = new StringWriter();
        writer.write("SELECT LAST_INSERT_ID()");
        selectQuery.setSQLString(writer.toString());
        return selectQuery;
    }


    @Override
    public String buildProcedureCallString(StoredProcedureCall call, AbstractSession session, AbstractRecord row) {
        return buildProcedureCallString(call, session, row);
    }


    @Override
    public int computeMaxRowsForSQL(int firstResultIndex, int maxResults){
        return maxResults - ((firstResultIndex >= 0) ? firstResultIndex : 0);
    }


    @Override
    public boolean canBatchWriteWithOptimisticLocking(DatabaseCall call){
        return true;//usesNativeBatchWriting || !call.hasParameters();
    }

    @Override
    public String getConstraintDeletionString() {
        return " DROP CONSTRAINT ";
    }

    @Override
    public String getUniqueConstraintDeletionString() {
        return getConstraintDeletionString();
    }

    @Override
    public String getSelectForUpdateString() {
        return " FOR UPDATE";
    }

    @Override
    public boolean isForUpdateCompatibleWithDistinct() {
        return false;
    }

    @Override
    public ValueReadQuery getTimestampQuery() {
        if (timestampQuery == null) {
            timestampQuery = new ValueReadQuery();
            timestampQuery.setSQLString("SELECT NOW()");
            timestampQuery.setAllowNativeSQLQuery(true);
        }
        return timestampQuery;
    }


    @Override
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Atan2, "ATAN2"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Concat, "CONCAT"));
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Trunc, "TRUNCATE"));


        addOperator(logOperator());
        addOperator(ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.Today, "SYSDATE"));
        addOperator(ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentDate, "TO_DATE(CURRENT_DATE)"));
        addOperator(ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.CurrentTime, "SYSDATE"));
        addOperator(ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.LocalTime, "SYSDATE"));
        addOperator(ExpressionOperator.simpleFunctionNoParentheses(ExpressionOperator.LocalDateTime, "SYSDATE"));
        addOperator(ExpressionOperator.truncateDate());
        addOperator(ExpressionOperator.newTime());
        addOperator(ExpressionOperator.ifNull());
        addOperator(ExpressionOperator.simpleTwoArgumentFunction(ExpressionOperator.Atan2, "ATAN2"));
        addOperator(oracleDateName());
        addOperator(operatorLocate());
        addOperator(operatorLocate2());
        addOperator(regexpOperator());
        addOperator(oracleExtractOperator());
    }

    protected static ExpressionOperator logOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Log);
        List<String> v = new ArrayList<>(2);
        v.add("LOG(10,");
        v.add(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(FunctionExpression.class);
        return result;
    }

    protected static ExpressionOperator oracleDateName() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ExpressionOperator.FunctionOperator);
        exOperator.setSelector(ExpressionOperator.DateName);
        List<String> v = new ArrayList<>(3);
        v.add("TO_CHAR(");
        v.add(", '");
        v.add("')");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    protected static ExpressionOperator operatorLocate() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate);
        List<String> v = new ArrayList<>(3);
        v.add("INSTR(");
        v.add(", ");
        v.add(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }

    protected static ExpressionOperator operatorLocate2() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Locate2);
        List<String> v = new ArrayList<>(4);
        v.add("INSTR(");
        v.add(", ");
        v.add(", ");
        v.add(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(RelationExpression.class);
        return result;
    }

    protected static ExpressionOperator regexpOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(ExpressionOperator.Regexp);
        result.setType(ExpressionOperator.FunctionOperator);
        List<String> v = new ArrayList<>(3);
        v.add("REGEXP_LIKE(");
        v.add(", ");
        v.add(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        v = new ArrayList<>(2);
        v.add(".regexp(");
        v.add(")");
        result.printsJavaAs(v);
        return result;
    }

    private static final class XuguExtractOperator extends ExtractOperator {

        // QUARTER emulation: TO_NUMBER(TO_CHAR(", ", 'Q'))
        private static final String[] QUARTER_STRINGS = new String[] {"TO_NUMBER(TO_CHAR(", ", 'Q'))"};
        // ISO WEEK emulation: TO_NUMBER(TO_CHAR(", ", 'IW'))
        private static final String[] WEEK_STRINGS = new String[] {"TO_NUMBER(TO_CHAR(", ", 'IW'))"};

        private XuguExtractOperator() {
            super();
        }

        @Override
        protected void printQuarterSQL(final Expression first, Expression second, final ExpressionSQLPrinter printer) {
            printer.printString(QUARTER_STRINGS[0]);
            first.printSQL(printer);
            printer.printString(QUARTER_STRINGS[1]);
        }

        @Override
        protected void printQuarterJava(final Expression first, Expression second, final ExpressionJavaPrinter printer) {
            printer.printString(QUARTER_STRINGS[0]);
            first.printJava(printer);
            printer.printString(QUARTER_STRINGS[1]);
        }

        @Override
        protected void printWeekSQL(final Expression first, Expression second, final ExpressionSQLPrinter printer) {
            printer.printString(WEEK_STRINGS[0]);
            first.printSQL(printer);
            printer.printString(WEEK_STRINGS[1]);
        }

        @Override
        protected void printWeekJava(final Expression first, Expression second, final ExpressionJavaPrinter printer) {
            printer.printString(WEEK_STRINGS[0]);
            first.printJava(printer);
            printer.printString(WEEK_STRINGS[1]);
        }

    }

    private static ExpressionOperator oracleExtractOperator() {
        return new XuguExtractOperator();
    }


    @Override
    public void printFieldIdentityClause(Writer writer) throws ValidationException {
        try {
            writer.write(" AUTO_INCREMENT");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    @Override
    public boolean shouldUseJDBCOuterJoinSyntax() {
        return false;
    }

    @Override
    public boolean supportsIdentity() {
        return true;
    }

    @Override
    public boolean supportsCountDistinctWithMultipleFields() {
        return false;
    }

    @Override
    public boolean requiresTableInIndexDropDDL() {
        return true;
    }
    @Override
    public String buildDropIndex(String fullTableName, String indexName, String qualifier) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("DROP INDEX ");
        queryString.append(fullTableName);
        if (!qualifier.equals("")) {
            queryString.append(qualifier).append(".");
        }
        queryString.append(indexName);
        return queryString.toString();
    }

    @Override
    public boolean supportsGlobalTempTables() {
        return true;
    }

    @Override
    public boolean supportsIndividualTableLocking() {
        return true;
    }

    @Override
    public boolean supportsStoredFunctions() {
        return true;
    }

    @Override
    public boolean supportsAutoConversionToNumericForArithmeticOperations() {
        return true;
    }

    @Override
    protected String getCreateTempTableSqlPrefix() {
        return "CREATE TEMPORARY TABLE IF NOT EXISTS ";
    }

    @Override
    public String getDropDatabaseSchemaString(String schema) {
        return "DROP SCHEMA " + schema ;
    }
    @Override
    public boolean shouldAlwaysUseTempStorageForModifyAll() {
        return true;
    }

    @Override
    public boolean shouldPrintForUpdateClause(){
        return false;
    }

    @Override
    public boolean shouldPrintOutputTokenAtStart() {
        return false;
    }



    @Override
    public String getProcedureBeginString() {
        return "BEGIN ";
    }

    @Override
    public String getProcedureEndString() {
        return "END";
    }

    @Override
    public void writeUpdateOriginalFromTempTableSql(Writer writer, DatabaseTable table,
                                                    Collection<DatabaseField> pkFields,
                                                    Collection<DatabaseField> assignedFields) throws IOException
    {
        writer.write("UPDATE ");
        String tableName = table.getQualifiedNameDelimited(this);
        writer.write(tableName);
        writer.write(", ");
        String tempTableName = getTempTableForTable(table).getQualifiedNameDelimited(this);
        writer.write(tempTableName);
        writeAutoAssignmentSetClause(writer, tableName, tempTableName, assignedFields, this);
        writeAutoJoinWhereClause(writer, tableName, tempTableName, pkFields, this);
    }


    @Override
    public void writeDeleteFromTargetTableUsingTempTableSql(Writer writer, DatabaseTable table, DatabaseTable targetTable,
                                                            Collection<DatabaseField> pkFields,
                                                            Collection<DatabaseField> targetPkFields, DatasourcePlatform platform) throws IOException
    {
        writer.write("DELETE FROM ");
        String targetTableName = targetTable.getQualifiedNameDelimited(this);
        writer.write(targetTableName);
        writer.write(" USING ");
        writer.write(targetTableName);
        writer.write(", ");
        String tempTableName = getTempTableForTable(table).getQualifiedNameDelimited(this);
        writer.write(tempTableName);
        writeJoinWhereClause(writer, targetTableName, tempTableName, targetPkFields, pkFields, this);
    }

    @Override
    public void writeParameterMarker(Writer writer, ParameterExpression expression, AbstractRecord record, DatabaseCall call) throws IOException {
        super.writeParameterMarker(writer, expression, record, call);
    }

    private static final String LIMIT = " LIMIT ";
    @Override
    public void printSQLSelectStatement(DatabaseCall call, ExpressionSQLPrinter printer, SQLSelectStatement statement) {
        int max = 0;
        if (statement.getQuery() != null) {
            max = statement.getQuery().getMaxRows();
        }

        if (max <= 0 || !(this.shouldUseRownumFiltering())) {
            super.printSQLSelectStatement(call, printer, statement);
            statement.appendForUpdateClause(printer);
            return;
        }
        statement.setUseUniqueFieldAliases(true);
        call.setFields(statement.printSQL(printer));
        printer.printString(LIMIT);
        printer.printParameter(DatabaseCall.FIRSTRESULT_FIELD);
        printer.printString(", ");
        printer.printParameter(DatabaseCall.MAXROW_FIELD);
        statement.appendForUpdateClause(printer);
        call.setIgnoreFirstRowSetting(true);
        call.setIgnoreMaxResultsSetting(true);
    }

    @Override
    public boolean requiresProcedureBrackets() {
        return true;
    }

    @Override
    public void printStoredFunctionReturnKeyWord(Writer writer) throws IOException {
        writer.write("\n\t RETURN ");
    }

    @Override
    protected DataReadQuery getTableExistsQuery(final TableDefinition table) {
        String tableName = table.getFullName();
        if (isQuoted(table.getFullName())){
            tableName = unquote(tableName);
        }else {
            tableName = tableName.toUpperCase();
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT\n" +
                "\tt.table_name\n" +
                "FROM\n" +
                "\tall_tables t,\n" +
                "\tall_schemas s,\n" +
                "\tall_databases d\n" +
                "WHERE\n" +
                "\tt.schema_id = s.schema_id\n" +
                "\tand s.db_id = d.db_id\n" +
                "\tand d.db_id =(\n" +
                "\tSELECT CURRENT_DB_ID())");
        stringBuffer.append("\tand t.table_name = '");
        stringBuffer.append(tableName);
        stringBuffer.append("'");
        stringBuffer.append("\tand s.schema_name = (\n" +
                "\tSELECT CURRENT_SCHEMA())");
        final DataReadQuery query = new DataReadQuery(stringBuffer.toString());
        query.setMaxRows(1);
        return query;
    }
    public static String unquote(String name) {

        return isQuoted(name)? name.substring( 1, name.length() - 1 ) : name;
    }
    public static boolean isQuoted(final String name) {
        if ( name == null || name.isEmpty() ) {
            return false;
        }

        final char first = name.charAt( 0 );
        final char last = name.charAt( name.length() - 1 );

        return ( ( first == last ) && ( first == '`' || first == '"' ) );
    }
    @Override
    public boolean checkTableExists(final DatabaseSessionImpl session,
                                    final TableDefinition table, final boolean suppressLogging) {
        try {
            session.setLoggingOff(suppressLogging);
            final Vector result = (Vector)session.executeQuery(getTableExistsQuery(table));
            return !result.isEmpty();
        } catch (Exception notFound) {
            return false;
        }
    }




    @Override
    public ValueReadQuery getUUIDQuery() {
        if (uuidQuery == null) {
            uuidQuery = new ValueReadQuery();
            uuidQuery.setSQLString("SELECT UUID()");
            uuidQuery.setAllowNativeSQLQuery(true);
        }
        return uuidQuery;
    }
}
