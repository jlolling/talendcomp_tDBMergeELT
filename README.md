# Talend component tDBMergeELT
A Talend component dedicated to use the merge command. This is an ELT SQL command which takes the values from a query and merge them in a target table.
Ths component creates the merge statement based on the current metadata of the target table and source query.

It works with following databases:
* Oracle (https://www.oracletutorial.com/oracle-basics/oracle-merge/)
* Exasol (https://docs.exasol.com/db/latest/sql/merge.htm)
* IBM DB2 (https://www.ibm.com/docs/de/db2/11.1?topic=statements-merge)
* MS SQL Server (https://learn.microsoft.com/en-us/sql/t-sql/statements/merge-transact-sql?view=sql-server-ver16)
* Teradata (https://docs.teradata.com/r/Teradata-VantageTM-SQL-Data-Manipulation-Language/March-2019/Statement-Syntax/MERGE/Usage-Notes/About-the-MERGE-Statement)
