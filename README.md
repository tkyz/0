# [0.git](.)

## 💬 Hello World!

独り言コード、実験環境

⚠️不定期で[``squash``](./bin/git-autofixup)と[``force-push``](https://git-scm.com/docs/git-push#Documentation/git-push.txt---force)します⚠️

## 💻 [setup](./setup)

```bash
( curl "https://setup.$(hostname -d)" || curl 'https://raw.githubusercontent.com/tkyz/0/main/setup' ) | bash
```

## 👤 auther

### gpg

- [``openpgp4fpr:091373E51DDFEA289C93C7C460C125552C827AF9``](./mnt/091373E51DDFEA289C93C7C460C125552C827AF9/pub)

### pki

- [jpki](https://github.com/jpki) ([公的個人認証サービス](https://www.jpki.go.jp/)) 🗾
  - 公的個人認証AP
    - ``EF0001`` 署名用証明書
    - ``EF0002`` [署名用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_sign_ca.der) 
    - ``EF000A`` 認証用証明書 ([ssh](./mnt/0000-0000-0000/pub))
    - ``EF000B`` [認証用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_auth_ca.der)

> # [総務省](https://www.soumu.go.jp/)(と[デジタル庁](https://www.digital.go.jp/))へ
>
> [番号法](https://laws.e-gov.go.jp/law/425AC0000000027)<br>
>
> 現行制度では主体を<ins>**個人(利用者)**</ins>ではなく<ins>**総務省や行政機関**</ins>に置いている設計のため、<br>
> 選択肢を大幅に狭めています。<br>
>
> 行政・民間・個人での利用拡大や効率化を行うには、<br>
> 現在制限されている<ins>**個人番号の一般公開化**</ins>と、<br>
> <ins>**個人番号のみを含んだ証明書**</ins>の導入が必要です。
>
> - 個人情報を含むため廃止
>   - ``EF0001`` 署名用証明書 ⚠️発行元自治体情報、生年月日の年月(有効期限)、氏名、住所、性別、生年月日
>   - ``EF000A`` 認証用証明書 ⚠️発行元自治体情報、生年月日の年月(有効期限)
> - 個人番号のみを含む証明書を追加
>   - ``EF0003`` 署名用証明書
>   - ``EF000C`` 認証用証明書
>
> マイナンバーカード・個人番号の利点が広く共有・活用され、沢山の社会的課題が改善されることを望んでいます。

### 🙏 donation

|<img src='./mnt/00000000-0000-0000-0000-000000000000/btc.png'       height=20> btc|<img src='./mnt/00000000-0000-0000-0000-000000000000/eth.png'       height=20> eth|<img src='./mnt/00000000-0000-0000-0000-000000000000/sol.png'         height=20> sol|<img src='./mnt/00000000-0000-0000-0000-000000000000/xrp.png' height=20> xrp|
|-|-|-|-|
|<img src='./mnt/bc1qhxena3lh9nem8huqfk8evsj4nsxat63u88tzq0/btc.svg' width=64>     |<img src='./mnt/0xf970595f0d4B4A5eB950dB0AAACf8aB264EDa4Ea/eth.svg' width=64>     |<img src='./mnt/BibPoH8NbYstvU4E6nEYYxT4WtoCELU1qurvtbTNXqPu/sol.svg' width=64>     |<img src='./mnt/rNuQHmQesVCmPT3x1ndKimGgMKuURXyhhL/xrp.svg'   width=64>     |
