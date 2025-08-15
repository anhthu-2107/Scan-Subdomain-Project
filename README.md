Xây dựng hệ thống Client-Server:
- Client gửi một message với mã hoá BASE64(SHA256withRSA) kèm theo raw-message lên server và public key mã hoá bằng AES mode CBC. Hoặc mã hoá toàn bộ tin nhắn bằng AES-CBC rồi gửi qua server.
- Server giải mã và xác thực tin nhắn đó có phải của client không. Nếu verify thành công thì server thực hiện scan tất cả subdomain của hệ thống (https://huflit.edu.vn) và trả response về cho client. Nếu không verify được thì trả về “VERIFICATION_FAILED” 
- Server lấy public key từ request của client và giải mã, key và IV để giải mã lưu vào Database. Client sử dụng private key từ file để ký số. Wordlist của subdomain lấy từ link sau: https://github.com/danielmiessler/SecLists/blob/master/Discovery/DNS/subdomains-top1million-110000.txt

Database:

CREATE DATABASE IF NOT EXISTS subdomain_scanner;
USE subdomain_scanner;

CREATE TABLE IF NOT EXISTS keys (
    id INT PRIMARY KEY AUTO_INCREMENT,
    aes_key VARCHAR(255) NOT NULL,
    aes_iv VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO keys (id, aes_key, aes_iv) VALUES (1, '1234567890123456', '1234567890123456');


Link demo youtube: https://youtu.be/cf2t3-GH-MU
