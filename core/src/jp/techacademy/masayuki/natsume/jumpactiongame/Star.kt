package jp.techacademy.masayuki.natsume.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

//ｽﾀｰ
class Star(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : Sprite(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val STAR_WIDTH = 0.8f
        val STAR_HEIGHT = 0.8f

        // 状態
        val STAR_EXIST = 0    //初期設定
        val STAR_NONE = 1     //ﾌﾟﾚｲﾔｰが触れた時の状態
    }

    var mState: Int = 0       //初期設定

    init {
        setSize(STAR_WIDTH, STAR_HEIGHT) //ｽﾀｰのｻｲｽﾞ
        mState = STAR_EXIST               //状態を入力
    }

    fun get() {                //ﾌﾟﾚｲﾔｰが触れた時呼ばれるﾒｿｯﾄﾞ
        mState = STAR_NONE    //触れた時の状態を入力
        setAlpha(0f)            //ﾒｿｯﾄﾞを透明にする
    }
}
