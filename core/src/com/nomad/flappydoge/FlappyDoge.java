package com.nomad.flappydoge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyDoge extends ApplicationAdapter {


    //region Objetos
    private Preferences preferences;
    private Stage stage;
    private SpriteBatch batch;
    private Texture[] doge, fundo, digits, digitsSmall;
    private Texture canoBaixo, canoTopo, gameOver, hint, ready, bronzeCoin, endScore,
            goldCoin, platinumCoin, play, rate, scores, share, silverCoin;
    private Random numeroAleatorio;
    private BitmapFont fonte, msgGameOver;
    private Rectangle canoTopoCollision, canoBaixoCollision, dogeCollision;
    ShapeRenderer shape;
    private TextureRegion playRegion;
    private TextureRegionDrawable playRegionDrawable;
    private ImageButton playButton;

    //endregion

    //region Atributos
    float alturaDispositivo;
    float larguraDispositivo;
    int estadoJogo = 0;
    int pontuacao = 100;
    int nfundo;
    float variacao;
    float velocidadeQueda = 0;
    float posicaoInicialVertical;
    float posicaoMovimentoCanoHorizontal;
    float espacoEntreCanos;
    float deltaTime;
    float alturaEntreCanosAleatoria;
    boolean marcouPonto, firstTouch;
    int highScore;
    Integer[] pontuacaoDigits;
    //endregion

    //region camera
    OrthographicCamera camera;
    Viewport viewport;
    final float VIRTUAL_WIDTH = 768, VIRTUAL_HEIGHT = 1024;

    //endregion


    @Override
    public void create() {
        preferences = Gdx.app.getPreferences("HighScore");
        highScore = preferences.getInteger("HighScore", 0);


        numeroAleatorio = new Random();
        nfundo = (Math.random() < 0.5) ? 0 : 1;
        fonte = new BitmapFont();
        fonte.setColor(Color.WHITE);
        fonte.getData().setScale(6);
        msgGameOver = new BitmapFont();
        msgGameOver.setColor(Color.WHITE);
        msgGameOver.getData().setScale(3);
        alturaDispositivo = VIRTUAL_HEIGHT;
        larguraDispositivo = VIRTUAL_WIDTH;
        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 250;

        SetupTextures();

        /*Configuração da camera*/
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        stage = new Stage(viewport);
        stage.addActor(playButton);

        playButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (estadoJogo == 2) {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    firstTouch = true;
                    return false;
                } else return true;
            }
        });
    }

    @Override
    public void render() {
        camera.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        deltaTime = Gdx.graphics.getDeltaTime();
        if (variacao > 2) variacao = 0;
        variacao += deltaTime * 5;

        if (estadoJogo == 0) {
            if (firstTouch)
                firstTouch = false;
            else if (Gdx.input.justTouched())
                estadoJogo = 1;
        } else {

            velocidadeQueda++;
            if (posicaoInicialVertical > 0)
                posicaoInicialVertical -= velocidadeQueda;

            if (estadoJogo == 1) {
                posicaoMovimentoCanoHorizontal -= deltaTime * 400;
                if (Gdx.input.justTouched())
                    velocidadeQueda = -15;
                if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    alturaEntreCanosAleatoria = numeroAleatorio.nextInt(350) - 200;
                    marcouPonto = false;
                }
                if (posicaoMovimentoCanoHorizontal < 150) {
                    if (!marcouPonto) {
                        pontuacao++;
                        marcouPonto = true;
                    }
                }
            }
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(fundo[nfundo], 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosAleatoria);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosAleatoria);
        batch.draw(doge[(int) variacao], 150, posicaoInicialVertical);
        Pontuacao_Geral();

        if (estadoJogo == 0) {
            batch.draw(hint, larguraDispositivo / 2 - hint.getWidth() / 2, alturaDispositivo / 2 - hint.getHeight() / 2);
        }
        if (estadoJogo == 2) {
            Gdx.input.setInputProcessor(stage);
            stage.getBatch().begin();
            stage.getBatch().draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, (alturaDispositivo / 2 + 200) - gameOver.getHeight() / 2);
            stage.getBatch().draw(endScore, larguraDispositivo / 2 - endScore.getWidth() / 2, (alturaDispositivo / 2) - endScore.getHeight() / 2);
            if (pontuacao >= 10 && pontuacao < 20) {
                stage.getBatch().draw(bronzeCoin, (larguraDispositivo / 2 - endScore.getWidth() / 2) + bronzeCoin.getWidth() / 2 + 10,
                        ((alturaDispositivo / 2) - endScore.getHeight() / 2) + bronzeCoin.getHeight() / 2 + 10);
            } else if (pontuacao >= 20 && pontuacao < 30) {
                stage.getBatch().draw(silverCoin, (larguraDispositivo / 2 - endScore.getWidth() / 2) + bronzeCoin.getWidth() / 2 + 10,
                        ((alturaDispositivo / 2) - endScore.getHeight() / 2) + bronzeCoin.getHeight() / 2 + 10);
            } else if (pontuacao >= 30 && pontuacao < 50) {
                stage.getBatch().draw(goldCoin, (larguraDispositivo / 2 - endScore.getWidth() / 2) + bronzeCoin.getWidth() / 2 + 10,
                        ((alturaDispositivo / 2) - endScore.getHeight() / 2) + bronzeCoin.getHeight() / 2 + 10);
            } else if (pontuacao >= 50) {
                stage.getBatch().draw(platinumCoin, (larguraDispositivo / 2 - endScore.getWidth() / 2) + bronzeCoin.getWidth() / 2 + 10,
                        ((alturaDispositivo / 2) - endScore.getHeight() / 2) + bronzeCoin.getHeight() / 2 + 10);
            }

            Pontuacao_Final();
            if (highScore < pontuacao) {
                preferences.putInteger("HighScore", pontuacao);
                stage.getBatch().draw(digitsSmall[9], larguraDispositivo / 2 + (endScore.getWidth() / 2) - 50 - digitsSmall[9].getWidth(),
                        alturaDispositivo / 2 - 50 - digitsSmall[9].getHeight() / 2);
            }
            stage.getBatch().end();
            stage.draw();
            playButton.setPosition(larguraDispositivo / 2 - playButton.getWidth() / 2, (alturaDispositivo / 2) - playButton.getHeight() * 2);
            // msgGameOver.draw(batch, "Toque para Reiniciar!", larguraDispositivo / 2 - 200, (alturaDispositivo / 2 + 200) - gameOver.getHeight() / 2 - 100);
        }
        batch.end();

        dogeCollision = new Rectangle(
                150, posicaoInicialVertical, doge[0].getWidth(), doge[0].getHeight()
        );
        canoBaixoCollision = new Rectangle(
                posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosAleatoria,
                canoBaixo.getWidth(), canoBaixo.getHeight());
        canoTopoCollision = new Rectangle(
                posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosAleatoria,
                canoTopo.getWidth(), canoTopo.getHeight()
        );

        //Desenhar formas
     /*   shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(propinaCollision.x, propinaCollision.y, propinaCollision.radius);
        shape.rect(canoBaixoCollision.x, canoBaixoCollision.y, canoBaixoCollision.width, canoBaixoCollision.height);
        shape.rect(canoTopoCollision.x, canoTopoCollision.y, canoTopoCollision.width, canoTopoCollision.height);
        shape.setColor(Color.RED);
        shape.end();*/

        if (Intersector.overlaps(dogeCollision, canoBaixoCollision) ||
                Intersector.overlaps(dogeCollision, canoTopoCollision) || posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo) {
            estadoJogo = 2;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    void SetupTextures() {
        batch = new SpriteBatch();
        doge = new Texture[4];
        doge[0] = new Texture("doge_1.png");
        doge[1] = new Texture("doge_2.png");
        doge[2] = new Texture("doge_3.png");
        doge[3] = new Texture("doge_4.png");
        hint = new Texture("img_inicio.png");
        fundo = new Texture[2];
        fundo[0] = new Texture("fundo_noite.png");
        fundo[1] = new Texture("fundo_dia.png");
        canoBaixo = new Texture("cano_baixo.png");
        canoTopo = new Texture("cano_alto.png");
        gameOver = new Texture("game_over.png");
        ready = new Texture("ready.png");
        bronzeCoin = new Texture("bronze_coin.png");
        endScore = new Texture("end_score.png");
        goldCoin = new Texture("gold_coin.png");
        platinumCoin = new Texture("platinum_coin.png");
        play = new Texture("play.png");
        rate = new Texture("rate.png");
        scores = new Texture("scores.png");
        share = new Texture("share.png");
        silverCoin = new Texture("silver_coin.png");

        playRegion = new TextureRegion(play);
        playRegionDrawable = new TextureRegionDrawable(playRegion);
        playButton = new ImageButton(playRegionDrawable);


        //region BigDigits

        digits = new Texture[10];
        digits[0] = new Texture("digits/b0.png");
        digits[1] = new Texture("digits/b1.png");
        digits[2] = new Texture("digits/b2.png");
        digits[3] = new Texture("digits/b3.png");
        digits[4] = new Texture("digits/b4.png");
        digits[5] = new Texture("digits/b5.png");
        digits[6] = new Texture("digits/b6.png");
        digits[7] = new Texture("digits/b7.png");
        digits[8] = new Texture("digits/b8.png");
        digits[9] = new Texture("digits/b9.png");

        //endregion

        //region SmallDigits
        digitsSmall = new Texture[10];

        digitsSmall[0] = new Texture("digits/s0.png");
        digitsSmall[1] = new Texture("digits/s1.png");
        digitsSmall[2] = new Texture("digits/s2.png");
        digitsSmall[3] = new Texture("digits/s3.png");
        digitsSmall[4] = new Texture("digits/s4.png");
        digitsSmall[5] = new Texture("digits/s5.png");
        digitsSmall[6] = new Texture("digits/s6.png");
        digitsSmall[7] = new Texture("digits/s7.png");
        digitsSmall[8] = new Texture("digits/s8.png");
        digitsSmall[9] = new Texture("digits/s9.png");


        //endregion
    }

    //region SetupDigits

    public static Integer[] getDigits(int num) {
        List<Integer> digits = new ArrayList<Integer>();
        collectDigits(num, digits);
        return digits.toArray(new Integer[]{});
    }

    private static void collectDigits(int num, List<Integer> digits) {
        if (num / 10 > 0) {
            collectDigits(num / 10, digits);
        }
        digits.add(num % 10);
    }

//endregion

    //region Calcular Pontuacao


    void Pontuacao_Geral() {
        pontuacaoDigits = getDigits(pontuacao);

        if (pontuacaoDigits.length == 1) {
            batch.draw(digits[pontuacaoDigits[0]], larguraDispositivo / 2 - digits[pontuacaoDigits[0]].getWidth() / 2,
                    (alturaDispositivo - digits[pontuacaoDigits[0]].getHeight()) - 25);
        } else if (pontuacaoDigits.length == 2) {
            batch.draw(digits[pontuacaoDigits[1]], larguraDispositivo / 2 - digits[pontuacaoDigits[0]].getWidth() / 2,
                    (alturaDispositivo - digits[pontuacaoDigits[0]].getHeight()) - 25);
            batch.draw(digits[pontuacaoDigits[0]], larguraDispositivo / 2 - digits[pontuacaoDigits[1]].getWidth() / 2 - 50,
                    (alturaDispositivo - digits[pontuacaoDigits[1]].getHeight()) - 25);
        } else if (pontuacaoDigits.length == 3) {

            batch.draw(digits[pontuacaoDigits[2]], larguraDispositivo / 2 - digits[pontuacaoDigits[0]].getWidth() + 100,
                    (alturaDispositivo - digits[pontuacaoDigits[2]].getHeight()) - 25);
            batch.draw(digits[pontuacaoDigits[1]], larguraDispositivo / 2 - digits[pontuacaoDigits[0]].getWidth() + 50,
                    (alturaDispositivo - digits[pontuacaoDigits[2]].getHeight()) - 25);
            batch.draw(digits[pontuacaoDigits[0]], larguraDispositivo / 2 - digits[pontuacaoDigits[0]].getWidth(),
                    (alturaDispositivo - digits[0].getHeight()) - 25);
        }
    }

    void Pontuacao_Final() {

        pontuacaoDigits = getDigits(pontuacao);

        if (pontuacaoDigits.length == 1) {
            stage.getBatch().draw(digitsSmall[pontuacaoDigits[0]],
                    larguraDispositivo / 2 + (endScore.getWidth() / 2) - 50 - digitsSmall[pontuacaoDigits[0]].getWidth(),
                    alturaDispositivo / 2 + digitsSmall[pontuacaoDigits[0]].getHeight() / 2);


        } else if (pontuacaoDigits.length == 2) {
            batch.draw(digitsSmall[pontuacaoDigits[1]], larguraDispositivo / 2 - digitsSmall[pontuacaoDigits[0]].getWidth() / 2,
                    (alturaDispositivo - digitsSmall[pontuacaoDigits[0]].getHeight()) - 25);
            batch.draw(digitsSmall[pontuacaoDigits[0]], larguraDispositivo / 2 - digitsSmall[pontuacaoDigits[1]].getWidth() / 2 - 50,
                    (alturaDispositivo - digitsSmall[pontuacaoDigits[1]].getHeight()) - 25);
        } else if (pontuacaoDigits.length == 3) {

            batch.draw(digitsSmall[pontuacaoDigits[2]], larguraDispositivo / 2 - digitsSmall[pontuacaoDigits[0]].getWidth() + 100,
                    (alturaDispositivo - digitsSmall[pontuacaoDigits[2]].getHeight()) - 25);
            batch.draw(digitsSmall[pontuacaoDigits[1]], larguraDispositivo / 2 - digitsSmall[pontuacaoDigits[0]].getWidth() + 50,
                    (alturaDispositivo - digitsSmall[pontuacaoDigits[2]].getHeight()) - 25);
            batch.draw(digitsSmall[pontuacaoDigits[0]], larguraDispositivo / 2 - digitsSmall[pontuacaoDigits[0]].getWidth(),
                    (alturaDispositivo - digitsSmall[0].getHeight()) - 25);
        }
    }

    //endregion
}
