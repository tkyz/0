# [0.git](.)

実験環境、コード、独り言

> [!NOTE]
> 不定期で[``squash``](./bin/git-autofixup)します。

## 💻 [setup](./setup)

```bash
( curl "https://setup.$(hostname -d)" || curl 'https://raw.githubusercontent.com/tkyz/0/main/setup' ) | bash
```

## 👤 auther

### gpg

- [``openpgp4fpr:091373E51DDFEA289C93C7C460C125552C827AF9``](./dataset/openpgp4fpr/091373E51DDFEA289C93C7C460C125552C827AF9.pub)

### pki

- [jpki](https://github.com/jpki) ([公的個人認証サービス](https://www.jpki.go.jp/))
  - 公的個人認証AP
    - ``EF0001`` 署名用証明書
    - ``EF0002`` [署名用CA証明書](./dataset/mynaid/jp.go.jpki_sign_ca.der)
    - ``EF000A`` 認証用証明書 ([ssh](./dataset/mynaid/0000-0000-0000/pub))
    - ``EF000B`` [認証用CA証明書](./dataset/mynaid/jp.go.jpki_auth_ca.der)

> [!CAUTION]
> 「署名用証明書」には以下の個人情報が含まれています。<br>
> ``発行元自治体情報``、``生年月日の年月``（有効期限）、``氏名``、``住所``、``性別``、``生年月日``
>
> 「認証用証明書」には以下の個人情報が含まれています。<br>
> ``発行元自治体情報``、``生年月日の年月``（有効期限）

## 🙏 donation

|<img src='./dataset/btc/icon.png'                                       height=20> btc|<img src='./dataset/eth/icon.png'                                       height=20> eth|<img src='./dataset/sol/icon.png'                                         height=20> sol|<img src='./dataset/xrp/icon.png'                               height=20> xrp|
|-|-|-|-|
|<img src='./dataset/btc/bc1qhxena3lh9nem8huqfk8evsj4nsxat63u88tzq0.svg' width=64>     |<img src='./dataset/etc/0xf970595f0d4B4A5eB950dB0AAACf8aB264EDa4Ea.svg' width=64>     |<img src='./dataset/sol/BibPoH8NbYstvU4E6nEYYxT4WtoCELU1qurvtbTNXqPu.svg' width=64>     |<img src='./dataset/xrp/rNuQHmQesVCmPT3x1ndKimGgMKuURXyhhL.svg' width=64>     |
