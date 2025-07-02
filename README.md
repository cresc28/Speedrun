# 【Minecraft】Speedrun用プラグイン(タイム計測、チェックポイント)

---

## **対応バージョン**

- **Minecraft 1.12台** で動作確認済み  
  ※ 他のバージョンでの動作は未確認です。

---

## **機能**

- 設定したスタート地点から中間地点、ゴール地点間の移動にかかった現実時間及びゲーム内時間を表示
- 設定したチェックポイントへのテレポート (ネザースターを右クリック)

---

## ダウンロード
<p align="center">
  <a href="https://github.com/cresc28/Speedrun/releases/tag/v2.0.0" style="font-size: 1.5em;">
    <strong>⬇️ Speedrunプラグインをダウンロード</strong>
  </a>
</p>

---

## **使い方**

<details>
<summary><strong>コース設定</strong></summary>

### `/course add <start|end|via_point> <コース名>`
スタート地点、中間地点、ゴール地点を設定します。(地点はブロック単位です。)<br>
設定したい地点の上に立ち、コースの名前を指定してください。  <br>
同じコースには同じ名前を設定する必要があります。

同じ名前のスタート地点やゴール地点が複数存在する場合は、
最後に踏んだスタート地点から最初に踏んだゴール地点までの時間が計測・表示されます。

---

### `/course add <start|end|via_point> <コース名>　<中間地点名>`
名前付きで中間地点を登録します。<br>
内部的には`コース名.中間地点名`という形式で保存されます。
中間地点名は省力可能です。<br>

---

### `/course remove <start|end|via_point> <コース名>　または  /course remove <コース名>`
指定した名前のコースのスタート地点、中間地点、ゴール地点を削除します。<br>
後者のコマンドでは、指定したコースの地点を一括で削除します。

---

### `/course list <start|end|via_point> または /course list` 
登録されているコースの一覧を表示します。

---

### `/course tp <start|end|via_point> <コース名> または /course tp <コース名>`
指定したコースへTPします。タイプを指定しない場合はスタート地点にTPされます。<br>
このコマンドは`/cp tp`に比べて低速です。

---

### その他
コースの計測開始メッセージやクリアメッセージを変更するには、  
`Speedrun`ディレクトリに生成される`message.yml`を編集してください。

</details>

<details>
<summary><strong>チェックポイント設定</strong></summary>

### `/cp <CP名> または /cp`
現在位置に指定した名前のチェックポイント(以下CP)を登録します。</br>
名前を指定しなかった場合tmpという名前のCPを登録します。<br>
ネザースターを右クリックすると、その位置にテレポートします。

---

### `/cp remove <CP名>`
指定した名前のCPを削除します。

---

### `/cp tp <CP名>`
指定した名前のCPへTPします。

---

### `/cp list`
現在のワールドに存在するCPの一覧を表示します。

---

### `/cp allowCrossWorldTp <true|false>`
ワールドを跨ぐCPでの移動を許可または禁止します。
</details>