package jp.techacademy.masayuki.natsume.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.R
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.graphics.OrthographicCamera
import java.util.*
import com.badlogic.gdx.math.Rectangle  // ←追加する
import com.badlogic.gdx.math.Vector3    // ←追加する

//実際のｹﾞｰﾑ画面
class GameScreen(private val mGame: JumpActionGame) : ScreenAdapter() { //JAGｸﾗｽのｵﾌﾞｼﾞｪｸﾄを引数のﾌﾟﾛﾊﾟﾃｲ
    companion object {                //ｶﾒﾗｻｲｽﾞ
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f
        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 3   // 20画面分登れば終了→test3
        val GUI_WIDTH = 320f    // ←ｶﾒﾗｻｲｽﾞ
        val GUI_HEIGHT = 480f   // ←ｶﾒﾗｻｲｽﾞ

        val GAME_STATE_READY = 0      //ｹﾞｰﾑ開始前
        val GAME_STATE_PLAYING = 1    //ｹﾞｰﾑ中
        val GAME_STATE_GAMEOVER = 2   //ｺﾞｰﾙか落下でｹﾞｰﾑ終了

        // 重力
        val GRAVITY = -12               //重力の値 大で強くなる
    }

    private val mBg: Sprite                  //高速処理のﾌﾟﾛﾊﾟﾃｲ
    private val mCamera: OrthographicCamera //ｶﾒﾗｸﾗｽのﾌﾟﾛﾊﾟﾃｲ
    private val mGuiCamera: OrthographicCamera  // ←追加する
    private val mViewPort: FitViewport      //ﾋﾞｭｰﾎﾟｰﾄのﾌﾟﾛﾊﾟﾃｲ
    private val mGuiViewPort: FitViewport   // ←

    //ﾌﾟﾛﾊﾟﾃｲの定義
    private var mRandom: Random           //生成される乱数を取得する為のｸﾗｽ
    private var mSteps: ArrayList<Step>   //生成して配置した踏み台を保持するﾘｽﾄ
    private var mStars: ArrayList<Star>   //生成して配置した☆を保持するﾘｽﾄ
    private lateinit var mUfo: Ufo       //生成して配置したUFOを保持する
    private lateinit var mPlayer: Player //生成して配置したﾌﾟﾚｲﾔｰを保持する
    private lateinit var mEnemy: Enemy   //生成して配置したｴﾒﾆｰを保持する
    private lateinit var mSound: Sound

    private var mGameState: Int           //ｹﾞｰﾑの状態を保持する
    private var mHeightSoFar: Float = 0f    // ←ﾌﾟﾚｲﾔｰと地面の距離
    private var mTouchPoint: Vector3    // ←
    private var mFont: BitmapFont   // ←
    private var mScore: Int // ←
    private var mHighScore: Int // ←
    private var mPrefs: Preferences // ←



