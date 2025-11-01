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

- [jpki](https://github.com/jpki) ([公的個人認証サービス](https://www.jpki.go.jp/))
  - 公的個人認証AP
    - ``EF0001`` 署名用証明書
    - ``EF0002`` [署名用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_sign_ca.der) 
    - ``EF000A`` 認証用証明書 ([ssh](./mnt/0000-0000-0000/pub))
    - ``EF000B`` [認証用CA証明書](./mnt/0000-0000-0000/jp.go.jpki_auth_ca.der)

> ---
>
> # 🗾[総務省](https://www.soumu.go.jp/)（及び[デジタル庁](https://www.digital.go.jp/)）へ
>
> 現行の[番号法](https://laws.e-gov.go.jp/law/425AC0000000027)に基づく制度設計では、<ins>**行政機関を中心とした本人確認**</ins>が主たる運用となっており、<br>
> 民間や個人による主体的な活用には一定の制約がある状況です。<br>
>
> 今後は、行政・民間・個人がそれぞれの意思と責任のもとで、<br>
> 安全かつ確実に識別・署名・認証を行える社会基盤へと発展させることが重要です。<br>
>
> この目的を実現するため、以下の点について制度的な見直し及び検討を行うことが求められます。
> - 行政が管理する個人番号から不可逆変換によって生成される個人識別子を追加する。<br>この個人識別子は公開情報として取り扱う。
> - 個人情報を含む証明書を廃止する。
>   - ``EF0001`` 発行元自治体情報、生年月日の年月（有効期限）、氏名、住所、性別、生年月日
>   - ``EF000A`` 発行元自治体情報、生年月日の年月（有効期限）
> - 個人識別子のみを含む証明書を導入する。
>   - ``EF0003`` 署名用証明書
>   - ``EF000C`` 認証用証明書
> 
> これらの見直しにより、マイナンバーカードの利便性がより広く社会で共有・活用され、<br>
> 行政の効率化、デジタル経済の発展、個人の利便性向上に加え、<br>
> 国民のプライバシー保護と信頼性の確保を両立できる制度となることが期待されます。<br>
>
> ---

### 🙏 donation

|<img src='./mnt/00000000-0000-0000-0000-000000000000/btc.png'       height=20> btc|<img src='./mnt/00000000-0000-0000-0000-000000000000/eth.png'       height=20> eth|<img src='./mnt/00000000-0000-0000-0000-000000000000/sol.png'         height=20> sol|<img src='./mnt/00000000-0000-0000-0000-000000000000/xrp.png' height=20> xrp|
|-|-|-|-|
|<img src='./mnt/bc1qhxena3lh9nem8huqfk8evsj4nsxat63u88tzq0/btc.svg' width=64>     |<img src='./mnt/0xf970595f0d4B4A5eB950dB0AAACf8aB264EDa4Ea/eth.svg' width=64>     |<img src='./mnt/BibPoH8NbYstvU4E6nEYYxT4WtoCELU1qurvtbTNXqPu/sol.svg' width=64>     |<img src='./mnt/rNuQHmQesVCmPT3x1ndKimGgMKuURXyhhL/xrp.svg'   width=64>     |
