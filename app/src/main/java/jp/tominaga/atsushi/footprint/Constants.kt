package jp.tominaga.atsushi.footprint

enum class ModeInEdit{
    SHOOT, EDIT
}

enum class  IntentKey {
    EDIT_MODE,
    CONTENT_URI
}


//パーミッションの許可を求めるためのリクエストコード
val RQ_CODE_PERMISSION = 200

val RQ_CODE_CAMERA = 100

//撮影した写真を入れる共有フォルダ名
val PHOTO_FOLDER_NAME = "FOOTPRINT"

val ZOOM_LEVEL_DETAIL = 15

val ZOOM_LEVEL_MASTER = 10