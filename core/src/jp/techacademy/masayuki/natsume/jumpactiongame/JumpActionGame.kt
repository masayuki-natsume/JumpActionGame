package jp.techacademy.masayuki.natsume.jumpactiongame

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch

//GameｸﾗｽはScreenAdapterｸﾗｽを持ち1画面のｸﾗｽを設定し画面遷移を行う、libGDXのｱﾌﾟﾘを制御するｸﾗｽ
class JumpActionGame(val mRequestHandler: ActivityRequestHandler) : Game() { //ARHandlerのﾌﾟﾛﾊﾟﾃｨ引数
    lateinit var batch: SpriteBatch //SpriteBatchｸﾗｽは画像をGPUで描画

    override fun create() {
        batch = SpriteBatch()

        // GameScreenを表示する
        setScreen(GameScreen(this))
    }
}