    init {
        // 背景の準備
        val bgTexture = Texture("back.png")
        // TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f, 0f)

        // カメラ、ViewPortを生成、設定する 同じｻｲｽﾞにする
        mCamera = OrthographicCamera()   //ﾌﾟﾛﾊﾟﾃｲ代入
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT) //↓,↓
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()   // ←
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT) //↓,↓
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // プロパティの初期化
        mRandom = Random()
        mSteps = ArrayList<Step>()
        mStars = ArrayList<Star>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()         // ←

        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)   // ←
        mFont.data.setScale(0.8f)   // ←
        mScore = 0  // ←
        mHighScore = 0  // ←

        mSound = Gdx.audio.newSound(Gdx.files.internal("data/blackout15.mp3")) //ｺﾝｽﾄﾗｸﾀｰでｻｳﾝﾄﾞをﾊﾟｽで読み込む

        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.masayuki.natsume.jumpactiongame") // ←
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0) // ←(1:ｷｰ,2:値)


        createStage()                        //呼び出された、ｵﾌﾞｼﾞｪｸﾄを配置するﾒｿｯﾄﾞ
    }

    override fun render(delta: Float) { //描画を行いupdateﾒｿｯﾄﾞを呼び出す
        // それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.y > mCamera.position.y) { // ←ﾌﾟﾚｰﾔｰの座標がｶﾒﾗの中心を超えたれ
            mCamera.position.y = mPlayer.y // ←ｶﾒﾗの中心をﾌﾟﾚｰﾔｰの座標にする
        }

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update()  //呼び出されたﾒｯｿｯﾄﾞ(ｶﾒﾗの座標値を再計算する)
        //           ﾒｯｿｯﾄﾞ                      OgCｸﾗｽのﾒｿｯﾄﾞ  2つでその座標をｽﾌﾟﾗｲﾄに反映
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin() //ﾃﾞｰﾀ一括処理(batch 一束)開始

        //背景　原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // Step(Listで保持しているので順番に取り出して描画)
        for (i in 0 until mSteps.size) {
            mSteps[i].draw(mGame.batch)
        }

        // Star(Listで保持しているので順番に取り出して描画)
        for (i in 0 until mStars.size) {
            mStars[i].draw(mGame.batch)
        }

        // UFO
        mUfo.draw(mGame.batch)

        //Player
        mPlayer.draw(mGame.batch)

        //Enemy
        mEnemy.draw(mGame.batch)

        mGame.batch.end() //ﾃﾞｰﾀ一括処理(batch 一束)終了

        // スコア表示
        mGuiCamera.update() // ←
        mGame.batch.projectionMatrix = mGuiCamera.combined  // ←
        mGame.batch.begin() // ↓
        //         1:SprteBatch    2:表示の文字列　　　　　　　　　　 3:x     4:y
        mFont.draw(mGame.batch, "HighScore: $mHighScore", 16f, GUI_HEIGHT - 15) //←BitmapFontｸﾗｽの
        mFont.draw(mGame.batch, "Score: $mScore", 16f, GUI_HEIGHT - 35)    //←ｸﾗｽのdrawﾒｿｯﾄﾞ
        mGame.batch.end()   // ↑
    }

    //FitViewｸﾗｽのupdateﾒｿｯﾄﾞを呼び出す。物理的画面ｻｲｽﾞが変更された時呼び出される
    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height) // ←
        mGuiViewPort.update(width, height) // ←
        mViewPort.update(width, height)
    }

    // ステージを作成する
    private fun createStage() {

        // テクスチャの準備
        val stepTexture = Texture("step.png")
        val starTexture = Texture("star.png")
        val playerTexture = Texture("uma.png")
        val ufoTexture = Texture("ufo.png")
        val enemyTexture = Texture("enemy.png")

        // StepとStarをゴールの高さまで配置していく
        var y = 0f

        val maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY)
        while (y < WORLD_HEIGHT - 5) {
            val type = if(mRandom.nextFloat() > 0.8f) Step.STEP_TYPE_MOVING else Step.STEP_TYPE_STATIC
            val x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH) //0.0～1.0までの値を取得

            val step = Step(type, stepTexture, 0, 0, 144, 36)
            step.setPosition(x, y)
            mSteps.add(step)

            if (mRandom.nextFloat() > 0.6f) {
                val star = Star(starTexture, 0, 0, 72, 72)
                star.setPosition(step.x + mRandom.nextFloat(), step.y + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mStars.add(star)
            }

            y += (maxJumpHeight - 0.5f)
            y -= mRandom.nextFloat() * (maxJumpHeight / 3)
        }

        // Playerを配置
        mPlayer = Player(playerTexture, 0, 0, 72, 72)
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.width / 2, Step.STEP_HEIGHT)

        // ゴールのUFOを配置
        mUfo = Ufo(ufoTexture, 0, 0, 120, 74)
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y)

        // Enemyを配置
        mRandom.nextFloat() > 0.8f
        val type = Enemy.ENEMY_TYPE_MOVING
        val x = mRandom.nextFloat() * (WORLD_WIDTH - Enemy.ENEMY_WIDTH) //0.0～1.0までの値を取得
        mEnemy = Enemy(type, enemyTexture, 0, 0, 120,130)
        mEnemy.setPosition(x, y /2)

    }

    // それぞれのオブジェクトの状態をアップデートする
    private fun update(delta: Float) {           //それぞれの状態用のupdatﾒｿｯﾄﾞを呼び出す
        when (mGameState) {
            GAME_STATE_READY ->
                updateReady()
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver()
        }
    }

    //ｹﾞｰﾑ開始前
    private fun updateReady() {
        if (Gdx.input.justTouched()) {         //ﾀｯﾁされた
            mGameState = GAME_STATE_PLAYING //ｹﾞｰﾑ中に変更に変更
        }
    }

    //ｹﾞｰﾑ中
    private fun updatePlaying(delta: Float) {
        var accel = 0f                         //初期化
        if (Gdx.input.isTouched) {             //ﾀｯﾁされた
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))//座標の取得三次元は0
            val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT) //左半分のﾀｯﾁの矩形の定義
            val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)//右半分のﾀｯﾁの矩形の定義
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) { //ﾀｯﾁの領域の判断
                accel = 5.0f                   //左側
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f                  //右側
            }
        }

        // Enemy
        mEnemy.update(delta) //updateは動かす意味

        // Step
        for (i in 0 until mSteps.size) { //mSteps.sizeまでiは0にある
            mSteps[i].update(delta)
        }

        // Player
        if (mPlayer.y <= 0.5f) {
            mPlayer.hitStep()
        }
        mPlayer.update(delta, accel)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // 当たり判定を行う
        checkCollision() //Collision(衝突)

        // ゲームオーバーか判断する
        checkGameOver()
    }

    private fun checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.y) { //mHeightSoFarﾌﾟﾚｲﾔｰと地面の距離-ｶﾒﾗ高さの1/2
            Gdx.app.log("JampActionGame", "GAMEOVER")
            mGameState = GAME_STATE_GAMEOVER //の時ｹﾞｰﾑｵｰﾊﾞｰ
        }
    }


    //Rectangleｸﾗｽoverlapsﾒｿｯﾄﾞに相手のRectangleを指定
    private fun checkCollision() {
        // UFO(ゴールとの当たり判定)
        if (mPlayer.boundingRectangle.overlaps(mUfo.boundingRectangle)) {
            mGameState = GAME_STATE_GAMEOVER
            return
        }

        // Enemyとの当たり判定
        if (mPlayer.boundingRectangle.overlaps(mEnemy.boundingRectangle)) {
            mGameState = GAME_STATE_GAMEOVER

            mSound.play() //playﾒｿｯﾄﾞで

            return
        }

        // Starとの当たり判定
        for (i in 0 until mStars.size) {
            val star = mStars[i]

            if (star.mState == Star.STAR_NONE) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(star.boundingRectangle)) {
                star.get()
                mScore++    // ←1を足す
                if (mScore > mHighScore) {  // ←mScoreが大きければ
                    mHighScore = mScore     // ←代入
                    //ハイスコアをPreferenceに保存する
                    mPrefs.putInteger("HIGHSCORE", mHighScore)  //(1:ｷｰ,2:値)
                    mPrefs.flush()  // ←flushﾒｿｯﾄﾞで永続化
                }

                break
            }
        }

        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) { //上昇中
            return
        }

        for (i in 0 until mSteps.size) {
            val step = mSteps[i]

            if (step.mState == Step.STEP_STATE_VANISH) { //消える以外で
                continue
            }

            if (mPlayer.y > step.y) {
                if (mPlayer.boundingRectangle.overlaps(step.boundingRectangle)) {
                    mPlayer.hitStep()
                    if (mRandom.nextFloat() > 0.3f) { //2分の1で踏み台を消す→3分の1
                        step.vanish()
                    }
                    break
                }
            }
        }
    }

    private fun updateGameOver() {                      //ｹﾞｰﾑ終了後
        if (Gdx.input.justTouched()) {                   //ﾀｯﾁしたら
            mGame.screen = ResultScreen(mGame, mScore) //結果画面に遷移
        }
    }
}