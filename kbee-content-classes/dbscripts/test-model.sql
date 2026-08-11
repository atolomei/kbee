

INSERT INTO DATASET(name, lastModifiedUser) VALUES('Dataset1', 1);	
INSERT INTO DATASET(name, lastModifiedUser) VALUES('Dataset2', 1);	
INSERT INTO DATASET(name, lastModifiedUser) VALUES('Dataset3', 1);	
INSERT INTO DATASET(name, lastModifiedUser) VALUES('Dataset4', 1);	 	
INSERT INTO DATASET(name, lastModifiedUser) VALUES('Dataset5', 1);	 	

INSERT INTO classifier(name, lastModifiedUser, dataset_id) VALUES('Classifier1', 1,1);
INSERT INTO classifier(name, lastModifiedUser, dataset_id) VALUES('Classifier2', 1,2);
INSERT INTO classifier(name, lastModifiedUser, dataset_id) VALUES('Classifier3', 1,3);
INSERT INTO classifier(name, lastModifiedUser, dataset_id) VALUES('Classifier4', 1,4);
INSERT INTO classifier(name, lastModifiedUser, dataset_id) VALUES('Classifier5', 1,5);


INSERT INTO datasetmember(name, lastModifiedUser, parent, dataset_id) VALUES('DatasetMember11', 1, null, 1);	
INSERT INTO datasetmember(name, lastModifiedUser, parent, dataset_id) VALUES('DatasetMember12', 1, null, 1);	
INSERT INTO datasetmember(name, lastModifiedUser, parent, dataset_id) VALUES('DatasetMember13', 1, null, 1);	
INSERT INTO datasetmember(name, lastModifiedUser, parent, dataset_id) VALUES('DatasetMember21', 1, null, 2);	
INSERT INTO datasetmember(name, lastModifiedUser, parent, dataset_id) VALUES('DatasetMember22', 1, null, 2);	


INSERT INTO classification(name, lastModifiedUser, classifier_id, datasetmember_id) VALUES('classification1', 1,6, 30);	
INSERT INTO classification(name, lastModifiedUser, classifier_id, datasetmember_id) VALUES('classification2', 1,7, 31);	
INSERT INTO classification(name, lastModifiedUser, classifier_id, datasetmember_id) VALUES('classification3', 1,8, 32);	
INSERT INTO classification(name, lastModifiedUser, classifier_id, datasetmember_id) VALUES('classification4', 1,9, 33);	




 

