/** -----------------------------------------------------------------------------------------  
   User
  -----------------------------------------------------------------------------------------  */
INSERT INTO KB_USER(username, firstname, lastname, password, lastModifiedUser) VALUES('abonzi',   'aldo', 'bonzi', '12345'	  , 1);
INSERT INTO KB_USER(username, firstname, lastname, password, lastModifiedUser) VALUES('atolomei', 'alejandro', 'tolomei', 'at', 1);
INSERT INTO KB_USER(username, firstname, lastname, password, lastModifiedUser) VALUES('aferraria', 'alejo', 'ferraria', 'af'  , 1);


/** -----------------------------------------------------------------------------------------  
   Resource.File
  -----------------------------------------------------------------------------------------  */
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('file1 v1', 1, null, 0,'seed0', 100,1);																				
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('file1 v2', 2, 1, 0,  'seed11', 100,1);																				
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('file1 v3', 3, 2, 0,  'seed12', 100,1);																				
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('file2 v1', 1, null, 0, 'seed2', 200,1);																				


/** -----------------------------------------------------------------------------------------  
   Resource.HTMLText
  -----------------------------------------------------------------------------------------  */
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('htmltext1_v1', 1, null, 0,'seedhtml1', 100,1);																				
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('htmltext2_v1', 1, null, 0,'seedhtml2', 100,1);																				
INSERT INTO RESOURCE(name, version, prev_version, mode, seed, size, lastModifiedUser) VALUES('htmltext3_v1', 1, null, 0,'seedhtml3', 100,1);																				
  
													
INSERT INTO HTMLTEXT(resource_id, htmltext) VALUES(5, 'html texto 1');																				
INSERT INTO HTMLTEXT(resource_id, htmltext) VALUES(6, 'html texto 2');																				
INSERT INTO HTMLTEXT(resource_id, htmltext) VALUES(7, 'html texto 3');																				


INSERT INTO ResourceFile (resource_id, file_id) VALUES(5, 1);																				
INSERT INTO ResourceFile (resource_id, file_id) VALUES(5, 2);																				
INSERT INTO ResourceFile (resource_id, file_id) VALUES(5, 3);																				
INSERT INTO ResourceFile (resource_id, file_id) VALUES(6, 1);																				
INSERT INTO ResourceFile (resource_id, file_id) VALUES(7, 4);																				


/** -----------------------------------------------------------------------------------------  
   Content.Ad.Banner
   
   INSERT INTO BANNER(title, bannertext, link, external, ga, image, lastModifiedUser) VALUES('title', 'banner text....', 'http', false, 'ga', 1,1);																				
   
   File -> versions !!!
   
-----------------------------------------------------------------------------------------  */








