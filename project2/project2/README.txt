1. List your relations. Please specify all keys that hold on each relation. You need not specify attribute types at this stage.

Category(itemid, category) key:(itemid, category);

Item(itemid, name, currently, buy_price, first_bid, numbids, lname, llong, lla, country, started, ends, sellerid, description,
) key: itemid

Bids(userid, time, itemid, amount) key: (userid, time)

Bidder(userid, lname, country, rating) key: userid

Seller(userid, rating) key: userid

2. List all completely nontrivial functional dependencies that hold on each relation, excluding those that effectively specify keys.

lla, llong -> lname
itemid -> (name, currently, buy_price, first_bid, numbids, lname, llong, lla, country, started, ends, sellerid, description) 
（Bids.userid, Bids.time) -> (Bids.itemid, Bids.amount)
Bidder.userid -> (Bidder.lname, Bidder.country, Bidder.rating)
Seller.userid -> Seller.rating

3. Are all of your relations in Boyce-Codd Normal Form (BCNF)? If not, either redesign them and start over, or explain why you feel it is advantageous to use non-BCNF relations.
Yes.

4. Are all of your relations in Fourth Normal Form (4NF)? If not, either redesign them and start over, or explain why you feel it is advantageous to use non-4NF relations.
Yes.



