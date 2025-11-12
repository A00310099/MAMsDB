-- Create the database
DROP DATABASE IF EXISTS db4_jdbc_assignment;
CREATE DATABASE IF NOT EXISTS db4_jdbc_assignment;
USE db4_jdbc_assignment;

-- Create the tables
DROP TABLE IF EXISTS Enclosures;
CREATE TABLE Enclosures (
	EnclosureID INT NOT NULL AUTO_INCREMENT,
    EnclosureLabel VARCHAR(255),
    PRIMARY KEY (EnclosureID)
);
DROP TABLE IF EXISTS Animals;
CREATE TABLE Animals (
    AnimalID INT NOT NULL AUTO_INCREMENT,
    AnimalType VARCHAR(255) NOT NULL,
    AnimalName VARCHAR(255),
    AnimalGender VARCHAR(20),
    AnimalAge INT,
    EnclosureID INT,
    PRIMARY KEY (AnimalID)
);
DROP TABLE IF EXISTS FeedingTimes;
CREATE TABLE FeedingTimes (
	FeedingID INT NOT NULL AUTO_INCREMENT,
	EnclosureID INT NOT NULL,
    FeedingHour TIME NOT NULL,
    FedToday BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (FeedingID)
);
DROP TABLE IF EXISTS AuditLog;
CREATE TABLE AuditLog (
	Type VARCHAR(20),
    Description VARCHAR(255),
    TimeDone DATETIME NOT NULL
);

-- Initial data for enclosures table
INSERT INTO Enclosures (EnclosureLabel) VALUES
	('Chicken coop'),
    ('Cows'),
    ('Sheep'),
	('Pigs');
    
SELECT * FROM Enclosures;

