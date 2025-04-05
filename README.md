# TruthTLS

我想藉由實作來學習 TLSNotary.

從 FAQ 來看, TLSNotary 目前還沒支援 TLS 1.3 .

另外目前 TLSNotary 的 encryption / decryption 應該是在 MPC 裡面做的. 我想試著改變這一點. 希望能讓計算量降低.
(先前在 TLSNotary discord 的[討論](https://discord.com/channels/943612659163602974/968798323341418547/1351479225454035005))

這是在 [ETHGlobal Taipei 2025](https://ethglobal.com/events/taipei) 的 project.

我會從頭開發一個 minimal 的 TLS 1.3 client. 能多簡陋就多簡陋.

我也沒有時間開發 MPC. 我會把 interface 切出來, 先用 local computation 計算.

能寫多少算多少.

先用中文寫, 最後再叫 AI 翻譯成英文.

----

現在是 5:33.

Client 兩種做法. 寫 code 產生. 拿現成的挖洞.

先選挖洞的.

叫 Github Copilot - Agent - Claude 3.7 Sonnet 來幫忙.

```prompt
幫我寫一個 script.
開一個 local port forwarder 在 port 4433.
用 openssl s_client 透過它去連接 google.com port 443. 送 HEAD request 就好. HTTP/1.1 . 用 TLS 1.3 .
用 tcpdump 把內容 capture 下來. (不用 sudo)
開 wireshark 讓我檢視內容.
```

![wireshark](image.png)


但是顯然內容太多了. 只要最少的內容
cipher_suite: TLS_AES_128_GCM_SHA256
supported_versions: TLS 1.3
signature_algorithms: rsa_pkcs1_sha256 / rsa_pss_rsae_sha256 / ecdsa_secp256r1_sha256
supported_groups: secp256r1
