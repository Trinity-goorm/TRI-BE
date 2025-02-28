USE ctc;

-- User 테이블 더미 데이터 삽입
INSERT INTO ctc.user (empty_ticket_count, is_deleted, normal_ticket_count, sex, id, image_url, nickname, phone_number,
                      status)
VALUES (1, FALSE, 10, 'MALE', 1, 'https://example.com/image1.jpg', 'User1', '010-1001-1001', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 2, 'https://example.com/image2.jpg', 'User2', '010-1002-1002', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 3, 'https://example.com/image3.jpg', 'User3', '010-1003-1003', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 4, 'https://example.com/image4.jpg', 'User4', '010-1004-1004', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 5, 'https://example.com/image5.jpg', 'User5', '010-1005-1005', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 6, 'https://example.com/image6.jpg', 'User6', '010-1006-1006', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 7, 'https://example.com/image7.jpg', 'User7', '010-1007-1007', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 8, 'https://example.com/image8.jpg', 'User8', '010-1008-1008', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 9, 'https://example.com/image9.jpg', 'User9', '010-1009-1009', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 10, 'https://example.com/image10.jpg', 'User10', '010-1010-1010', 'AVAILABLE'),
       (1, FALSE, 10, 'MALE', 11, 'https://example.com/image11.jpg', 'User11', '010-1011-1011', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 12, 'https://example.com/image12.jpg', 'User12', '010-1012-1012', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 13, 'https://example.com/image13.jpg', 'User13', '010-1013-1013', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 14, 'https://example.com/image14.jpg', 'User14', '010-1014-1014', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 15, 'https://example.com/image15.jpg', 'User15', '010-1015-1015', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 16, 'https://example.com/image16.jpg', 'User16', '010-1016-1016', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 17, 'https://example.com/image17.jpg', 'User17', '010-1017-1017', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 18, 'https://example.com/image18.jpg', 'User18', '010-1018-1018', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 19, 'https://example.com/image19.jpg', 'User19', '010-1019-1019', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 20, 'https://example.com/image20.jpg', 'User20', '010-1020-1020', 'AVAILABLE');

INSERT INTO ctc.user (empty_ticket_count, is_deleted, normal_ticket_count, sex, id, image_url, nickname, phone_number,
                      status)
VALUES (1, FALSE, 10, 'MALE', 21, 'https://example.com/image21.jpg', 'User21', '010-1021-1021', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 22, 'https://example.com/image22.jpg', 'User22', '010-1022-1022', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 23, 'https://example.com/image23.jpg', 'User23', '010-1023-1023', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 24, 'https://example.com/image24.jpg', 'User24', '010-1024-1024', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 25, 'https://example.com/image25.jpg', 'User25', '010-1025-1025', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 26, 'https://example.com/image26.jpg', 'User26', '010-1026-1026', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 27, 'https://example.com/image27.jpg', 'User27', '010-1027-1027', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 28, 'https://example.com/image28.jpg', 'User28', '010-1028-1028', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 29, 'https://example.com/image29.jpg', 'User29', '010-1029-1029', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 30, 'https://example.com/image30.jpg', 'User30', '010-1030-1030', 'AVAILABLE'),
       (1, FALSE, 10, 'MALE', 31, 'https://example.com/image31.jpg', 'User31', '010-1031-1031', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 32, 'https://example.com/image32.jpg', 'User32', '010-1032-1032', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 33, 'https://example.com/image33.jpg', 'User33', '010-1033-1033', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 34, 'https://example.com/image34.jpg', 'User34', '010-1034-1034', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 35, 'https://example.com/image35.jpg', 'User35', '010-1035-1035', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 36, 'https://example.com/image36.jpg', 'User36', '010-1036-1036', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 37, 'https://example.com/image37.jpg', 'User37', '010-1037-1037', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 38, 'https://example.com/image38.jpg', 'User38', '010-1038-1038', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 39, 'https://example.com/image39.jpg', 'User39', '010-1039-1039', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 40, 'https://example.com/image40.jpg', 'User40', '010-1040-1040', 'AVAILABLE');

