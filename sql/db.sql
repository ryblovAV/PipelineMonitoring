\echo '=================== creating database ==================='

DROP DATABASE IF EXISTS job_monitoring;
CREATE DATABASE job_monitoring;
\c job_monitoring;

CREATE TYPE CONNECTION_TYPE AS ENUM('input', 'output');

CREATE TABLE PIPELINE_JOB (
    ID SERIAL PRIMARY KEY,
    NAME TEXT NOT NULL UNIQUE
);

CREATE TABLE DATA_SOURCE (
    ID SERIAL PRIMARY KEY,
    PATH TEXT NOT NULL UNIQUE
);

CREATE TABLE CONNECTION (
    ID SERIAL PRIMARY KEY,
    PIPELINE_JOB_ID INT NOT NULL REFERENCES PIPELINE_JOB (ID),
    DATA_SOURCE_ID INT NOT NULL REFERENCES DATA_SOURCE (ID),
    CONNECTION_TYPE CONNECTION_TYPE NOT NULL,
    UNIQUE (PIPELINE_JOB_ID, DATA_SOURCE_ID)
);

\echo '=================== loading debug data ==================='
insert into pipeline_job (NAME) VALUES ('JOB_A');
insert into pipeline_job (NAME) VALUES ('JOB_B');
insert into pipeline_job (NAME) VALUES ('JOB_C');
insert into pipeline_job (NAME) VALUES ('JOB_D');
insert into pipeline_job (NAME) VALUES ('JOB_E');

insert into data_source (path) values ('/path/to/a');
insert into data_source (path) values ('/path/to/b');
insert into data_source (path) values ('/path/to/c');
insert into data_source (path) values ('/path/to/d');

insert into connection (pipeline_job_id, data_source_id, connection_type) values (1, 1, 'output');
insert into connection (pipeline_job_id, data_source_id, connection_type) values (2, 2, 'output');

insert into connection (pipeline_job_id, data_source_id, connection_type) values (3, 1, 'input');
insert into connection (pipeline_job_id, data_source_id, connection_type) values (3, 2, 'input');
insert into connection (pipeline_job_id, data_source_id, connection_type) values (3, 3, 'output');

insert into connection (pipeline_job_id, data_source_id, connection_type) values (4, 3, 'input');
insert into connection (pipeline_job_id, data_source_id, connection_type) values (4, 4, 'output');