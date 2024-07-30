# Welcome to pgdbdiff documentation

- Use to compare postgres schema change and data change, and generate schema incremental script (ddl) and data incremental script (dml).
- The schema comparer references third-party DDL "https://github.com/ab92015359/apgdiff".
- This compare tool has high performance (multi threaded supported).


## How this library works
  1. You can download "pgdbdiff-0.0.1-SNAPSHOT.jar", "apgdiff-2.7.0.jar" and "datadiffconfig.json" to your local from ./lib directory.
  2. Before running it, you should ensure that the machine has installed the "pg_dump.exe", you can downlond it from https://www.postgresql.org/download/
  3. Edit the "datadiffconfig.json" file.
  

## How to Run It
  - Run using default config file "datadiffconfig.json" in current working dir, and compare all config item.
~~~
java -jar pgdbdiff-0.0.1-SNAPSHOT.jar
~~~

  - Run using the specified configuration file, and compare all config item.
~~~
java -jar pgdbdiff-0.0.1-SNAPSHOT.jar -f=<configFile>
~~~

  - Run using the specified configuration file, and compare specified config item. if all, the "-c=ALL"; if K1, the "-c=K1"; if K1, k2 and K3, the "-c=K1,K2,K3"
~~~
java -jar pgdbdiff-0.0.1-SNAPSHOT.jar -f=<configFile> -c=<ALL|CompareKey>
~~~

## How to Config It
Create a config file in your project folder like the below example:
~~~
{
	"misc": {
		"pgDumpPath": "D:\\Program Files (x86)\\pgadmin-v7\\runtime\\pg_dump.exe",   // The pg_dump tool's location which is installed in the computer
		"enableSchemaDiff": true,   // Is it compared to schema change
		"enableDataDiff": true      // Is it compared to data change
	},
	"compares": [
		{
			"key": "gkskfbr_gs_prod2fz",   // Compare item's unique value, can include multiple compare items
			"value": {
				"source": {
					"host": "",                // Postgres db host
					"port": "",                // Postgres db port
					"dbName": "",              // Postgres db name
					"schema": "",              // Postgres schema name
					"username": "",            // Postgres username
					"password": ""             // Postgres password
				},
				"target": {
					"host": "",
					"port": "",
					"dbName": "",
					"schema": "",
					"username": "",
					"password": ""
				},
				"compareOptions": {
					"batchSize": 8000,        // The max number of data loaded from the database each time
					"concurrent": 200,        // Pool size for concurrent query threads
					"dataCompare": {
						"tables": [             // The table array to compare
							{
								"tableName": "base_code",    // The name of the table without schema
								"tableKeyFields": [          // The comma-separated list of fields name that can be used to identify rows uniquely
									"id", "code"
								]
							}
						]
					}
				}
			}
		}
	]
}
~~~