INSERT INTO ctc.user (empty_ticket_count, is_deleted, normal_ticket_count, sex, id, image_url, nickname, phone_number,
                      status)
VALUES (1, FALSE, 10, 'MALE', 41, 'https://example.com/image41.jpg', 'User41', '010-1041-1041', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 42, 'https://example.com/image42.jpg', 'User42', '010-1042-1042', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 43, 'https://example.com/image43.jpg', 'User43', '010-1043-1043', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 44, 'https://example.com/image44.jpg', 'User44', '010-1044-1044', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 45, 'https://example.com/image45.jpg', 'User45', '010-1045-1045', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 46, 'https://example.com/image46.jpg', 'User46', '010-1046-1046', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 47, 'https://example.com/image47.jpg', 'User47', '010-1047-1047', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 48, 'https://example.com/image48.jpg', 'User48', '010-1048-1048', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 49, 'https://example.com/image49.jpg', 'User49', '010-1049-1049', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 50, 'https://example.com/image50.jpg', 'User50', '010-1050-1050', 'AVAILABLE'),
       (1, FALSE, 10, 'MALE', 51, 'https://example.com/image51.jpg', 'User51', '010-1051-1051', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 52, 'https://example.com/image52.jpg', 'User52', '010-1052-1052', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 53, 'https://example.com/image53.jpg', 'User53', '010-1053-1053', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 54, 'https://example.com/image54.jpg', 'User54', '010-1054-1054', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 55, 'https://example.com/image55.jpg', 'User55', '010-1055-1055', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 56, 'https://example.com/image56.jpg', 'User56', '010-1056-1056', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 57, 'https://example.com/image57.jpg', 'User57', '010-1057-1057', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 58, 'https://example.com/image58.jpg', 'User58', '010-1058-1058', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 59, 'https://example.com/image59.jpg', 'User59', '010-1059-1059', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 60, 'https://example.com/image60.jpg', 'User60', '010-1060-1060', 'AVAILABLE');

INSERT INTO ctc.user (empty_ticket_count, is_deleted, normal_ticket_count, sex, id, image_url, nickname, phone_number,
                      status)
VALUES (1, FALSE, 10, 'MALE', 61, 'https://example.com/image61.jpg', 'User61', '010-1061-1061', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 62, 'https://example.com/image62.jpg', 'User62', '010-1062-1062', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 63, 'https://example.com/image63.jpg', 'User63', '010-1063-1063', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 64, 'https://example.com/image64.jpg', 'User64', '010-1064-1064', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 65, 'https://example.com/image65.jpg', 'User65', '010-1065-1065', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 66, 'https://example.com/image66.jpg', 'User66', '010-1066-1066', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 67, 'https://example.com/image67.jpg', 'User67', '010-1067-1067', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 68, 'https://example.com/image68.jpg', 'User68', '010-1068-1068', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 69, 'https://example.com/image69.jpg', 'User69', '010-1069-1069', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 70, 'https://example.com/image70.jpg', 'User70', '010-1070-1070', 'AVAILABLE'),
       (1, FALSE, 10, 'MALE', 71, 'https://example.com/image71.jpg', 'User71', '010-1071-1071', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 72, 'https://example.com/image72.jpg', 'User72', '010-1072-1072', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 73, 'https://example.com/image73.jpg', 'User73', '010-1073-1073', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 74, 'https://example.com/image74.jpg', 'User74', '010-1074-1074', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 75, 'https://example.com/image75.jpg', 'User75', '010-1075-1075', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 76, 'https://example.com/image76.jpg', 'User76', '010-1076-1076', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 77, 'https://example.com/image77.jpg', 'User77', '010-1077-1077', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 78, 'https://example.com/image78.jpg', 'User78', '010-1078-1078', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 79, 'https://example.com/image79.jpg', 'User79', '010-1079-1079', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 80, 'https://example.com/image80.jpg', 'User80', '010-1080-1080', 'AVAILABLE');

