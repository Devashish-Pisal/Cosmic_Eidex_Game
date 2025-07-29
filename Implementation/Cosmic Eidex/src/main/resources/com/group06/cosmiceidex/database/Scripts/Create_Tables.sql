CREATE TABLE [Users] ( 
    [ID] INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
    [Name] VARCHAR(250) NOT NULL UNIQUE,
    [Password] VARCHAR(250) NOT NULL
);

CREATE TABLE [Leaderboard](
    [User_ID] INTEGER PRIMARY KEY,
    [Wins] INTEGER,
    [Tricks] INTEGER,
    [Points] INTEGER,
    FOREIGN KEY (USER_ID) REFERENCES Users(ID) ON DELETE CASCADE
);

-- LOADING SAMPLE DATA INTO TABLES --

INSERT INTO Users (Name, Password) VALUES
('test1', 'test1'),
('test2', 'test2'),
('test3', 'test3'),
('test4', 'test4'),
('test5', 'test5'),
('test6', 'test6');

INSERT INTO Leaderboard (User_ID, Wins, Tricks, Points) VALUES
(1, 9, 10, 50),
(2, 7, 13, 40),
(3, 6, 15, 45),
(4, 5, 10, 50),
(5, 4, 4, 30),
(6, 1, 15, 22);
