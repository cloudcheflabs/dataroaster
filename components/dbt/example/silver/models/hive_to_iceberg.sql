{{
  config(
    pre_hook = "set session query_max_run_time='10m'",
    materialized = "incremental",
    incremental_strategy = "append",
    on_table_exists = "drop",
    format = "ORC",
    using = "ICEBERG"
  )
}}
SELECT
	baseproperties.eventtype,
	itemid, 
	price 
FROM hive.default.test_parquet