INSERT INTO ctc.user (empty_ticket_count, is_deleted, normal_ticket_count, sex, id, image_url, nickname, phone_number,
                      status)
VALUES (1, FALSE, 10, 'MALE', 81, 'https://example.com/image81.jpg', 'User81', '010-1081-1081', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 82, 'https://example.com/image82.jpg', 'User82', '010-1082-1082', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 83, 'https://example.com/image83.jpg', 'User83', '010-1083-1083', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 84, 'https://example.com/image84.jpg', 'User84', '010-1084-1084', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 85, 'https://example.com/image85.jpg', 'User85', '010-1085-1085', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 86, 'https://example.com/image86.jpg', 'User86', '010-1086-1086', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 87, 'https://example.com/image87.jpg', 'User87', '010-1087-1087', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 88, 'https://example.com/image88.jpg', 'User88', '010-1088-1088', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 89, 'https://example.com/image89.jpg', 'User89', '010-1089-1089', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 90, 'https://example.com/image90.jpg', 'User90', '010-1090-1090', 'AVAILABLE'),
       (1, FALSE, 10, 'MALE', 91, 'https://example.com/image91.jpg', 'User91', '010-1091-1091', 'AVAILABLE'),
       (2, FALSE, 20, 'FEMALE', 92, 'https://example.com/image92.jpg', 'User92', '010-1092-1092', 'AVAILABLE'),
       (3, TRUE, 30, 'MALE', 93, 'https://example.com/image93.jpg', 'User93', '010-1093-1093', 'AVAILABLE'),
       (4, FALSE, 40, 'FEMALE', 94, 'https://example.com/image94.jpg', 'User94', '010-1094-1094', 'AVAILABLE'),
       (5, FALSE, 50, 'MALE', 95, 'https://example.com/image95.jpg', 'User95', '010-1095-1095', 'AVAILABLE'),
       (1, FALSE, 60, 'FEMALE', 96, 'https://example.com/image96.jpg', 'User96', '010-1096-1096', 'AVAILABLE'),
       (2, FALSE, 70, 'MALE', 97, 'https://example.com/image97.jpg', 'User97', '010-1097-1097', 'AVAILABLE'),
       (3, TRUE, 80, 'FEMALE', 98, 'https://example.com/image98.jpg', 'User98', '010-1098-1098', 'AVAILABLE'),
       (4, FALSE, 90, 'MALE', 99, 'https://example.com/image99.jpg', 'User99', '010-1099-1099', 'AVAILABLE'),
       (5, FALSE, 100, 'FEMALE', 100, 'https://example.com/image100.jpg', 'User100', '010-1100-1100', 'AVAILABLE');


-- UserPreference 테이블 더미 데이터 삽입
INSERT INTO ctc.user_preference (max_price, min_price, user_id)
VALUES (50000, 10000, 1),
       (60000, 15000, 2),
       (70000, 20000, 3),
       (40000, 12000, 4),
       (50000, 18000, 5),
       (60000, 20000, 6),
       (70000, 25000, 7),
       (40000, 13000, 8),
       (50000, 17000, 9),
       (60000, 22000, 10),
       (50000, 10000, 11),
       (60000, 15000, 12),
       (70000, 20000, 13),
       (40000, 12000, 14),
       (50000, 18000, 15),
       (60000, 20000, 16),
       (70000, 25000, 17),
       (40000, 13000, 18),
       (50000, 17000, 19),
       (60000, 22000, 20);

INSERT INTO ctc.user_preference (max_price, min_price, user_id)
VALUES (50000, 10000, 21),
       (60000, 15000, 22),
       (70000, 20000, 23),
       (40000, 12000, 24),
       (50000, 18000, 25),
       (60000, 20000, 26),
       (70000, 25000, 27),
       (40000, 13000, 28),
       (50000, 17000, 29),
       (60000, 22000, 30),
       (50000, 10000, 31),
       (60000, 15000, 32),
       (70000, 20000, 33),
       (40000, 12000, 34),
       (50000, 18000, 35),
       (60000, 20000, 36),
       (70000, 25000, 37),
       (40000, 13000, 38),
       (50000, 17000, 39),
       (60000, 22000, 40);

