CREATE TABLE geom (
	itemid varchar(100) NOT NULL,
	position POINT NOT NULL
) ENGINE=MyISAM;

INSERT INTO geom(itemid, position) (SELECT itemid, GeomFromText(CONCAT('POINT(',lla,' ',llong,')')) FROM Item WHERE llong <> '' AND lla <> '');

-- select asText(position) from geom;

CREATE SPATIAL INDEX sp_index ON geom(position);


