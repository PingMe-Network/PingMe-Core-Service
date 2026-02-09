-- ===========================================================================================================
--
-- Bước 1: Drop DB hiện tại
-- Bước 2: Tạo lại DB đó (tên: PingMe_DB)
-- Bước 3: Chạy lại application để nó tự động tạo bảng
-- Bước 4: Chạy script sql bên dưới để nó thêm dữ liệu
--
-- ===========================================================================================================
-- Tạo Role
-- 2 Role:
-- + MEMBER
-- + ADMIN: cho phép truy cập trang quản lý trên frontend
-- ===========================================================================================================
INSERT INTO `roles` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `description`, `name`)
VALUES (1, 1, '2025-11-23 10:37:30.000000', 'anonymousUser', '2025-11-23 10:37:33.000000', 'anonymousUser',
'Admin Role', 'ADMIN'),
(2, 1, '2025-11-23 10:38:08.000000', 'anonymousUser', '2025-11-23 10:38:09.000000', 'anonymousUser',
'Member Role', 'MEMBER');

-- ===========================================================================================================
-- Tạo User
-- Tất cả User tạo example ra đều có pass là Test123@
--
-- Các user có quyền ADMIN:
-- HuynhDucPhu2502@gmail.com / tranlehuygia2210@gmail.com / atvn15@gmail.com / shibalnq2112@gmail.com
--
-- Các user còn lại:
-- Test1@gmail.com / Test2@gmail.com / ... / Test10@gmail.com
-- ===========================================================================================================
INSERT INTO `users` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `address`, `auth_provider`,
                     `avatar_url`, `dob`, `email`, `gender`, `name`, `password`, `status`, `role_id`, `account_status`)
