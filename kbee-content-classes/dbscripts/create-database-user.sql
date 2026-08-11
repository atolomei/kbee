--POSTGRES

--Creación de usuario kbee (ejecutar si no existe el usuario)
CREATE USER kbee PASSWORD 'kbee';


--ORACLE

--Creación de tablespace y usuario kbee (ejecutar si no existe el usuario)
--CREATE TABLESPACE idoc datafile 'C:\oraclexe\idoc\idoc.dbf' size 1000m; 
--CREATE USER kbee IDENTIFIED BY kbee DEFAULT TABLESPACE idoc;
--GRANT ALL PRIVILEGES TO kbee;