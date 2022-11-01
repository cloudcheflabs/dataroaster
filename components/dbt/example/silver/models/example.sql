{{
  config(
    pre_hook = "set session query_max_run_time='10m'",
    materialized = "incremental",
    on_table_exists = "drop",
    unique_key = "itemid",
    incremental_strategy = "delete+insert",
    format = "ORC",
    properties = {
      "partitioning": "ARRAY['itemid']"
    },
    using = "ICEBERG"
  )
}}
SELECT
	baseproperties.eventtype,
	itemid, 
	price 
FROM iceberg.iceberg_db.test_iceberg