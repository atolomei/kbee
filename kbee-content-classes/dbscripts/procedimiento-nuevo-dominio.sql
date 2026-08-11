-- Funcion que crea un dominio con los parámetros especificados
--(el id de usuario y de grupo deben ser distintos: generan distintas entradas de la tabla principal)
select createDomain(	1							-- id del dominio
						, 1							-- id del usuario
						, 2							-- id del grupo (distinto del usurio)
						, 1							-- id del profile
						, 1							-- id de la perona			
						, 1							-- id del temlate (Idoc)
						, 'novamens'				-- nombre de dominio
						, 'erivero@novamens.com');	-- mail del usuario root

-- DataSet Usuarios
INSERT INTO dataset(id, creationdate, lastmodifieddate, lastmodifieduser, state, enabled, domain_id, name, type, hierarchical) VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, true, 1, 'Usuarios', 4, false);
--DataSetMember de usuario para el usuario root
INSERT INTO datasetmember(id, creationdate, lastmodifieddate, lastmodifieduser, state, domain_id, entity_id, strvalue, parent, dataset_id, type) VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, 1, 1, null, null, 1, 3);


--select createDomain(2, 3, 4, 2, 2, 2, 'centralab', 'erivero@novamens.com');




-- En Oracle
--EXECUTE  createDomain(1, 1, 2, 1, 1, 1, 'novamens', 'erivero@novamens.com');
--EXECUTE  createDomain(2, 3, 4, 2, 2, 2, 'centralab', 'erivero@novamens.com');



-- Ejemplo de insersión de nuevo content template (KbeeOrgChart)
--INSERT INTO ContentTemplate(id, lastmodifieduser, state, domain_id, contentclass_id, name) VALUES(4, 1, 1, 1, 'KbeeOrgChart', 'Orgchart');
