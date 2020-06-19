package jp.techacademy.masayuki.natsume.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import java.util.*

class Enemy(type: Int, texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val ENEMY_WIDTH = 1.0f
        val ENEMY_HEIGHT = 1.0f

        // 状態（動く)
        val ENEMY_TYPE_MOVING = 1

        // 速度
        val ENEMY_VELOCITY = 2.0f //横方向の速度定数
    }

    var mState: Int = 0  //状態を保持の定数定義
    var mType: Int

    init {
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT) //ｻｲｽﾞ指定
        mType = type
        mType = ENEMY_TYPE_MOVING         //状態を定数に指定
        velocity.x = ENEMY_VELOCITY
    }

    //表示する位置の決定と状態が変わるかの確認
    fun update (deltaTime: Float) { //描画を行うﾒｿｯﾄﾞ(render)から呼ばれる
        mType = ENEMY_TYPE_MOVING
        x += velocity.x * deltaTime

        // 画面の端まで来たら反対側に移動させる
        if (x < ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = ENEMY_WIDTH / 2
        }
        if (x > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2
        }


    }
}
