
-- create Business table
CREATE TABLE Business (
    business_id     VARCHAR(100),
    business_name   VARCHAR(150) NOT NULL,
    city            VARCHAR(50),
    b_state         VARCHAR(20),
    address         VARCHAR(200),
    stars           NUMBER,
    review_count    INTEGER,
    PRIMARY KEY (business_id)
);
-- create Business hours
CREATE TABLE Hours (
    business_id     VARCHAR(100),
    openDay         VARCHAR(10),
    openTime        VARCHAR(5),
    closeTime       VARCHAR(5),
    PRIMARY KEY	(business_id, openDay),
    FOREIGN KEY (business_id) REFERENCES Business
);

-- create Business category table
CREATE TABLE MainCategory (
    business_id     VARCHAR(100),
    mainCategory    VARCHAR(150),
    PRIMARY KEY (business_id, mainCategory),
    FOREIGN KEY (business_id) REFERENCES Business
);

-- create Business subcategory table
CREATE TABLE SubCategory (
    business_id     VARCHAR(100),
    subCategory     VARCHAR(150),
    PRIMARY KEY (business_id, subCategory),
    FOREIGN KEY (business_id) REFERENCES Business
);

-- create Attribute table
CREATE TABLE Attribute (
    business_id     VARCHAR(100),
    attribute       VARCHAR(200),
    PRIMARY KEY (business_id, attribute),
    FOREIGN KEY (business_id) REFERENCES Business
);

-- create YelpUser table
CREATE TABLE YelpUser (
    votes_funny     INTEGER,
    votes_useful    INTEGER,
    votes_cool      INTEGER,
    yelping_since   VARCHAR(20),
    review_count    INTEGER,
    user_name       VARCHAR(150) NOT NULL,
    user_id         VARCHAR(50),
    average_stars   NUMBER,
    PRIMARY KEY (user_id)
);

-- create Review table
CREATE TABLE Review (
    votes_funny     INTEGER,
    votes_useful    INTEGER,
    votes_cool      INTEGER,
    user_id         VARCHAR(100) NOT NULL,
    review_id       VARCHAR(100),
    stars           INTEGER,
    review_date     VARCHAR(20),
    text            BLOB,
    business_id     VARCHAR(100) NOT NULL,
    PRIMARY KEY (review_id),
    FOREIGN KEY (business_id) REFERENCES Business,
    FOREIGN KEY (user_id) REFERENCES YelpUser
);

-- create Checkin table
CREATE TABLE Checkin (
    business_id		VARCHAR(100),
    checkin_time	VARCHAR(5),
    checkin_number	INTEGER,
    FOREIGN KEY (business_id) REFERENCES Business
);
	

CREATE INDEX MAINCATEGORY_INDEX ON MainCategory (mainCategory);
CREATE INDEX SUBCATEGORY_INDEX ON SubCategory (subCategory);
CREATE INDEX ATTRIBUTE_INDEX ON Attribute (attribute);
CREATE INDEX BUSINESSCITY_INDEX ON Business (city);
CREATE INDEX OPENDAY_INDEX ON Hours (openDay);
CREATE INDEX OPENTIME_INDEX ON Hours (openTime);
CREATE INDEX CLOSETIME_INDEX ON Hours (closeTime);
CREATE INDEX CHECKINTIME_INDEX ON Checkin (checkin_time);

