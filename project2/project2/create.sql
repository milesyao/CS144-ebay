create table Category(
itemid varchar(100) NOT NULL,
category varchar(100) NOT NULL,
PRIMARY KEY(itemid, category)
);

create table Item(
itemid varchar(100) NOT NULL,
name varchar(100) NOT NULL,
currently DECIMAL(8,2) NOT NULL, 
buy_price DECIMAL(8,2),
first_bid DECIMAL(8,2) NOT NULL,
numbids int NOT NULL,
lname varchar(100) NOT NULL,
lla varchar(100), 
llong varchar(100),
country varchar(100) NOT NULL,
started TIMESTAMP NOT NULL,
ends TIMESTAMP NOT NULL,
sellerid varchar(100) NOT NULL,
description varchar(4000) NOT NULL,
PRIMARY KEY(itemid)
);

create table Bids(
userid varchar(100) NOT NULL,
time TIMESTAMP NOT NULL,
itemid varchar(100) NOT NULL,
amount DECIMAL(8,2) NOT NULL,
PRIMARY KEY(userid, time)
);

create table Bidder(
userid varchar(100) NOT NULL,
lname varchar(100) NOT NULL,
country varchar(100) NOT NULL,
rating int NOT NULL,
PRIMARY KEY(userid)
);

create table Seller(
userid varchar(100) NOT NULL,
rating int NOT NULL,
PRIMARY KEY(userid)
);