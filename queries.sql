
-- get subcategory VIEW filtered by maincategory
SELECT DISTINCT subCategory
From SubCategory, (SELECT DISTINCT business_id AS selected_bid
                    From MainCategory
                    WHERE mainCategory = 'Restaurants') SelectedMainCate
WHERE SubCategory.business_id = SelectedMainCate.selected_bid;


-- get attributes filered by mainCategory and SubCategory
SELECT DISTINCT attribute
FROM Attribute, (SELECT DISTINCT business_id AS sub_id
                 From SubCategory, (SELECT DISTINCT business_id AS selected_bid
                                    From MainCategory
                                    WHERE mainCategory = 'Restaurants') SelectedMainCate
                WHERE SubCategory.business_id = SelectedMainCate.selected_bid AND SubCategory.subCategory = 'Diners') SelectedSubCate
WHERE Attribute.business_id = SelectedSubCate.sub_id;


-- get business after choosing attributes and locations
SELECT DISTINCT Business.*, checkins.checkinTotal AS checkInNo
FROM Business, (SELECT business_id as bid
                FROM Attribute, (SELECT DISTINCT business_id AS sub_id
                                 From SubCategory, (SELECT DISTINCT business_id AS selected_bid
                                                    From MainCategory
                                                    WHERE mainCategory = 'Restaurants') SelectedMainCate
                                WHERE SubCategory.business_id = SelectedMainCate.selected_bid AND SubCategory.subCategory = 'Diners') SelectedSubCate
                WHERE Attribute.business_id = SelectedSubCate.sub_id) SelectedAttr,
               (SELECT business_id AS checkin_bid, SUM(checkin_number) AS checkinTotal
                FROM Checkin
                GROUP BY business_id) checkins
WHERE Business.business_id = SelectedAttr.bid AND Business.business_id = checkins.checkin_bid 
      AND Business.city = 'Phoenix' AND Business.b_state = 'AZ';


-- -- get sum of checkin number
-- SELECT business_id, SUM(checkin_number) AS checkinTotal
-- FROM Checkin
-- GROUP BY business_id;

-- get all open times
SELECT DISTINCT openTime
FROM Hours;

-- get all close times
SELECT DISTINCT closeTime
FROM Hours;

-- get location of business
SELECT DISTINCT city
FROM Business;

-- get business_id based on open time info
SELECT business_id
FROM Hours
WHERE openDay = 'Monday' AND openTime >= '02:00' AND closeTime <= '22:00';

-- get business filtered by open day and hours
SELECT DISTINCT Business.*, checkins.checkinTotal AS checkInNo
FROM Business, (SELECT business_id as bid
                FROM Attribute, (SELECT DISTINCT business_id AS sub_id
                                 From SubCategory, (SELECT DISTINCT business_id AS selected_bid
                                                    From MainCategory
                                                    WHERE mainCategory = 'Restaurants') SelectedMainCate
                                WHERE SubCategory.business_id = SelectedMainCate.selected_bid AND SubCategory.subCategory = 'Diners') SelectedSubCate
                WHERE Attribute.business_id = SelectedSubCate.sub_id) SelectedAttr,
               (SELECT business_id AS checkin_bid, SUM(checkin_number) AS checkinTotal
                FROM Checkin
                GROUP BY business_id) checkins,
               (SELECT business_id AS hour_bid
                FROM Hours
                WHERE openDay = 'Monday' AND openTime >= 'openhour' AND closeTime <= 'closehour')openHours
WHERE Business.business_id = SelectedAttr.bid AND Business.business_id = checkins.checkin_bid 
      AND Business.business_id = openHours.hour_bid
      AND Business.city = 'Phoenix';


-- get review after choose particular business
SELECT Review.*
FROM Review, (SELECT DISTINCT Business.BUSINESS_ID AS b_bid
              FROM Business, (SELECT business_id as bid
                            FROM Attribute, (SELECT DISTINCT business_id AS sub_id
                                             From SubCategory, (SELECT DISTINCT business_id AS selected_bid
                                                                From MainCategory
                                                                WHERE mainCategory = 'Restaurants') SelectedMainCate
                                            WHERE SubCategory.business_id = SelectedMainCate.selected_bid AND SubCategory.subCategory = 'Diners') SelectedSubCate
                            WHERE Attribute.business_id = SelectedSubCate.sub_id) SelectedAttr,
                           (SELECT business_id AS hour_bid
                            FROM Hours
                            WHERE openDay = 'Monday' AND openTime >= 'openhour' AND closeTime <= 'closehour')openHours
            WHERE Business.business_id = SelectedAttr.bid 
                  AND Business.business_id = openHours.hour_bid
                  AND Business.city = 'Phoenix') selectedBusiness
WHERE Review.BUSINESS_ID = selectedBusiness.b_bid;



        