-- 创建用户 aa，密码为 123456
-- BCrypt加密后的密码哈希值

USE ceshi1;

INSERT INTO t_user (username, password, nickname, role, status, create_time) VALUES (
    'aa',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.eG1H2DkKsQPmPLF9E.',
    'aa',
    1,
    1,
    NOW()
);

-- 验证插入结果
SELECT * FROM t_user WHERE username = 'aa';