VALUES
-- User cũ (1-15) set ACTIVE
(1, 1, '2025-11-18 22:48:59.615037', 'anonymousUser', '2025-11-18 23:05:04.360119', 'Test1@gmail.com',
 '56/9 Lạc Long Quân, P.5, Q.11, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test1@gmail.com', '2025-07-17', 'Test1@gmail.com',
 'MALE', 'Phạm Tuấn Khoa', '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'ACTIVE'),

(2, 1, '2025-11-18 22:49:47.075581', 'anonymousUser', '2025-11-18 23:05:42.434655', 'Test2@gmail.com',
 '10/9 Lê Duẩn, P.Thắng Nhất, TP.Vũng Tàu', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test2@gmail.com', '2025-09-10', 'Test2@gmail.com',
 'FEMALE', 'Trần Thị Thu Trang', '$2a$10$GiN9ZRoCKZDMmyjQ08LjAOGcLUDISkywdkDdMEsOc1ayTSctpAP82', 'OFFLINE', 2, 'ACTIVE'),

(3, 1, '2025-11-18 22:51:09.449569', 'anonymousUser', '2025-11-18 23:06:20.796721', 'Test3@gmail.com',
 '45 Nguyễn Tất Thành, P.Vĩnh Thọ, Nha Trang', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test3@gmail.com', '2025-04-30', 'Test3@gmail.com',
 'MALE', 'Bùi Anh Quân', '$2a$10$ND9jQ7/ybwMp.8XBIiw4k.hJtg2fvxqusVTA/c7AWnqZjN/2yucgG', 'OFFLINE', 2, 'ACTIVE'),

(4, 1, '2025-11-18 22:53:00.105600', 'anonymousUser', '2025-11-18 23:09:24.414137', 'Test4@gmail.com',
 '68/2 Nguyễn Văn Trỗi, P.8, Phú Nhuận, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test4@gmail.com', '2001-04-17', 'Test4@gmail.com',
 'FEMALE', 'Phạm Bảo Ngọc', '$2a$10$.L99JYyUqk4DIKhrvUfv4OAaQw3DpGgdlUA9PCIPIg1u55RX.mMaK', 'OFFLINE', 2, 'ACTIVE'),

(5, 1, '2025-11-18 22:54:09.992369', 'anonymousUser', '2025-11-18 23:09:54.900420', 'Test5@gmail.com',
 '112 Trần Hưng Đạo, P.Mỹ Bình, Long Xuyên, An Giang', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test5@gmail.com', '2001-01-09', 'Test5@gmail.com',
 'MALE', 'Võ Nhật Long', '$2a$10$VdLRPrwdjcO.J0Vq.MO2Iu2tNd9flFZ6tFasACEMOmTT0Rb96nrB2', 'OFFLINE', 2, 'ACTIVE'),

(6, 1, '2025-11-18 22:57:19.534273', 'anonymousUser', '2025-11-18 23:16:32.044163', 'Test6@gmail.com',
 '33 Nguyễn Thị Minh Khai, P.Bến Thành, Q.1, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test6@gmail.com', '2000-09-13', 'Test6@gmail.com',
 'FEMALE', 'Trần Quế My', '$2a$10$avKIRLICxePlrPnThNhALO0mltduU4OcgOoUnTmqXzBWT6Dv.Vwce', 'OFFLINE', 2, 'ACTIVE'),

(7, 1, '2025-11-18 22:58:40.727272', 'anonymousUser', '2025-11-18 23:19:05.678958', 'Test7@gmail.com',
 '23/7 Hoàng Hoa Thám, P.6, Q.Bình Thạnh, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test7@gmail.com', '2025-06-21', 'Test7@gmail.com',
 'MALE', 'Đặng Xuân Nam', '$2a$10$/R47zjqACeJvZUcgqvEV1uJtkGz96KGfKHM6L/.qVJcqNnOeBwUbi', 'OFFLINE', 2, 'ACTIVE'),

(8, 1, '2025-11-18 22:59:51.828506', 'anonymousUser', '2025-11-18 23:21:32.273121', 'Test8@gmail.com',
 '50 Tôn Đức Thắng, P.Bến Nghé, Q.1, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test8@gmail.com', '2003-10-18', 'Test8@gmail.com',
 'FEMALE', 'Phạm Hải Yến', '$2a$10$L5q7vVIc4cELw6cJ.pOYOOhMGBmnTB3kkbgDAdknKwapXlMqdV19K', 'OFFLINE', 2, 'ACTIVE'),

(9, 1, '2025-11-18 23:03:47.999784', 'anonymousUser', '2025-11-18 23:23:14.336209', 'Test9@gmail.com',
 '18A Hà Huy Tập, P.Tân Lợi, TP.Buôn Ma Thuột', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test9@gmail.com', '2000-12-03', 'Test9@gmail.com',
 'MALE', 'Nguyễn Tấn Tín', '$2a$10$LXf6qogfe2XJr.BM7Of4YO3P8InO8Fgs2Up9rsn7FjZ4Mx8hZzyfG', 'OFFLINE', 2, 'ACTIVE'),

(10, 1, '2025-11-18 23:04:34.924416', 'anonymousUser', '2025-11-18 23:23:48.527676', 'Test10@gmail.com',
 '44/7 Phan Đăng Lưu, P.3, Q.Bình Thạnh, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test10@gmail.com', '2002-06-25', 'Test10@gmail.com',
 'FEMALE', 'Đỗ Hoài Như', '$2a$10$xTxJpev9CJ8QxrXpTc/RmO7KayY9lDCakEFKxKossIoyneB0Oxq16', 'OFFLINE', 2, 'ACTIVE'),

(11, 1, '2025-11-18 23:06:50.393330', 'anonymousUser', '2025-11-18 23:36:04.009499', 'huynhducphu2502@gmail.com',
 '120 Xóm Chiếu, Phường 14, Quận 4, TPHCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/huynhducphu2502@gmail.com', '2003-02-25',
 'huynhducphu2502@gmail.com', 'MALE', 'Huỳnh Đức Phú',
 '$2a$10$qVGedE0iLfFMkBhsFqvNte571l38ZbQLO8luBK9xF0qbKvaclm7tW', 'OFFLINE', 1, 'ACTIVE'),

(12, 1, '2025-11-23 10:43:51.329198', 'anonymousUser', '2025-11-23 10:43:51.329198', 'anonymousUser', '',
 'LOCAL', NULL, '2025-11-01', 'tranlehuygia2210@gmail.com', 'MALE', 'Lê Trần Gia Huy',
 '$2a$10$aymbUZwxdgH7YdEVd51Qd.DQV6S2x3wValMrEwpPkoRr1p1c/v9bW', NULL, 1, 'ACTIVE'),

(13, 1, '2025-11-23 10:44:34.041359', 'anonymousUser', '2025-11-23 10:44:34.041359', 'anonymousUser', '',
 'LOCAL', NULL, '2025-11-01', 'atvn15@gmail.com', 'MALE', 'Nguyễn Anh Tùng',
 '$2a$10$5qVqbH5DNRDR0jqihcBa6.QxGSKT1VfIXf.NVUR1CrwCTY.71LffK', NULL, 1, 'ACTIVE'),

(14, 1, '2025-11-23 10:45:03.711495', 'anonymousUser', '2025-11-23 10:45:03.711495', 'anonymousUser', '',
 'LOCAL', NULL, '2025-11-01', 'shibalnq2112@gmail.com', 'MALE', 'Lê Nguyễn Quỳnh',
 '$2a$10$rjodqTNy1AaT.PXOzdIqt.MtzO8mhbbSQ8XK/ki2MGjoq.G4ZzgX2', NULL, 1, 'ACTIVE'),

(15, 1, '2025-11-23 11:00:28.015457', 'anonymousUser', '2025-11-23 11:00:28.015457', 'anonymousUser', '',
 'LOCAL', NULL, '2025-11-01', 'nhpel11@gmail.com', 'MALE', 'Phạm Ngọc Hùng',
 '$2a$10$AZ15eoSTJsSYffcY7eGsNeA5MovUgMlzooG5DKQLcE.6tFPyAA5pq', NULL, 1, 'ACTIVE'),

-- User mới (16-20) set DEACTIVATED
(16, 1, '2025-12-01 08:00:00.000000', 'admin', '2025-12-01 08:00:00.000000', 'admin', '123 Đường A, Quận 1',
 'LOCAL', NULL, '1999-01-01', 'Test16_Deactive@gmail.com', 'MALE', 'Nguyễn Văn Bị Khóa',
 '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'DEACTIVATED'),

(17, 1, '2025-12-01 08:05:00.000000', 'admin', '2025-12-01 08:05:00.000000', 'admin', '456 Đường B, Quận 2',
 'LOCAL', NULL, '1998-05-05', 'Test17_Deactive@gmail.com', 'FEMALE', 'Trần Thị Vô Hiệu',
 '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'DEACTIVATED'),

(18, 1, '2025-12-01 08:10:00.000000', 'admin', '2025-12-01 08:10:00.000000', 'admin', '789 Đường C, Quận 3',
 'LOCAL', NULL, '2000-10-10', 'Test18_Deactive@gmail.com', 'MALE', 'Lê Văn Cấm',
 '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'DEACTIVATED'),

(19, 1, '2025-12-01 08:15:00.000000', 'admin', '2025-12-01 08:15:00.000000', 'admin', '321 Đường D, Quận 4',
 'LOCAL', NULL, '1995-12-12', 'Test19_Deactive@gmail.com', 'FEMALE', 'Phạm Thị Dừng',
 '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'DEACTIVATED'),

(20, 1, '2025-12-01 08:20:00.000000', 'admin', '2025-12-01 08:20:00.000000', 'admin', '654 Đường E, Quận 5',
 'LOCAL', NULL, '1997-07-07', 'Test20_Deactive@gmail.com', 'MALE', 'Hoàng Văn Chặn',
 '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'DEACTIVATED'),

(21, 1, '2025-11-18 22:48:59.615037', 'anonymousUser', '2025-11-18 23:05:04.360119', 'phatdang19032004@gmail.com',
 '56/9 Lạc Long Quân, P.5, Q.11, TP.HCM', 'LOCAL',
 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/avatar/Test1@gmail.com', '2025-07-17', 'phatdang19032004@gmail.com',
 'MALE', 'Meo Ngot', '$2a$10$6RAjVIU8XxvwJ2ewoDPoAeCUlKJqH7gRHaashHrwkwVv0WKm5/z5e', 'OFFLINE', 2, 'NON_ACTIVATED');
-- ===========================================================================================================
-- Tạo mối quan hệ giữa các User (chủ yếu là tài khoản HuynhDucPhu2502@gmail.com với 9 user TestX@gmail.com)
-- ===========================================================================================================
INSERT INTO `friendships` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `status`,
`user_high_id`, `user_low_id`, `user_a_id`, `user_b_id`)
VALUES (1, 1, '2025-11-18 23:07:19.344422', 'Test3@gmail.com', '2025-11-18 23:24:46.665294',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 3, 3, 11),
(2, 1, '2025-11-18 23:07:38.791005', 'Test1@gmail.com', '2025-11-18 23:24:46.171186',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 1, 1, 11),
(3, 1, '2025-11-18 23:08:07.691509', 'Test2@gmail.com', '2025-11-18 23:24:45.639012',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 2, 2, 11),
(4, 1, '2025-11-18 23:10:04.110379', 'Test5@gmail.com', '2025-11-18 23:24:44.638708',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 5, 5, 11),
(5, 1, '2025-11-18 23:10:29.852871', 'Test4@gmail.com', '2025-11-18 23:24:44.107127',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 4, 4, 11),
(6, 1, '2025-11-18 23:18:22.722881', 'Test6@gmail.com', '2025-11-18 23:24:43.524639',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 6, 6, 11),
(7, 1, '2025-11-18 23:19:29.490937', 'Test7@gmail.com', '2025-11-18 23:24:42.997787',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 7, 7, 11),
(8, 1, '2025-11-18 23:21:02.702739', 'Test8@gmail.com', '2025-11-18 23:24:42.401438',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 8, 8, 11),
(9, 1, '2025-11-18 23:24:00.662050', 'Test10@gmail.com', '2025-11-18 23:24:41.848693',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 10, 10, 11),
(10, 1, '2025-11-18 23:24:18.596440', 'Test9@gmail.com', '2025-11-18 23:24:41.084521',
'huynhducphu2502@gmail.com', 'ACCEPTED', 11, 9, 9, 11);

-- ===========================================================================================================
-- Tạo phòng chat giữa HuynhDucPhu2502@gmail.com với 9 user TestX@gmail.com
-- ===========================================================================================================
INSERT INTO `rooms` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `direct_key`,
`last_message_at`, `name`, `room_img_url`, `room_type`, `theme`, `last_message_id`)
VALUES (1, 1, '2025-11-23 10:52:26.205934', 'huynhducphu2502@gmail.com', '2025-11-23 11:19:44.279932',
'huynhducphu2502@gmail.com', '1_11', '2025-11-23 11:13:09.397749', NULL, NULL, 'DIRECT', 'OCEAN', NULL),
(2, 1, '2025-11-23 10:52:33.953105', 'huynhducphu2502@gmail.com', '2025-11-23 11:19:00.521773',
'huynhducphu2502@gmail.com', '2_11', '2025-11-23 11:19:00.517775', NULL, NULL, 'DIRECT', NULL, NULL),
(3, 1, '2025-11-23 10:52:41.242709', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:41.242709',
'huynhducphu2502@gmail.com', '3_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(4, 1, '2025-11-23 10:52:45.011259', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:45.011259',
'huynhducphu2502@gmail.com', '4_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(5, 1, '2025-11-23 10:52:51.836255', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:51.836255',
'huynhducphu2502@gmail.com', '5_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(6, 1, '2025-11-23 10:52:56.270817', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:56.270817',
'huynhducphu2502@gmail.com', '6_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(7, 1, '2025-11-23 10:53:01.048623', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:01.048623',
'huynhducphu2502@gmail.com', '7_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(8, 1, '2025-11-23 10:53:05.071792', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:05.071792',
'huynhducphu2502@gmail.com', '8_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(9, 1, '2025-11-23 10:53:12.400313', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:12.400313',
'huynhducphu2502@gmail.com', '9_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(10, 1, '2025-11-23 10:53:16.699416', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:16.699416',
'huynhducphu2502@gmail.com', '10_11', NULL, NULL, NULL, 'DIRECT', NULL, NULL),
(11, 1, '2025-11-23 11:20:51.173246', 'huynhducphu2502@gmail.com', '2025-11-23 11:21:31.391949',
'huynhducphu2502@gmail.com', NULL, '2025-11-23 11:21:31.390418', '3 thằng bạn',
'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/group-images/0e6b9276-1c82-44c2-91e9-28de91e74406.avif',
'GROUP', 'ROSE', NULL);

INSERT INTO `room_participants` (`active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `last_read_at`,
`last_read_message_id`, `role`, `room_id`, `user_id`)
VALUES (1, '2025-11-23 10:52:26.272324', 'huynhducphu2502@gmail.com', '2025-11-23 11:12:56.463561', 'Test1@gmail.com',
'2025-11-23 11:12:56.461609', 23, 'MEMBER', 1, 1),
(1, '2025-11-23 10:52:26.257516', 'huynhducphu2502@gmail.com', '2025-11-23 11:13:09.401746',
'huynhducphu2502@gmail.com', '2025-11-23 11:13:09.397749', 24, 'MEMBER', 1, 11),
(1, '2025-11-23 10:52:33.962722', 'huynhducphu2502@gmail.com', '2025-11-23 11:16:51.016870', 'Test2@gmail.com',
'2025-11-23 11:16:51.013871', 28, 'MEMBER', 2, 2),
(1, '2025-11-23 10:52:33.956090', 'huynhducphu2502@gmail.com', '2025-11-23 11:19:00.521773',
'huynhducphu2502@gmail.com', '2025-11-23 11:19:00.517775', 31, 'MEMBER', 2, 11),
(1, '2025-11-23 10:52:41.249423', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:41.249423',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 3, 3),
(1, '2025-11-23 10:52:41.245680', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:41.245680',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 3, 11),
(1, '2025-11-23 10:52:45.017269', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:45.017269',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 4, 4),
(1, '2025-11-23 10:52:45.014266', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:45.014266',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 4, 11),
(1, '2025-11-23 10:52:51.843246', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:51.843246',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 5, 5),
(1, '2025-11-23 10:52:51.839246', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:51.839246',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 5, 11),
(1, '2025-11-23 10:52:56.279047', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:56.279047',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 6, 6),
(1, '2025-11-23 10:52:56.274958', 'huynhducphu2502@gmail.com', '2025-11-23 10:52:56.274958',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 6, 11),
(1, '2025-11-23 10:53:01.054546', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:01.054546',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 7, 7),
(1, '2025-11-23 10:53:01.052660', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:01.052660',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 7, 11),
(1, '2025-11-23 10:53:05.081600', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:05.081600',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 8, 8),
(1, '2025-11-23 10:53:05.077600', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:05.077600',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 8, 11),
(1, '2025-11-23 10:53:12.405306', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:12.405306',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 9, 9),
(1, '2025-11-23 10:53:12.403308', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:12.403308',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 9, 11),
(1, '2025-11-23 10:53:16.705068', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:16.705068',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 10, 10),
(1, '2025-11-23 10:53:16.702064', 'huynhducphu2502@gmail.com', '2025-11-23 10:53:16.702064',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 10, 11),
(1, '2025-11-23 11:20:51.197864', 'huynhducphu2502@gmail.com', '2025-11-23 11:22:17.086133',
'huynhducphu2502@gmail.com', NULL, NULL, 'ADMIN', 11, 1),
(1, '2025-11-23 11:20:51.203919', 'huynhducphu2502@gmail.com', '2025-11-23 11:20:51.203919',
'huynhducphu2502@gmail.com', NULL, NULL, 'MEMBER', 11, 9),
(1, '2025-11-23 11:20:51.185896', 'huynhducphu2502@gmail.com', '2025-11-23 11:21:31.391949',
'huynhducphu2502@gmail.com', '2025-11-23 11:21:31.390418', 34, 'OWNER', 11, 11);

-- ===========================================================================================================
-- Tạo Blog
-- ===========================================================================================================

INSERT INTO `blogs` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `category`, `content`,
`description`, `img_preview_url`, `is_approved`, `title`, `user_id`)
VALUES (1, 1, '2025-11-23 18:50:27.291443', 'huynhducphu2502@gmail.com', '2025-11-23 18:50:46.982676',
'huynhducphu2502@gmail.com', 'TECHNOLOGY',
'<p>Kotlin Multiplatform (KMP) đang ngày càng khẳng định vị thế như một trong những hướng tiếp cận hiệu quả nhất cho phát triển ứng dụng đa nền tảng, đặc biệt khi doanh nghiệp muốn <em>tối ưu chi phí nhưng vẫn giữ trọn trải nghiệm native</em>. Không giống các framework “write once run everywhere”, KMP tập trung vào <strong>chia sẻ business logic</strong>—những phần như networking, xử lý dữ liệu, validation, caching—trong khi UI vẫn hoàn toàn native để đảm bảo hiệu năng và cảm giác sử dụng mượt mà.</p><p><br></p><p>Một trong những ưu điểm lớn nhất của KMP là khả năng nâng cấp từng phần thay vì bắt buộc rewrite toàn bộ ứng dụng. Điều này phù hợp với mô hình doanh nghiệp vừa/nhỏ hay các startup đang muốn <strong>chuyển đổi công nghệ mà không gián đoạn hoạt động hiện tại</strong>. Với cơ chế expect/actual, các phần native API vẫn có thể được gọi trực tiếp từ shared module, ví dụ:</p><p><br></p><p>// commonMain</p><p>expect fun getPlatformName(): String</p><p><br></p><p>// androidMain</p><p>actual fun getPlatformName(): String = "Android"</p><p><br></p><p>// iosMain</p><p>actual fun getPlatformName(): String = "iOS"</p><p><br></p><p>Ngoài khả năng chia sẻ code, hệ sinh thái của KMP ngày càng mạnh mẽ nhờ <strong>Kotlinx Serialization</strong>, <strong>Ktor</strong>, <strong>SQLDelight</strong>, và <strong>Compose Multiplatform</strong>, giúp nhà phát triển xây dựng sản phẩm nhanh và dễ maintain hơn.</p><p><br></p><p>Khi so sánh với Flutter hay React Native, nhiều team đánh giá KMP mang lại lợi thế lớn hơn trong những trường hợp yêu cầu cao về performance hoặc sâu vào tích hợp hệ thống. Vì KMP vẫn dùng <strong>UI native</strong>, nhà phát triển có thể sử dụng toàn bộ tài nguyên, thư viện, widget và pattern quen thuộc trên từng nền tảng. Điều này cũng mở ra khả năng <strong>tối ưu hiệu suất từng nền tảng mà không tạo ra sự lệch pha trong hành vi ứng dụng</strong>.</p><p>Các công ty lớn như Netflix, Philips, VMWare đã áp dụng KMP cho nhiều phần trong ứng dụng sản xuất. Theo JetBrains, bản cập nhật Compose Multiplatform và Gradle 8 sẽ giúp KMP trở thành nền tảng đa năng, vừa linh hoạt vừa mạnh mẽ và phù hợp cho sản phẩm lớn trong nhiều năm tới.</p><p><br></p><p><strong>Tóm lại</strong>, nếu doanh nghiệp muốn tiết kiệm chi phí, rút ngắn thời gian phát triển và giữ nguyên trải nghiệm native, thì Kotlin Multiplatform là một trong những lựa chọn đáng cân nhắc nhất hiện nay. 🚀</p><p><br></p>',
'Giảm chi phí, tăng tốc phát triển và giữ nguyên native experience.',
'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/blog-images/31d55288-27a0-48a0-9af4-b6598b600644.png', b'1',
'Kotlin Multiplatform: Tương lai của phát triển ứng dụng đa nền tảng', 11),
(2, 1, '2025-11-23 18:56:47.757820', 'huynhducphu2502@gmail.com', '2025-11-23 18:56:53.912752',
'huynhducphu2502@gmail.com', 'EDUCATION',
'<p>Trong bối cảnh lượng kiến thức ngày càng khổng lồ và tốc độ thay đổi nhanh chóng, việc học thông minh quan trọng hơn rất nhiều so với học nhiều. Nhiều nghiên cứu về khoa học nhận thức cho thấy rằng não bộ lưu giữ thông tin tốt nhất khi chúng ta chủ động tương tác với kiến thức thay vì tiếp nhận thụ động. Dưới đây là bốn kỹ thuật học tập đã chứng minh hiệu quả cao và được áp dụng rộng rãi trong các lĩnh vực từ giáo dục đến nghiên cứu chuyên sâu.</p><p><br></p><h2><strong>1. Active Recall – Tự buộc não truy xuất thông tin</strong></h2><p>Active Recall là kỹ thuật học tập hiệu quả nhất theo các nghiên cứu hiện đại. Thay vì đọc lại tài liệu liên tục, người học che phần đáp án và cố gắng <em>tự nhớ lại</em> nội dung. Quá trình chủ động truy xuất này kích hoạt vùng trí nhớ sâu, giúp thông tin lưu lại lâu hơn. Ví dụ: khi học từ vựng, thay vì nhìn từ và nghĩa liên tục, hãy thử che nghĩa lại và hỏi: “Từ này có nghĩa là gì?”. Nếu bạn phải vận động não để trả lời, bạn đang tạo ra một “điểm neo trí nhớ” vững chắc hơn nhiều.</p><p><br></p><h2><strong>2. Spaced Repetition – Ôn tập theo chu kỳ giãn cách</strong></h2><p>Spaced Repetition giải quyết vấn đề “quên nhanh” bằng cách ôn tập theo khoảng thời gian tăng dần: ngày 1, ngày 3, ngày 7, ngày 14… Các nghiên cứu của Ebbinghaus chỉ ra rằng bộ não quên thông tin theo đường cong rất nhanh nếu không được củng cố đúng lúc. Việc ôn lại ở thời điểm não bắt đầu quên giúp tái xây dựng lại ký ức và kéo dài thời gian ghi nhớ. Công cụ như <strong>Anki</strong>, <strong>Quizlet</strong>, <strong>RemNote</strong> mô phỏng chính xác mô hình này, giúp người học ghi nhớ từ vựng, định nghĩa hoặc công thức lâu dài mà không quá tải.</p><p><br></p><h2><strong>3. Pomodoro – Quản lý sự tập trung</strong></h2><p>Pomodoro là phương pháp chia thời gian học thành các phiên ngắn, phổ biến nhất là 25 phút học – 5 phút nghỉ. Não bộ hoạt động hiệu quả nhất trong những khoảng tập trung ngắn nhưng cường độ cao, và Pomodoro giúp duy trì nhịp độ này mà không bị kiệt sức. Cứ 4 phiên Pomodoro thì nghỉ dài hơn 15–20 phút. Phương pháp này đặc biệt hữu ích cho những ai dễ bị sao nhãng, trì hoãn hoặc gặp khó khăn trong việc duy trì sự tập trung liên tục.</p><p><br></p><h2><strong>4. Feynman Technique – Kiểm tra mức độ “hiểu thật”</strong></h2><p>Richard Feynman, nhà vật lý từng đoạt Nobel, tin rằng bạn chỉ thực sự hiểu một khái niệm khi có thể giải thích nó bằng ngôn ngữ đơn giản. Kỹ thuật này yêu cầu người học viết lại kiến thức như đang dạy cho một đứa trẻ 12 tuổi. Nếu gặp chỗ nào không thể giải thích đơn giản, đó chính là “lỗ hổng kiến thức”. Bạn quay lại tài liệu, bổ sung chỗ thiếu và thử giải thích lại lần nữa. Đây là công cụ tuyệt vời để học các môn khó như toán, vật lý, lập trình hoặc tài chính.</p><p><br></p><h2><strong>Kết luận</strong></h2><p>Việc học hiệu quả không nằm ở thời gian bạn bỏ ra, mà nằm ở chiến lược. Khi kết hợp <strong>Active Recall</strong>, <strong>Spaced Repetition</strong>, <strong>Pomodoro</strong> và <strong>Feynman Technique</strong>, bạn sẽ tạo thành một quy trình học tối ưu: học chủ động – củng cố đúng thời điểm – duy trì nhịp độ tập trung – kiểm tra sự hiểu thật sự. Nếu duy trì đều đặn, hiệu suất học tập có thể tăng gấp đôi chỉ trong vài tuần.</p><p><br></p>',
'Đơn giản nhưng cực hiệu quả nếu áp dụng đúng cách.',
'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/blog-images/1afd53fe-f324-46f4-ae88-89d0f2aa391e.jpg', b'1',
'4 kỹ thuật học tập hiện đại giúp bạn ghi nhớ nhanh và sâu hơn', 11);

-- ===========================================================================================================
-- Music
-- ===========================================================================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE song_play_history;
TRUNCATE TABLE song_artist_role;
TRUNCATE TABLE album_genre;
TRUNCATE TABLE album_song;
TRUNCATE TABLE song_genre;
TRUNCATE TABLE album_artist;
TRUNCATE TABLE songs;
TRUNCATE TABLE albums;
TRUNCATE TABLE artists;
TRUNCATE TABLE genres;

SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO artists (id, name, bio, img_url) VALUES
(1, 'Gracie Abrams', 'Gracie Abrams is an American singer-songwriter known for her emotional indie pop songs.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e2ce7e9d-fd1b-4cc2-a154-9cd283cde66a.jpg'),
(2, 'Rosé', 'Rosé is a member of the girl group BLACKPINK, recognized for her high vocals and versatile music style.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/1ee3c71d-b101-42fb-98d1-cad4ab4f228e.jpg'),
(3, 'The Weeknd', 'The Weeknd is a Canadian singer, songwriter, and producer, famous for dark R&B and global hits.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/d4b4a377-a804-4f47-bb5f-70184f521000.jpg'),
(4, 'Taylor Swift', 'Taylor Swift is an American singer-songwriter, renowned for her pop and country music and storytelling.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f774a90d-6844-453c-a1c1-6eb1b1f4b018.jpg'),
(5, 'Ariana Grande', 'Ariana Grande is an American singer and actress, known for her wide vocal range and pop/R&B hits.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/b55cde1c-6397-4b65-9fac-116839a2d863.jpg'),
(6, 'Justin Bieber', 'Justin Bieber is a Canadian pop singer who rose to fame as a teen and has many international hits.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/5f8f5177-2c69-4600-9ff6-7495d4af8cbc.jpg'),
(7, 'Billie Eilish', 'Billie Eilish is an American singer-songwriter, known for her unique alternative pop style and emotive music.', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/8e30bd9a-9c29-4342-8407-8f899bee80be.jpg');

INSERT INTO genres (id, name) VALUES
(1, 'Pop'),
(2, 'Indie Pop'),
(3, 'Folk'),
(4, 'K-Pop'),
(5, 'R&B'),
(6, 'Synthwave'),
(7, 'Alternative'),
(8, 'Electropop');


INSERT INTO albums (id, title, owner_id, cover_image_url, play_count) VALUES
-- Gracie Abrams
(1, 'The Secret of Us (Deluxe)', 1, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e497e934-68d9-4a6f-b1df-8b1a2459088f.jpg', 0),
(2, 'minor', 1, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/fa1924d8-3c95-45a8-a742-f931830ae09d.jpg', 0),
(3, 'Good Riddance (Deluxe)', 1, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7b334f04-b3c9-404c-9817-677a6eb30530.jpg', 0),

-- Rosé
(4, 'R', 2, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4e9d9f4b-5c52-40be-96a4-1f0a9284df85.jpg', 0),
(5, 'rosie', 2, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7d151b43-6d2e-485b-a805-0b4d914404f2.jpg', 0),

-- The Weeknd
(6, 'After Hours', 3, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a41994a4-0f3b-4510-a600-0a16e1f5971e.jpg', 0),
(7, 'Dawn FM', 3, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/36b12f6c-faba-4809-afed-a9338e14ecad.jpg', 0),

-- Taylor Swift
(8, 'Midnights', 4, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/87e83111-5466-4342-9acf-ecd9380b9c3b.jpg', 0),
(9, 'Folklore', 4, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/66ab9e40-6ea8-479c-b49d-b70318bf7165.jpg', 0),

-- Ariana Grande
(10, 'Thank U, Next', 5, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/c9e86af8-a86f-48b3-9e16-3f7500c7a9e7.jpg', 0),
(11, 'Positions', 5, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4274a44c-cbe2-473a-b38a-b2ca34006dad.jpg', 0),

-- Justin Bieber
(12, 'Justice', 6, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/ff2d1745-0c74-4e95-ab4e-4725678e52c6.jpg', 0),
(13, 'Changes', 6, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/ad429881-97c2-4a3f-bb1a-bdefe70837e5.jpg', 0),

-- Billie Eilish
(14, 'Happier Than Ever', 7, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/67ec69fe-432b-46b6-b9e8-ccbad103061a.jpg', 0);

INSERT INTO songs (id, title, duration, song_url, img_url, play_count, created_at, updated_at) VALUES
-- Album 1
(1, 'Felt Good About You', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/90234c4e-5db2-4d0d-8732-586ec4a869db.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f696b397-57f4-432a-be02-260ca287946c.jpg', 0, NOW(), NOW()),
(2, 'Risk', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/e0a502d0-ec78-43bb-b38b-373d15efeee8.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/bef3126c-02e7-4749-a1ba-d783bbe87442.jpg', 0, NOW(), NOW()),
(3, 'Blowing Smoke', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d07ea9bd-4e09-4cf3-9494-e50f5d504c17.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f696b397-57f4-432a-be02-260ca287946c.jpg', 0, NOW(), NOW()),
(4, 'I Love You Im Sorry', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/c2eead85-7fff-46c9-ad46-42e6c7334518.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a14cb162-4d16-4bd4-a406-1ca49e8d7372.jpg', 0, NOW(), NOW()),
(5, 'Us.', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/e396d527-0539-43d9-8476-e1c854f721d7.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7523cd14-c38a-41c6-b3fb-413a6069ca28.jpg', 0, NOW(), NOW()),
(6, 'Let It Happen', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/5eade70d-deb4-40d9-80a3-1d09b0e2f6bd.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/6a84c978-9881-4ef0-955a-b984a7f15bcd.jpg', 0, NOW(), NOW()),
(7, 'Touch Love', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/55187a2b-6517-4f3c-9c4d-eec63648d33e.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/b7a21c78-259e-413b-81c7-b9f0eff1a462.jpg', 0, NOW(), NOW()),
(8, 'I Knew It Im Know You', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/7595f4e9-48dc-4d69-baec-2c8bb0b87e54.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/352342e3-5832-425f-befc-5e3cddc48bf3.jpg', 0, NOW(), NOW()),
(9, 'Gave You I Gave You I', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a96a416a-b986-4822-a87d-a523f7458953.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/5af778e6-9248-4c7e-b64d-43437a1fdf20.jpg', 0, NOW(), NOW()),
(10, 'Normal Thing', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/dec4ce0e-83c5-4003-b54c-95d3437f78fd.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/13f23ff4-0612-4e66-b514-72ae88c26a95.jpg', 0, NOW(), NOW()),
(11, 'Good Luck Charlie', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/7086ef60-dbf7-417d-ab1b-e5f403ad4bd0.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7cb11e87-dfde-4d03-ba80-e6b5552babb1.jpg', 0, NOW(), NOW()),
(12, 'Free Now', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/960f512a-90e7-47d8-ac90-c70db930a29a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/40c1e061-8cbb-464c-8366-a73ab58cacc0.jpg', 0, NOW(), NOW()),
(13, 'Close To You', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/b04ce5d7-2868-4271-aca9-10ebe9694cc6.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e5cc1e8c-69cb-47f2-b6cc-2e62e26704d9.jpg', 0, NOW(), NOW()),
(14, 'Cool', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/347e68a2-ad41-4d9e-800f-7ad97e33cead.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e9b8f85e-b849-4c25-9e4b-6bd0868fc778.jpg', 0, NOW(), NOW()),
(15, 'That’s So True', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/fa56d49b-ea4b-444b-9d31-ea686434e8ae.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/b335c091-5398-46a5-a817-42a6c4065798.jpg', 0, NOW(), NOW()),
(16, 'I Told You Things', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/df01f54a-e355-401d-acda-174d4ccb3e8f.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/fb697691-956a-4ef1-bc0d-e0460b38bb14.jpg', 0, NOW(), NOW()),
(17, 'Packing It Up', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ec86aef9-8e6b-4e61-a249-812e3c30739c.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/63c2db49-37dd-4b19-8adf-a220e227e00f.jpg', 0, NOW(), NOW()),

-- Album 2
(18, 'Friend', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/256f61ea-3200-479d-a940-5db77cf63dc2.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/1c8acbfd-72d3-4ea9-b4f5-f1b2026141dd.jpg', 0, NOW(), NOW()),
(19, '21', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/bce3bf8a-a7f1-4942-b009-e9c0f142fec2.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/3a1b106a-7749-4bd1-a09e-5ad504dab44f.jpg', 0, NOW(), NOW()),
(20, 'Under / Over', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/c2e3e7a8-5195-43e8-9532-2cbe8591c5d7.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a9823e68-9f5a-4dbb-aadc-9c59f8bb147d.jpg', 0, NOW(), NOW()),
(21, 'tehe', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ecdd24b8-985c-4d83-82ec-29494fe80c81.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/c269bea9-089d-47e2-b2d3-0cfae171665e.jpg', 0, NOW(), NOW()),
(22, 'I Miss You Im Sorry', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/02e19ff5-bbfd-422b-acfa-acdddc13459a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4cbd9872-f152-4617-91a0-4601a8410783.jpg', 0, NOW(), NOW()),
(23, 'Long Sleeves', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/7c8e354f-6d5b-4f66-bb79-d83b36ddb992.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e409ed84-05e0-41d7-95bc-b9d82e780af7.jpg', 0, NOW(), NOW()),
(24, 'minor', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d04c049b-7ba0-4f63-993c-b8668195d8bb.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/161f2bd2-bb7e-4d54-b45d-ace4eb7e9708.jpg', 0, NOW(), NOW()),

-- Album 3
(25, 'Best', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/8bb49812-bba1-4a34-a5d6-4f2e29438bd4.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/51ad283a-f972-404c-9ef8-c753d3bdd894.jpg', 0, NOW(), NOW()),
(26, 'I Know It Won’t Work', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ec9aa2c2-f9c8-4039-9383-3013f1e7c37a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7f53e3aa-a303-4493-b2d8-08b34ce5624e.jpg', 0, NOW(), NOW()),
(27, 'Full Machine', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d38368a2-ddbc-46d6-809b-f33724bd6a45.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/b7d87654-349f-4d4a-9ba8-8246246134f6.jpg', 0, NOW(), NOW()),
(28, 'Where Do We Go Now?', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ae47e56e-6d10-43db-bbf8-9ae87e4a44a0.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/bfcbe48d-1965-48df-93d8-4dc13b536bd9.jpg', 0, NOW(), NOW()),
(29, 'I Should Hate You', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/b99df038-db03-49b6-bd51-26b69148b4dc.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/705d298f-3c8e-4743-94b2-a428c149f8ef.jpg', 0, NOW(), NOW()),
(30, 'Will You Cry?', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/5e61eef9-0867-4cfe-9bc7-6973942a6951.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/47531ddf-a5af-4b2c-b0d5-f4df2b3d3622.jpg', 0, NOW(), NOW()),
(31, 'Amelie', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/40bd8ade-83c9-4577-a4e7-646175b7197e.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4da7d351-d5ed-4349-a5a9-f20fd3c58b6e.jpg', 0, NOW(), NOW()),
(32, 'Difficult', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/193431d2-6c51-44fe-9716-7a7c1c7c9e09.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e0b53098-11c0-4472-b8c6-a6140c8f2ce6.jpg', 0, NOW(), NOW()),
(33, 'This Is What The Drugs Are For', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/cfdbb98f-535e-4ca9-ba7c-65bda61b3a02.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/530680d9-dcf1-4fb5-b5f4-e2ea408e4f01.jpg', 0, NOW(), NOW()),
(34, 'Fault Line', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/eb89d94f-c61a-4d95-9604-26f53e7c2ec5.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/42601b25-d15d-48a1-b8b6-85d45f632b41.jpg', 0, NOW(), NOW()),
(35, 'The Blue', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/002a30a3-26a9-4efe-a830-88c27dfe6795.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/ba482d4e-b9e1-4532-acca-5772dc986c2d.jpg', 0, NOW(), NOW()),
(36, 'Right Now', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/97ccaf4a-e747-4f90-b266-7c40ec72dd02.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/bfb87b55-76a7-414c-9e33-dc1f014405d9.jpg', 0, NOW(), NOW()),
(37, 'Block Me Out', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ea6ab630-b4da-405a-85ef-63bb2cd6a449.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/56733533-5d29-4add-99fe-b6453f758e3a.jpg', 0, NOW(), NOW()),
(38, 'Unsteady', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/0961274b-1e6a-4559-8933-b3a19b204c14.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/0d00d645-8953-4bc9-b7cb-fd7e65f25586.jpg', 0, NOW(), NOW()),
(39, '405', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/05255b29-93d0-4660-8e0d-ea7da4a1762a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/fb0d1078-52e3-453d-84cf-f5471089afcf.jpg', 0, NOW(), NOW()),
(40, 'Two People', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/da2e2fff-73e4-4da6-9492-02bedec63802.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/21c1a1ec-5092-4487-a77d-07beeb2339c1.jpg', 0, NOW(), NOW()),

-- Rosé (Album 4)
(41, 'On The Ground', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d71a11dd-53e6-4d52-94a2-2205a0f70826.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a9d224f7-3ad6-4ba9-aa25-49e2b9ea313e.jpg', 0, NOW(), NOW()),
(42, 'Gone', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/4db97305-3963-4c7d-90d1-c640b117a7a3.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/da859b22-27c2-47c3-b2f0-6c690f566886.jpg', 0, NOW(), NOW()),

-- Rosé (Album 5)
(43, '3am', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/16bed413-9c6c-47f4-bfdf-af4bc4ec5678.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/3afdf07c-11be-461d-ae77-6f60166dae82.jpg', 0, NOW(), NOW()),
(44, 'Toxic till the end', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/8338a862-c388-4959-a0d6-42e9f117795b.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/013d2d20-54f2-4c33-8482-e73214903357.jpg', 0, NOW(), NOW()),

-- The Weeknd (Album 6 — After Hours)
(45, 'Alone Again', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/345381f1-3b92-495b-b953-fbda7d1c904a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/af04dc64-5168-4c0c-88e3-166e187999f6.jpg', 0, NOW(), NOW()),
(46, 'Too Late', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ba6492e4-6346-4640-8532-430f5e18c2b3.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e1afb051-38c3-40ea-8756-0337a2ef1788.jpg', 0, NOW(), NOW()),
(47, 'Hardest To Love', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d38de57f-d51d-4b0d-8c2f-6eb3a3d2c8ac.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/918a2a4f-0ea1-4d1c-92e3-7c3bf15f0d19.jpg', 0, NOW(), NOW()),
(48, 'Scared To Live', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/266b1831-d4b4-471f-a800-3e80fa2ca57c.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7dc1dc35-2610-4a84-9611-b864dc1da146.jpg', 0, NOW(), NOW()),
(49, 'Snowchild', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/33f956bb-d194-429d-90b4-d6f86bec1535.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f1cde084-3152-45cc-82cb-f77aed08ad73.jpg', 0, NOW(), NOW()),
(50, 'Escape From LA', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/918de8e7-fdc2-4a3f-9621-df410b7bc722.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/e842680a-f154-4ba0-8f89-05d213760b37.jpg', 0, NOW(), NOW()),
(51, 'Heartless', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/1d841340-4088-4c60-ac2b-b58be15d56c8.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4f2a35d4-3133-4c78-942d-0a955aaaa29a.jpg', 0, NOW(), NOW()),
(52, 'Faith', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/efb521a7-78fe-41e8-b775-16aee45573e5.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/8e9e7481-31c9-4dd6-86b7-73cc0def0fc2.jpg', 0, NOW(), NOW()),
(53, 'Blinding Lights', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a6095aa1-1b07-44f5-99aa-1a7845a61e8b.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/2137aee1-4c7b-4ac2-9639-fa70874d4b65.jpg', 0, NOW(), NOW()),
(54, 'In Your Eyes', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/0cc79736-b796-4d8c-b0b3-72ae42487236.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/4d2bf8fd-6678-49f4-9282-a86822d5b3c4.jpg', 0, NOW(), NOW()),
(55, 'Save Your Tears', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/cce175ad-9864-4974-9f6f-9f108c157db1.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/c0d30522-1f36-441a-8e5c-c61216358e65.jpg', 0, NOW(), NOW()),
(56, 'Alone Hours', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/417bb863-615b-457b-afe1-79dc97c4c6d4.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/208fbfb6-eb8e-4242-a972-673fe0495cfb.jpg', 0, NOW(), NOW()),

-- Album 7 — Dawn FM
(57, 'Starry Eyes', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/d5735a51-7908-439d-ac3f-33dac3dff99e.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/7d7291f0-4d82-495f-a7f6-952f0308d6ad.jpg', 0, NOW(), NOW()),
(58, 'Gasoline', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/bb8ba5b9-6898-44b4-bc69-5e5de33b00a9.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/302f9d94-ffde-4840-ab3a-5d42600c851f.jpg', 0, NOW(), NOW()),
(59, 'How Do I Make You Love Me?', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/1fc36e4e-8d7d-470a-a438-c73728017d52.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/68cdedd8-73a4-4c57-8a8b-481c70cf3548.jpg', 0, NOW(), NOW()),
(60, 'Take My Breath', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/49f10a17-50e0-4ece-9f1f-48dbbb45f5dd.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a5576e5c-1c94-4e96-8213-460c2840617e.jpg', 0, NOW(), NOW()),
(61, 'Sacrifice', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/4427ef9e-8d6c-4461-9252-b7f1a660b7ac.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/cc6cdc5b-934b-493f-9151-1f15e8c70ce2.jpg', 0, NOW(), NOW()),
(62, 'Out of Time', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/00120f8b-624b-47d9-8348-69ede707741e.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/0657be39-7260-4207-836d-c25b387dd98b.jpg', 0, NOW(), NOW()),
(63, 'Is There Someone Else?', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/7b60de84-5081-4cda-82ae-594db0be88e7.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/201785f3-0bf2-450e-9990-4faea33303a4.jpg', 0, NOW(), NOW()),

-- Taylor Swift – Midnights (Album 8)
(64, 'Lavender Haze', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/160b7cc0-d54b-4d2f-bb03-83b1a5ae0258.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/eff25f04-cd8c-4748-8f7d-5b41f6f90a6c.jpg', 0, NOW(), NOW()),
(65, 'Maroon', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/16197451-ea56-4b72-a599-0937f89255ef.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/ad4bdb2b-912d-4f73-bb7b-ac1fdc2293ff.jpg', 0, NOW(), NOW()),
(66, 'Anti-Hero', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/936070a7-7535-4fa5-a04a-9e12338d0fef.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/5b2e4220-1fa9-413a-8d7f-7ce289914449.jpg', 0, NOW(), NOW()),
(67, 'Snow On The Beach', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a043296f-19cc-419f-a585-e05c948a6fba.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/8386f019-9c64-4017-8392-9064a388a1a5.jpg', 0, NOW(), NOW()),
(68, 'Midnight Rain', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/6a751897-1055-48ff-b3df-3bf3b4ae03dc.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/d2c92c4a-9517-4674-8fb0-005578f924f3.jpg', 0, NOW(), NOW()),

-- Album 9 – Folklore
(69, 'Cardigan', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a76dba54-eb0a-48ac-bc56-e407b5e7ce0c.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f544869d-2643-4747-b978-24b5a95ca208.jpg', 0, NOW(), NOW()),
(70, 'The 1', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/c50098be-3246-4f1c-8947-e7635a936dc3.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/cc117140-8d6e-4ff4-ac49-fac0889326ab.jpg', 0, NOW(), NOW()),
(71, 'Exile', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/92be757e-97cb-404c-9106-0f28b427e92a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/9153a591-1f49-4f9e-b91b-7644ab9589d5.jpg', 0, NOW(), NOW()),
(72, 'The Last Great American Dynasty', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/12a0686b-f406-4d2f-9964-0ae9efbfc57c.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/9ab74432-100c-49af-a657-5f3cdd83ea1e.jpg', 0, NOW(), NOW()),
(73, 'August', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/bb5b4cc3-82ca-4eb5-9c52-1e7131b2561a.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/0a95b82f-3801-4594-b1a5-2f3b49e34e0b.jpg', 0, NOW(), NOW()),

-- Ariana – Thank U Next (10)
(74, 'Thank U, Next', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/9001161c-7bce-4d73-a993-2e6fd264f441.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/52e40011-8053-4e90-8674-0304a5481c20.jpg', 0, NOW(), NOW()),
(75, '7 Rings', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/eccc5c0c-aa77-45f3-b478-369e0a94af84.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/594383b1-f4dd-41a3-9662-ee8801094f20.jpg', 0, NOW(), NOW()),
(76, 'Needy', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/b48778f6-4228-4223-b6d3-1a90243733a1.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/cb4e6661-0afe-4cc3-ac5b-ba21ec9133c2.jpg', 0, NOW(), NOW()),

-- Positions (11)
(77, 'Positions', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/3320a7f9-3405-4931-8fac-b8bbd7403f81.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/5d8ba942-77d0-4adf-bc6a-eba76595945e.jpg', 0, NOW(), NOW()),
(78, 'POV', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/3f1f3924-e8b4-423c-8607-20e31808cb09.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/cf33d4cc-76e4-4ea6-8ed8-e414b549bdaf.jpg', 0, NOW(), NOW()),
(79, '34+35', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/06e58d22-9fc1-4e18-8b3f-a362ff6cf37c.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/bb0fe885-d75f-4fbf-94b9-6744bb3d4567.jpg', 0, NOW(), NOW()),

-- Justin – Justice (12)
(80, 'Peaches', 198, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/e159cbf6-bd71-4fa0-948a-9272448d4e51.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/d2c873f4-78fd-40da-95e6-7c12849252db.jpg', 0, NOW(), NOW()),
(81, 'Ghost', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/5e03b063-4d2f-47b2-a33b-859b8717d9c8.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/f0a01b44-2177-4aab-9109-bec35d091c4c.jpg', 0, NOW(), NOW()),
(82, 'Hold On', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/9fd78abe-3738-4270-8515-a8230baca151.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/39375cce-7d63-4fb2-b27a-129263d7252e.jpg', 0, NOW(), NOW()),

-- Changes (13)
(83, 'Intentions', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/00770038-7e51-4368-96aa-f10b6f105ac1.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/a2d9a83d-bc13-4f80-b836-526192b15abf.jpg', 0, NOW(), NOW()),
(84, 'Yummy', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a896d38c-e126-4d0b-b489-a6032980b991.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/9ab08ee3-00a6-46a0-981c-5df19ca7f41b.jpg', 0, NOW(), NOW()),
(85, 'Come Around Me', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/7e028376-f443-410f-a99c-f42dd0716c68.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/67f89c49-916d-4d1f-8911-cb3590af6bbe.jpg', 0, NOW(), NOW()),

-- Billie – Happier than Ever (14)
(86, 'NDA', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/29cd988d-a45f-4ed6-b33c-c320bae6c2b5.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/d8a1fce4-6780-4af9-9a3d-26e27c407920.jpg', 0, NOW(), NOW()),
(87, 'Therefore I Am', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/8e182424-6a87-42a5-b0f3-4507a17af43f.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/1efeec45-1736-425e-805f-130c14436e73.jpg', 0, NOW(), NOW()),
(88, 'Your Power', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/f318bfa7-8e88-40c0-847d-9ffd226b564f.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/54027565-3fc1-4ae5-8b47-05fa846662f3.jpg', 0, NOW(), NOW()),
(89, 'Happier Than Ever', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/a26cf14e-5137-46ce-b4f1-4e4a93a577a8.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/6ff1634c-4d6c-4062-bc9a-883857a98a20.jpg', 0, NOW(), NOW()),
(90, 'Male Fantasy', 180, 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/song/ba267d0c-76ab-4aac-bb5c-720d412353cf.mp3', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/music/img/d811048a-2c97-43f8-a92b-f27ac21d81e6.jpg', 0, NOW(), NOW());


INSERT INTO album_genre (album_id, genre_id) VALUES
-- Gracie Abrams (Indie Pop, Alternative)
(1, 2), (1, 7),
(2, 2), (2, 7),
(3, 2), (3, 7),

-- Rosé (K-Pop, Pop)
(4, 4), (4, 1),
(5, 4), (5, 1),

-- The Weeknd (R&B, Synthwave)
(6, 5), (6, 6),
(7, 5), (7, 6),

-- Taylor Swift (Midnights: Pop/Synth, Folklore: Folk/Alt)
(8, 1), (8, 6),
(9, 3), (9, 7),

-- Ariana Grande (Pop, R&B)
(10, 1), (10, 5),
(11, 1), (11, 5),

-- Justin Bieber (Pop, R&B)
(12, 1), (12, 5),
(13, 1), (13, 5),

-- Billie Eilish (Alternative, Electropop)
(14, 7), (14, 8);

INSERT INTO album_song (album_id, song_id) VALUES
-- Album 1 (Songs 1-17)
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
-- Album 2 (Songs 18-24)
(2, 18), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
-- Album 3 (Songs 25-40)
(3, 25), (3, 26), (3, 27), (3, 28), (3, 29), (3, 30), (3, 31), (3, 32), (3, 33), (3, 34), (3, 35), (3, 36), (3, 37), (3, 38), (3, 39), (3, 40),
-- Album 4 (Songs 41-42)
(4, 41), (4, 42),
-- Album 5 (Songs 43-44)
(5, 43), (5, 44),
-- Album 6 (Songs 45-56)
(6, 45), (6, 46), (6, 47), (6, 48), (6, 49), (6, 50), (6, 51), (6, 52), (6, 53), (6, 54), (6, 55), (6, 56),
-- Album 7 (Songs 57-63)
(7, 57), (7, 58), (7, 59), (7, 60), (7, 61), (7, 62), (7, 63),
-- Album 8 (Songs 64-68)
(8, 64), (8, 65), (8, 66), (8, 67), (8, 68),
-- Album 9 (Songs 69-73)
(9, 69), (9, 70), (9, 71), (9, 72), (9, 73),
-- Album 10 (Songs 74-76)
(10, 74), (10, 75), (10, 76),
-- Album 11 (Songs 77-79)
(11, 77), (11, 78), (11, 79),
-- Album 12 (Songs 80-82)
(12, 80), (12, 81), (12, 82),
-- Album 13 (Songs 83-85)
(13, 83), (13, 84), (13, 85),
-- Album 14 (Songs 86-90)
(14, 86), (14, 87), (14, 88), (14, 89), (14, 90);

INSERT INTO song_genre (song_id, genre_id)
SELECT s.id, ag.genre_id
FROM songs s
JOIN album_song as_link ON s.id = as_link.song_id
JOIN album_genre ag ON as_link.album_id = ag.album_id;

INSERT INTO song_artist_role (song_id, artist_id, role)
SELECT
abs.song_id,      -- Lấy song_id từ bảng trung gian album_song
a.owner_id,       -- Lấy owner_id (artist) từ bảng albums
'MAIN_ARTIST'
FROM album_song abs   -- Bắt đầu từ bảng trung gian
JOIN albums a ON abs.album_id = a.id; -- Join sang albums để biết ai là chủ sở hữu album đó

-- Ví dụ: The Weeknd feat trong bài "Positions" (id 77) của Ariana
INSERT INTO song_artist_role (song_id, artist_id, role) VALUES (77, 3, 'FEATURED_ARTIST');

-- Ví dụ: Ariana feat trong bài "Save Your Tears" (id 55) của The Weeknd
INSERT INTO song_artist_role (song_id, artist_id, role) VALUES (55, 5, 'FEATURED_ARTIST');

-- Dựa trên ví dụ featuring ở trên:
-- The Weeknd (3) xuất hiện trong Album Positions (11)
INSERT INTO album_artist (album_id, artist_id) VALUES (11, 3);

-- Ariana (5) xuất hiện trong Album After Hours (6)
INSERT INTO album_artist (album_id, artist_id) VALUES (6, 5);

INSERT INTO song_play_history (user_id, song_id, played_at) VALUES
(1, 1, NOW()),
(1, 2, NOW() - INTERVAL 10 MINUTE),
(2, 3, NOW() - INTERVAL 30 MINUTE);

-- ===========================================================================================================
-- Reels
-- ===========================================================================================================
INSERT INTO `reels` (`id`, `active`, `created_at`, `created_by`, `updated_at`, `updated_by`, `caption`, `status`, `video_url`, `view_count`, `user_id`) VALUES
(1, 1, '2025-12-06 23:34:13.293124', 'huynhducphu2502@gmail.com', '2025-12-07 00:04:42.617579', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/e4a88c09-fe0d-4653-bc5a-61b2031ab07a.mp4', 6, 11),
(2, 1, '2025-12-06 23:34:37.200812', 'huynhducphu2502@gmail.com', '2025-12-07 00:04:44.615011', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/9d85138e-966c-48d7-98b8-dfe0965aca02.mp4', 10, 11),
(3, 1, '2025-12-06 23:35:01.547417', 'huynhducphu2502@gmail.com', '2025-12-08 20:16:58.755653', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/01bac4b7-3644-4381-a0c4-5aef7c9a2db8.mp4', 15, 11),
(4, 1, '2025-12-06 23:37:30.991576', 'atvn15@gmail.com', '2025-12-08 20:17:13.473824', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/96d51137-e208-4827-bc6d-14072bed034e.mp4', 15, 13),
(5, 1, '2025-12-06 23:37:41.151728', 'atvn15@gmail.com', '2025-12-08 22:17:30.303920', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/f9cb7339-d1fb-4de6-a9b0-0c417cf65925.mp4', 14, 13),
(6, 1, '2025-12-06 23:37:50.732743', 'atvn15@gmail.com', '2025-12-08 22:17:29.749465', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/bf39c347-4cbb-4d13-ad53-07efccea05a7.mp4', 14, 13),
(7, 1, '2025-12-06 23:38:00.463086', 'atvn15@gmail.com', '2025-12-08 22:17:27.017594', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/e3938b05-a55b-4799-9f73-5907e9f0b227.mp4', 18, 13),
(8, 1, '2025-12-06 23:38:29.756098', 'atvn15@gmail.com', '2025-12-08 22:17:12.971918', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/f460c9f2-0d94-49ff-a689-d202458e7d39.mp4', 35, 13),
(9, 1, '2025-12-08 20:18:20.860279', 'huynhducphu2502@gmail.com', '2025-12-08 22:17:11.781988', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/ae6c02aa-df97-4d91-8228-ddb1fccff53c.mp4', 3, 11),
(10, 1, '2025-12-08 20:27:07.918777', 'Test1@gmail.com', '2025-12-08 22:30:45.362393', 'huynhducphu2502@gmail.com', 'Vid hay', 'ACTIVE', 'https://pingme-s3.s3.ap-southeast-1.amazonaws.com/reels/99e05f77-1201-4baa-a916-319457db8fb5.mp4', 3, 1);

INSERT INTO `reel_hashtags` (`reel_id`, `tag`) VALUES
(1, 'viral'), (1, 'trending'), (1, 'fyp'),
(2, 'viral'), (2, 'trending'), (2, 'fyp'),
(3, 'viral'), (3, 'trending'), (3, 'fyp'),
(4, 'viral'), (4, 'trending'), (4, 'fyp'),
(5, 'viral'), (5, 'trending'), (5, 'fyp'),
(6, 'viral'), (6, 'trending'), (6, 'fyp'),
(7, 'viral'), (7, 'trending'), (7, 'fyp'),
(8, 'viral'), (8, 'trending'), (8, 'fyp'),
(10, 'travel'),
(10, 'fashion'),
(10, 'photography');

