#1
SELECT COUNT(*) FROM (SELECT userid FROM Bidder UNION SELECT userid FROM Seller) as Z;

#2
select count(*) from Item where binary lname = "New York";

#3
select count(*) from (select * from Category group by itemid having count(*)=4) as Z;

#4
select itemid from Item where currently = (select MAX(currently) from Item where ends > '2001-12-20 00:00:01' and numbids > 0) and numbids>0;

#5
select count(*) from Seller where Seller.rating > 1000;

#6
select count(*) from Bidder, Seller where Bidder.userid=Seller.userid;

#7
SELECT COUNT(DISTINCT Category) FROM Category c, Bids b 
WHERE c.itemid = b.itemid AND b.amount > 100;

