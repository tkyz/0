# 0.git

個人環境です。

⚠️不定期で[squash](./bin/git-autofixup)します。

## 📄 ソース

main
- [git://git.example/0.git](http://git.example/?p=0.git;a=tree)

mirror
- https://github.com/tkyz/0.git
- ...

## 💻 セットアップ

1. 最新のOSイメージを[ダウンロード](https://www.debian.org/CD/http-ftp/)
2. OSインストール用USBを作成<br>iso -> USB
   ```bash
   cat debian.iso > /dev/sdX
   ```
3. OSをインストール<br>USB -> PC
4. 個人環境をセットアップ・デプロイ
   ```bash
   ( curl "http://setup.$(hostname -d)" || curl 'https://raw.githubusercontent.com/tkyz/0/main/setup' ) | bash
   ```

## 🌐 ドメイン

- ``$(hostname -d)``
  - echo
  - dns
  - ntp
  - [setup](http://setup.example/)
  - _p2p_
    - tor
    - _blockchain_
      - btc
      - eth
  - _database_
    - mariadb
    - pgsql
    - redis
    - mongo
    - chroma
  - _registry_, _repository_
    - apt
    - [git](http://git.example/)
    - container
  - _game_
    - minecraft
  - _web_
    - [doc](http://doc.example/)
    - [bi](http://bi.example/)
    - _ai_
      - [chat](http://chat.example/)
      - [comfyui](http://comfyui.example/)

## 👤 Author

- [openpgp4fpr:091373E51DDFEA289C93C7C460C125552C827AF9](./dat/openpgp4fpr/091373E51DDFEA289C93C7C460C125552C827AF9/pub)
- マイナンバーカード
  - 公的個人認証AP
    - EF000A
      - [ssh](./dat/jpki_id/4abc370b9ad260aeebb12eda1d794d7fa80a87742ef637698ca145ce9129209b/auth.ssh.pub)

|<img src='./dat/blockchain/btc/icon.png'                                       height=20> btc|<img src='./dat/blockchain/eth/icon.png'                                       height=20> eth|<img src='./dat/blockchain/sol/icon.png'                                         height=20> sol|<img src='./dat/blockchain/xrp/icon.png'                               height=20> xrp|
|-|-|-|-|
|<img src='./dat/blockchain/btc/bc1qhxena3lh9nem8huqfk8evsj4nsxat63u88tzq0.svg' width=64>     |<img src='./dat/blockchain/eth/0xf970595f0d4B4A5eB950dB0AAACf8aB264EDa4Ea.svg' width=64>     |<img src='./dat/blockchain/sol/BibPoH8NbYstvU4E6nEYYxT4WtoCELU1qurvtbTNXqPu.svg' width=64>     |<img src='./dat/blockchain/xrp/rNuQHmQesVCmPT3x1ndKimGgMKuURXyhhL.svg' width=64>     |
