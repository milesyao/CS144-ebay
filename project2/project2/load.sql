load data local infile './tmp/category.csv' into table Category
fields terminated by '|*|';

load data local infile './tmp/item.csv' into table Item
fields terminated by '|*|';

load data local infile './tmp/bids.csv' into table Bids
fields terminated by '|*|';

load data local infile './tmp/bidder.csv' into table Bidder
fields terminated by '|*|';

load data local infile './tmp/seller.csv' into table Seller
fields terminated by '|*|';

