#!/bin/bash

# 建立輸出目錄
mkdir -p captures

# 定義檔案名稱
CAPTURE_FILE="captures/tls_capture_openssl_$(date +%Y%m%d_%H%M%S).pcap"

# 預先準備 HEAD 請求內容
cat > request.txt << EOF
HEAD / HTTP/1.1
Host: google.com
Connection: close

EOF

# 開始tcpdump捕獲（不需要sudo，使用-w寫入文件）
echo "開始捕獲網絡流量..."
tcpdump -i lo0 -n "tcp port 4433" -w "$CAPTURE_FILE" &
TCPDUMP_PID=$!

# 等待tcpdump啟動
sleep 1

# 啟動本地端口轉發器
echo "啟動本地端口轉發器..."
socat TCP-LISTEN:4433,fork,reuseaddr TCP:google.com:443 &
SOCAT_PID=$!

# 等待socat啟動
sleep 1

# 使用openssl s_client通過本地端口轉發器發送HEAD請求到google.com，使用 TLS 1.3
echo "使用openssl發送HEAD請求，使用TLS 1.3並限制 extension和僅使用TLS_AES_128_GCM_SHA256..."
cat request.txt | openssl s_client -connect localhost:4433 \
  -tls1_3 \
  -ciphersuites TLS_AES_128_GCM_SHA256 \
  -sigalgs "rsa_pkcs1_sha256:rsa_pss_rsae_sha256:ecdsa_secp256r1_sha256" \
  -curves secp256r1 \
  -ign_eof \
  -quiet

# 等待確保捕獲完成
sleep 2

# 終止進程
echo "終止捕獲和轉發器..."
kill $TCPDUMP_PID
kill $SOCAT_PID

# 等待進程完全終止
sleep 1

# 清理臨時文件
rm -f request.txt

# 開啟Wireshark查看捕獲檔案
echo "用Wireshark開啟捕獲檔案: $CAPTURE_FILE"
open -a Wireshark "$CAPTURE_FILE"

echo "完成!"