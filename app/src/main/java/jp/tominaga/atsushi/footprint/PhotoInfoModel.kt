package jp.tominaga.atsushi.footprint

import io.realm.RealmObject

open class PhotoInfoModel :RealmObject() {

    //撮影した写真音画像ファイルのContentURI(アプリ間でのファイル共有するための保存場所の表示形式)
    var stringContentUrl: String = ""

    //撮影日時
    var dateTime: String = ""

    //緯度
    var latitude : Double = 0.0

    //経度
    var longitude : Double = 0.0

    //地点(緯度の文字列＋経度の文字列) =>地図にマーキングする場所を特定させるため
    var locaton: String = ""
    //コメント
    var comment : String = ""
}