-- Initial data for animals table
INSERT INTO Animals (AnimalType, AnimalName, AnimalGender, AnimalAge, EnclosureID) VALUES
	('Sheep', 'Shawn', 'M', 8, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Sheep')),
    ('Sheep', 'Sean', 'M', 3, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Sheep')),
    ('Sheep', 'Shaun', 'M', 7, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Sheep')),
    ('Sheep', 'Shauna', 'F', 4, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Sheep')),
    ('Chicken', 'Ra', 'M', 10, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Chicken coop')),
	('Chicken', 'Horus', 'M', 10, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Chicken coop')),
	('Chicken', 'Thoth', 'M', 10, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Chicken coop')),
	('Chicken', 'Bennu', 'M', 10, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Chicken coop')),
	('Chicken', 'Nekhbet', 'F', 10, (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Chicken coop')),
    ('Pig', 'Peppa', 'F', '3', (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Pigs')),
	('Pig', 'George', 'M', '1', (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Pigs')),
	('Pig', 'Daddy Pig', 'M', '42', (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Pigs')),
    ('Pig', 'Mummy Pig', 'F', '30', (SELECT EnclosureID FROM Enclosures WHERE EnclosureLabel LIKE 'Pigs'));
    
SELECT * FROM Animals;

-- Initial data for feeding times table
INSERT INTO FeedingTimes (EnclosureID, FeedingHour) VALUES
	(1, '9:00'),
    (2, '13:00'),
    (3, '14:00'),
    (4, '12:00'),
    (4, '18:00');

SELECT * FROM FeedingTimes;
    
-- Initial entry in audit log
INSERT INTO AuditLog VALUES ('INITIAL SETUP', 'Tables created with default values', now());

-- Audit log for INSERTS
DROP TRIGGER IF EXISTS trAnimalInsertAudit;
DELIMITER //
CREATE TRIGGER trAnimalInsertAudit
	AFTER INSERT on Animals
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('INSERTED',
		CONCAT('Animal ', new.AnimalID, ': ', new.AnimalName, ' the ', new.AnimalAge, ' year-old ', new.AnimalGender, ' ', new.AnimalType, ' in Enclosure ', new.EnclosureID), 
		now());
END//

DROP TRIGGER IF EXISTS trEnclosureInsertAudit;
DELIMITER //
CREATE TRIGGER trEnclosureInsertAudit
	AFTER INSERT on Enclosures
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('INSERTED',
		CONCAT('Enclosure ', new.EnclosureID, ': ', new.EnclosureLabel), 
		now());
END//

DROP TRIGGER IF EXISTS trFeedingInsertAudit;
DELIMITER //
CREATE TRIGGER trFeedingInsertAudit
	AFTER INSERT on FeedingTimes
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('INSERTED',
		CONCAT('Feeding Time ', new.FeedingHour, ' for Enclosure ', new.EnclosureID, IF(new.FedToday, ' (Fed today)', ' (Not fed today)')), 
		now());
END//

-- Audit log for UPDATES
DROP TRIGGER IF EXISTS trAnimalUpdateAudit;
DELIMITER //
CREATE TRIGGER trAnimalUpdateAudit
	AFTER UPDATE on Animals
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('UPDATED',
		CONCAT('Animal ', old.AnimalID, ': [', old.AnimalName, ' the ', old.AnimalAge, ' year-old ', old.AnimalGender, ' ', old.AnimalType, ' in Enclosure ', old.EnclosureID,
			'] -> [', new.AnimalName, ' the ', new.AnimalAge, ' year-old ', new.AnimalGender, ' ', new.AnimalType, ' in Enclosure ', new.EnclosureID, ']'), 
		now());
END//

DROP TRIGGER IF EXISTS trEnclosureUpdateAudit;
DELIMITER //
CREATE TRIGGER trEnclosureUpdateAudit
	AFTER UPDATE on Enclosures
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('UPDATED',
		CONCAT('Enclosure ', old.EnclosureID, ': (', old.EnclosureLabel, ') -> (', new.EnclosureLabel, ')'), 
		now());
END//

DROP TRIGGER IF EXISTS trFeedingUpdateAudit;
DELIMITER //
CREATE TRIGGER trFeedingUpdateAudit
	AFTER UPDATE on FeedingTimes
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('UPDATED',
		CONCAT('Feeding Time [', old.FeedingHour, ' for Enclosure ', old.EnclosureID, IF(old.FedToday, ' (Fed today)', ' (Not fed today)'),
			'] -> [', new.FeedingHour, ' for Enclosure ', new.EnclosureID, IF(new.FedToday, ' (Fed today)', ' (Not fed today)'), ']'), 
		now());
END//

-- Audit log for DELETES
DROP TRIGGER IF EXISTS trAnimalDeleteAudit;
DELIMITER //
CREATE TRIGGER trAnimalDeleteAudit
	AFTER DELETE on Animals
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('DELETED',
		CONCAT('Animal ', old.AnimalID, ': ', old.AnimalName, ' the ', old.AnimalAge, ' year-old ', old.AnimalGender, ' ', old.AnimalType, ' in Enclosure ', old.EnclosureID), 
		now());
END//

DROP TRIGGER IF EXISTS trEnclosureDeleteAudit;
DELIMITER //
CREATE TRIGGER trEnclosureDeleteAudit
	AFTER DELETE on Enclosures
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('DELETED',
		CONCAT('Enclosure ', old.EnclosureID, ': ', old.EnclosureLabel), 
		now());
END//

DROP TRIGGER IF EXISTS trFeedingDeleteAudit;
DELIMITER //
CREATE TRIGGER trFeedingDeleteAudit
	AFTER DELETE on FeedingTimes
    FOR EACH ROW
    BEGIN
    INSERT INTO AuditLog VALUES
		('DELETED',
		CONCAT('Feeding Time ', old.FeedingHour, ' for Enclosure ', old.EnclosureID, IF(old.FedToday, ' (Fed today)', ' (Not fed today)')), 
		now());
END//

SELECT * FROM AuditLog;

-- Event for resetting "fed today" to false every day
DROP EVENT IF EXISTS evResetFedStatus;
DELIMITER //
CREATE EVENT evResetFedStatus
ON SCHEDULE EVERY 1 DAY
STARTS (current_date + interval 1 day)
DO BEGIN
	INSERT INTO AuditLog VALUES ('EVENT', 'Daily reset of Fed Status', now());
	UPDATE FeedingTimes SET FedToday = false;
END//

SET GLOBAL event_scheduler = ON;
SHOW EVENTS;

-- Stored function for getting the animal population in any given enclosure
DROP FUNCTION IF EXISTS fnCountAnimalsInEnclosure;
DELIMITER //
CREATE FUNCTION fnCountAnimalsInEnclosure
(
	id INT
)
RETURNS INT
DETERMINISTIC
BEGIN
	DECLARE population INT;
	SELECT count(*) INTO population FROM Animals WHERE EnclosureID = id;
	RETURN population;
END//

/*
	NOTE: if you get an error while running the whole SQL file,
	highlight everything from this point and run it separately
*/
-- Custom view for getting the enclosure labels with highest populations
DROP VIEW IF EXISTS max_population_enclosures;
CREATE VIEW max_population_enclosures AS
SELECT EnclosureLabel,
fnCountAnimalsInEnclosure(EnclosureID) AS 'Enclosure Population'
FROM Enclosures
WHERE fnCountAnimalsInEnclosure(EnclosureID) IN
(SELECT max(fnCountAnimalsInEnclosure(EnclosureID)) FROM Enclosures);

SELECT * FROM max_population_enclosures;

-- Custom view for basic Animals info without IDs
DROP VIEW IF EXISTS Animals_Info;
CREATE VIEW Animals_Info
AS SELECT AnimalType, AnimalName, AnimalGender, AnimalAge FROM Animals;

SELECT * FROM Animals_Info;

-- Custom view for Animals with Enclosure label and Feeding times
DROP VIEW IF EXISTS Merged_View;
CREATE VIEW Merged_View AS
SELECT a.AnimalType, a.AnimalName, a.AnimalGender, a.AnimalAge, e.EnclosureLabel, f.FeedingHour, f.FedToday
FROM Animals a
LEFT JOIN Enclosures e ON a.EnclosureID = e.EnclosureID
LEFT JOIN FeedingTimes f ON e.EnclosureID = f.EnclosureID
ORDER BY f.FeedingHour ASC, e.EnclosureID ASC;

SELECT * FROM Merged_View;

-- Merged_View as above but with IDs
DROP VIEW IF EXISTS Merged_View_withIDs;
CREATE VIEW Merged_View_withIDs AS
SELECT a.AnimalID, a.AnimalType, a.AnimalName, a.AnimalGender, a.AnimalAge, e.EnclosureID, e.EnclosureLabel, f.FeedingID, f.FeedingHour, f.FedToday
FROM Animals a
LEFT JOIN Enclosures e ON a.EnclosureID = e.EnclosureID
LEFT JOIN FeedingTimes f ON e.EnclosureID = f.EnclosureID
ORDER BY f.FeedingHour ASC, e.EnclosureID ASC;

SELECT * FROM Merged_View_withIDs;