INSERT INTO ctc.user_preference (max_price, min_price, user_id)
VALUES (50000, 10000, 41),
       (60000, 15000, 42),
       (70000, 20000, 43),
       (40000, 12000, 44),
       (50000, 18000, 45),
       (60000, 20000, 46),
       (70000, 25000, 47),
       (40000, 13000, 48),
       (50000, 17000, 49),
       (60000, 22000, 50),
       (50000, 10000, 51),
       (60000, 15000, 52),
       (70000, 20000, 53),
       (40000, 12000, 54),
       (50000, 18000, 55),
       (60000, 20000, 56),
       (70000, 25000, 57),
       (40000, 13000, 58),
       (50000, 17000, 59),
       (60000, 22000, 60);

INSERT INTO ctc.user_preference (max_price, min_price, user_id)
VALUES (50000, 10000, 61),
       (60000, 15000, 62),
       (70000, 20000, 63),
       (40000, 12000, 64),
       (50000, 18000, 65),
       (60000, 20000, 66),
       (70000, 25000, 67),
       (40000, 13000, 68),
       (50000, 17000, 69),
       (60000, 22000, 70),
       (50000, 10000, 71),
       (60000, 15000, 72),
       (70000, 20000, 73),
       (40000, 12000, 74),
       (50000, 18000, 75),
       (60000, 20000, 76),
       (70000, 25000, 77),
       (40000, 13000, 78),
       (50000, 17000, 79),
       (60000, 22000, 80);

INSERT INTO ctc.user_preference (max_price, min_price, user_id)
VALUES (50000, 10000, 81),
       (60000, 15000, 82),
       (70000, 20000, 83),
       (40000, 12000, 84),
       (50000, 18000, 85),
       (60000, 20000, 86),
       (70000, 25000, 87),
       (40000, 13000, 88),
       (50000, 17000, 89),
       (60000, 22000, 90),
       (50000, 10000, 91),
       (60000, 15000, 92),
       (70000, 20000, 93),
       (40000, 12000, 94),
       (50000, 18000, 95),
       (60000, 20000, 96),
       (70000, 25000, 97),
       (40000, 13000, 98),
       (50000, 17000, 99),
       (60000, 22000, 100);

-- UserPreferenceCategory 테이블 더미 데이터 삽입
-- 1st Batch (1~20)
INSERT INTO ctc.user_preference_category (category_id, user_preference_id)
VALUES (1, 1),
       (2, 1),
       (3, 1),
       (1, 2),
       (2, 2),
       (3, 2),
       (1, 3),
       (2, 3),
       (3, 3),
       (1, 4),
       (2, 4),
       (3, 4),
       (1, 5),
       (2, 5),
       (3, 5),
       (1, 6),
       (2, 6),
       (3, 6),
       (1, 7),
       (2, 7),
       (3, 7),
       (1, 8),
       (2, 8),
       (3, 8),
       (1, 9),
       (2, 9),
       (3, 9),
       (1, 10),
       (2, 10),
       (3, 10),
       (1, 11),
       (2, 11),
       (3, 11),
       (1, 12),
       (2, 12),
       (3, 12),
       (1, 13),
       (2, 13),
       (3, 13),
       (1, 14),
       (2, 14),
       (3, 14),
       (1, 15),
       (2, 15),
       (3, 15),
       (1, 16),
       (2, 16),
       (3, 16),
       (1, 17),
       (2, 17),
       (3, 17),
       (1, 18),
       (2, 18),
       (3, 18),
       (1, 19),
       (2, 19),
       (3, 19),
       (1, 20),
       (2, 20),
       (3, 20);

