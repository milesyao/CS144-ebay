#!/bin/bash

# Run the drop.sql batch file to drop existing tables
# Inside the drop.sql, you sould check whether the table exists. Drop them ONLY if they exists.
mysql CS144 < drop.sql

# Run the create.sql batch file to create the database and tables
mysql CS144 < create.sql

# Compile and run the parser to generate the appropriate load files
ant
ant run-all

filelist="category.csv item.csv bids.csv bidder.csv seller.csv"
mkdir tmp

for i in $filelist; do
    sort -u $i >./tmp/$i
    rm $i;
done

mysql CS144 < load.sql

rm -r tmp
rm -r bin
