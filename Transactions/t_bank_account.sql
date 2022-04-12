DROP TABLE IF EXISTS `t_bank_account`;

CREATE TABLE `t_bank_account`
(
	`id` bigint PRIMARY KEY AUTO_INCREMENT,
	`account_holder_name` varchar(225) NOT NULL,
	`account_no` char(8) UNIQUE NOT NULL,
	`balance` double NOT NULL
	-- `password` varchar(225) NOT NULL
);

INSERT INTO
	`t_bank_account` (`account_holder_name`, `account_no`, `balance`)
VALUES
	('jack', '12345678', 500),
	('alice', '87654321', 1000),
	('bob', '11111111', 1000),
	('smith', '00001111', 0);