-- 2nd Batch (21~40)
INSERT INTO ctc.user_preference_category (category_id, user_preference_id)
VALUES (1, 21),
       (2, 21),
       (3, 21),
       (1, 22),
       (2, 22),
       (3, 22),
       (1, 23),
       (2, 23),
       (3, 23),
       (1, 24),
       (2, 24),
       (3, 24),
       (1, 25),
       (2, 25),
       (3, 25),
       (1, 26),
       (2, 26),
       (3, 26),
       (1, 27),
       (2, 27),
       (3, 27),
       (1, 28),
       (2, 28),
       (3, 28),
       (1, 29),
       (2, 29),
       (3, 29),
       (1, 30),
       (2, 30),
       (3, 30),
       (1, 31),
       (2, 31),
       (3, 31),
       (1, 32),
       (2, 32),
       (3, 32),
       (1, 33),
       (2, 33),
       (3, 33),
       (1, 34),
       (2, 34),
       (3, 34),
       (1, 35),
       (2, 35),
       (3, 35),
       (1, 36),
       (2, 36),
       (3, 36),
       (1, 37),
       (2, 37),
       (3, 37),
       (1, 38),
       (2, 38),
       (3, 38),
       (1, 39),
       (2, 39),
       (3, 39),
       (1, 40),
       (2, 40),
       (3, 40);

-- 3rd Batch (41~60)
INSERT INTO ctc.user_preference_category (category_id, user_preference_id)
VALUES (1, 41),
       (2, 41),
       (3, 41),
       (1, 42),
       (2, 42),
       (3, 42),
       (1, 43),
       (2, 43),
       (3, 43),
       (1, 44),
       (2, 44),
       (3, 44),
       (1, 45),
       (2, 45),
       (3, 45),
       (1, 46),
       (2, 46),
       (3, 46),
       (1, 47),
       (2, 47),
       (3, 47),
       (1, 48),
       (2, 48),
       (3, 48),
       (1, 49),
       (2, 49),
       (3, 49),
       (1, 50),
       (2, 50),
       (3, 50),
       (1, 51),
       (2, 51),
       (3, 51),
       (1, 52),
       (2, 52),
       (3, 52),
       (1, 53),
       (2, 53),
       (3, 53),
       (1, 54),
       (2, 54),
       (3, 54),
       (1, 55),
       (2, 55),
       (3, 55),
       (1, 56),
       (2, 56),
       (3, 56),
       (1, 57),
       (2, 57),
       (3, 57),
       (1, 58),
       (2, 58),
       (3, 58),
       (1, 59),
       (2, 59),
       (3, 59),
       (1, 60),
       (2, 60),
       (3, 60);

-- 4th Batch (61~80)
INSERT INTO ctc.user_preference_category (category_id, user_preference_id)
VALUES (1, 61),
       (2, 61),
       (3, 61),
       (1, 62),
       (2, 62),
       (3, 62),
       (1, 63),
       (2, 63),
       (3, 63),
       (1, 64),
       (2, 64),
       (3, 64),
       (1, 65),
       (2, 65),
       (3, 65),
       (1, 66),
       (2, 66),
       (3, 66),
       (1, 67),
       (2, 67),
       (3, 67),
       (1, 68),
       (2, 68),
       (3, 68),
       (1, 69),
       (2, 69),
       (3, 69),
       (1, 70),
       (2, 70),
       (3, 70),
       (1, 71),
       (2, 71),
       (3, 71),
       (1, 72),
       (2, 72),
       (3, 72),
       (1, 73),
       (2, 73),
       (3, 73),
       (1, 74),
       (2, 74),
       (3, 74),
       (1, 75),
       (2, 75),
       (3, 75),
       (1, 76),
       (2, 76),
       (3, 76),
       (1, 77),
       (2, 77),
       (3, 77),
       (1, 78),
       (2, 78),
       (3, 78),
       (1, 79),
       (2, 79),
       (3, 79),
       (1, 80),
       (2, 80),
       (3, 80);

