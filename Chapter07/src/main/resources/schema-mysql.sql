CREATE TABLE customer  (
  id BIGINT  NOT NULL PRIMARY KEY ,
  firstName VARCHAR(11) NOT NULL ,
  middleInitial VARCHAR(1),
  lastName VARCHAR(10) NOT NULL,
  addressNumber VARCHAR(24) NOT NULL,
  street VARCHAR(20) NOT NULL,
  address VARCHAR(45) NOT NULL,
  city VARCHAR(16) NOT NULL,
  state CHAR(2) NOT NULL,
  zipCode CHAR(5)
);

DELIMITER //

CREATE PROCEDURE customer_list(IN cityOption CHAR(16))
  BEGIN
    SELECT * FROM customer
    WHERE city = cityOption;
  END //

DELIMITER ;