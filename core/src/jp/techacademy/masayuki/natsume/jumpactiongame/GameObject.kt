package jp.techacademy.masayuki.natsume.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2

//ｵﾌﾞｼﾞｪｸﾄｸﾗｽの作成と表示の継承元
open class GameObject(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : Sprite(texture, srcX, srcY, srcWidth, srcHeight){

    // x方向、y方向の速度を保持する
    val velocity: Vector2  //ﾌﾟﾛﾊﾟﾃｲにxとy持つｸﾗｽ、初期化

    init{
        velocity = Vector2()
    }
}