-- 5th Batch (81~100)
INSERT INTO ctc.user_preference_category (category_id, user_preference_id)
VALUES (1, 81),
       (2, 81),
       (3, 81),
       (1, 82),
       (2, 82),
       (3, 82),
       (1, 83),
       (2, 83),
       (3, 83),
       (1, 84),
       (2, 84),
       (3, 84),
       (1, 85),
       (2, 85),
       (3, 85),
       (1, 86),
       (2, 86),
       (3, 86),
       (1, 87),
       (2, 87),
       (3, 87),
       (1, 88),
       (2, 88),
       (3, 88),
       (1, 89),
       (2, 89),
       (3, 89),
       (1, 90),
       (2, 90),
       (3, 90),
       (1, 91),
       (2, 91),
       (3, 91),
       (1, 92),
       (2, 92),
       (3, 92),
       (1, 93),
       (2, 93),
       (3, 93),
       (1, 94),
       (2, 94),
       (3, 94),
       (1, 95),
       (2, 95),
       (3, 95),
       (1, 96),
       (2, 96),
       (3, 96),
       (1, 97),
       (2, 97),
       (3, 97),
       (1, 98),
       (2, 98),
       (3, 98),
       (1, 99),
       (2, 99),
       (3, 99),
       (1, 100),
       (2, 100),
       (3, 100);

-- Reservation Time 테이블
INSERT INTO ctc.reservation_time
    (created_at, id, time_slot)
VALUES (NOW(), 1, '09:00'),
       (NOW(), 2, '10:00'),
       (NOW(), 3, '11:00'),
       (NOW(), 4, '12:00'),
       (NOW(), 5, '13:00'),
       (NOW(), 6, '14:00'),
       (NOW(), 7, '15:00'),
       (NOW(), 8, '16:00'),
       (NOW(), 9, '17:00'),
       (NOW(), 10, '18:00'),
       (NOW(), 11, '19:00');

-- Seat Type 테이블
INSERT INTO ctc.seat_type
    (max_capacity, min_capacity, id)
VALUES (2, 1, 1), -- Seat Type 1: 최소 1명, 최대 2명
       (4, 3, 2), -- Seat Type 2: 최소 3명, 최대 4명
       (6, 5, 3); -- Seat Type 3: 최소 5명, 최대 6명

-- Seat Notification Message 테이블
# INSERT INTO ctc.seat_notification_message
#     (`id`, `seat_availability_id`, `body`, `title`, `url`)
# VALUES (1, 1, 'title1', 'body1', 'url1'),
#        (2, 2, 'title2', 'body2', 'url2'),
#        (3, 3, 'title3', 'body3', 'url3'),
#        (4, 4, 'title4', 'body4', 'url4'),
#        (5, 5, 'title5', 'body5', 'url5'),
#        (6, 6, 'title6', 'body6', 'url6'),
#        (7, 7, 'title7', 'body7', 'url7'),
#        (8, 8, 'title8', 'body8', 'url8'),
#        (9, 9, 'title9', 'body9', 'url9'),
#        (10, 10, 'title10', 'body10', 'url10');

-- Seat Notification 테이블
# INSERT INTO ctc.seat_notification
#     (`id`, `seat_notification_message_id`, `user_id`)
# VALUES (1, 1, 1),
#        (2, 2, 1),
#        (3, 3, 1),
#        (4, 4, 1),
#        (5, 5, 1),
#        (6, 6, 2),
#        (7, 7, 2),
#        (8, 8, 2),
#        (9, 9, 2),
#        (10, 10, 2),
#        (11, 1, 3),
#        (12, 2, 3),
#        (13, 3, 3),
#        (14, 4, 3),
#        (15, 5, 3);

-- Reservation 테이블
# INSERT INTO ctc.reservation
# (`reservation_date`, `created_at`, `id`, reservation_time_id, restaurant_id, seat_type_id, user_id, status)
# VALUES ('2025-02-24', NOW(), 1, 1, 1, 1, 1, 'COMPLETED')
