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

- [``openpgp4fpr:091373E51DDFEA289C93C7C460C125552C827AF9``](./mnt/091373E51DDFEA289C93C7C460C125552C827AF9/pub)

### pki

- [jpki](https://github.com/jpki) ([公的個人認証サービス](https://www.jpki.go.jp/))
  - 公的個人認証AP
    - ``EF0001`` 署名用証明書
    - ``EF0002`` [署名用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_sign_ca.der)
    - ``EF000A`` 認証用証明書 ([ssh](./mnt/0000-0000-0000/pub))
    - ``EF000B`` [認証用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_auth_ca.der)

> [!CAUTION]
> 「署名用証明書」には以下の個人情報が含まれています。<br>
> ``発行元自治体情報``、``生年月日の年月``（有効期限）、``氏名``、``住所``、``性別``、``生年月日``
>
> 「認証用証明書」には以下の個人情報が含まれています。<br>
> ``発行元自治体情報``、``生年月日の年月``（有効期限）

## 🙏 donation

|<img src='./mnt/00000000-0000-0000-0000-000000000000/btc.png'       height=20> btc|<img src='./mnt/00000000-0000-0000-0000-000000000000/eth.png'       height=20> eth|<img src='./mnt/00000000-0000-0000-0000-000000000000/sol.png'         height=20> sol|<img src='./mnt/00000000-0000-0000-0000-000000000000/xrp.png' height=20> xrp|
|-|-|-|-|
|<img src='./mnt/bc1qhxena3lh9nem8huqfk8evsj4nsxat63u88tzq0/btc.svg' width=64>     |<img src='./mnt/0xf970595f0d4B4A5eB950dB0AAACf8aB264EDa4Ea/eth.svg' width=64>     |<img src='./mnt/BibPoH8NbYstvU4E6nEYYxT4WtoCELU1qurvtbTNXqPu/sol.svg' width=64>     |<img src='./mnt/rNuQHmQesVCmPT3x1ndKimGgMKuURXyhhL/xrp.svg'   width=64>     |
