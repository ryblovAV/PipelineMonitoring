\echo '=================== creating database ==================='

DROP DATABASE IF EXISTS job_monitoring;
CREATE DATABASE job_monitoring;
\c job_monitoring;

CREATE TYPE PIPELINE_EVENT_TYPE AS ENUM('start', 'finish', 'failed');

CREATE TABLE PIPELINE_JOB (
    ID SERIAL PRIMARY KEY,
    NAME TEXT NOT NULL UNIQUE,
    EVENT_TYPE PIPELINE_EVENT_TYPE
);

CREATE TABLE DATA_SOURCE (
    ID SERIAL PRIMARY KEY,
    PATH TEXT NOT NULL UNIQUE UNIQUE
);

CREATE TABLE DATA_SOURCE_INPUT (
    ID SERIAL PRIMARY KEY,
    PIPELINE_JOB_ID INT NOT NULL REFERENCES PIPELINE_JOB (ID),
    DATA_SOURCE_ID INT NOT NULL REFERENCES DATA_SOURCE (ID),
    EVENT_DATE TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (PIPELINE_JOB_ID, DATA_SOURCE_ID)
);

CREATE TABLE DATA_SOURCE_OUTPUT (
    ID SERIAL PRIMARY KEY,
    PIPELINE_JOB_ID INT NOT NULL REFERENCES PIPELINE_JOB (ID),
    DATA_SOURCE_ID INT NOT NULL REFERENCES DATA_SOURCE (ID) UNIQUE,
    EVENT_DATE TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (PIPELINE_JOB_ID, DATA_SOURCE_ID)
);



--  JOB_A, JOB_B, JOB_C
--
-- JOB_A
--   - read failed
--   - write datasourceA, datasourceB (data is old data)
--
--
--
-- // PIPLEINE_A = JOB_A(failed) -> JOB_B (failed) -> JOB_C (failed) run daily
-- // PIPLEINE_B = JOB_D(success) run weekly
--
-- write event misiing
-- read datasourceA
--
--
-- SPark -> Kafka
--
-- HTPPServet -
--     background process: fs2kafka client,
--     read from Kafka -> write to DB ->
--
--     post => websocket => client
--
--     endpoints => get failed
--                  get outdated
--
--
--
--
--
--
--
--
--
-- // PIPLEINE_A 12/25
-- // PIPLEINE_A 12/26



-- CREATE TABLE PIPELINE_EVENT (
--     ID SERIAL PRIMARY KEY,
--     PIPELINE_ID INT NOT NULL REFERENCES PIPELINE (ID),
--     STATE PIPELINE_EVENT_TYPE NOT NULL,
--     EVENT_DATE TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );
--
-- CREATE TYPE DATASOURCE_EVENT_TYPE AS ENUM('read', 'write');
--
--
-- CREATE TABLE DATASOURCE_EVENT (
--     ID SERIAL PRIMARY KEY,
--     PIPELINE_ID INT NOT NULL REFERENCES PIPELINE (ID),
--     EVENT_TYPE DATASOURCE_EVENT_TYPE NOT NULL,
--     EVENT_DATE TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- )


\echo '=================== loading debug data ==================='
