package com.nomad.flappydoge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyDoge extends ApplicationAdapter {


    //region Objetos

    SpriteBatch batch;
    Texture[] doge, fundo;
    Texture canoBaixo, canoTopo, gameOver, hint, ready;
    Random numeroAleatorio;
    BitmapFont fonte, msgGameOver;
    Rectangle canoTopoCollision, canoBaixoCollision, dogeCollision;
    Circle propinaCollision;
    ShapeRenderer shape;

    //endregion

    //region Atributos
    float alturaDispositivo;
    float larguraDispositivo;
    int estadoJogo = 0;
    int pontuacao = 0;
    int nfundo;
    float variacao;
    float velocidadeQueda = 0;
    float posicaoInicialVertical;
    float posicaoMovimentoCanoHorizontal;
    float espacoEntreCanos;
    float deltaTime;
    float alturaEntreCanosAleatoria;
    boolean marcouPonto, propinaColetada;
    //endregion

    //region camera
    OrthographicCamera camera;
    Viewport viewport;
    final float VIRTUAL_WIDTH = 768, VIRTUAL_HEIGHT = 1024;

    //endregion


    @Override
    public void create() {
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
        posicaoInicialVertical = alturaDispositivo / 2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 250;

        /*Configuração da camera*/
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    }

    @Override
    public void render() {
        camera.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        deltaTime = Gdx.graphics.getDeltaTime();
        if (variacao > 2) variacao = 0;
        variacao += deltaTime * 5;

        if (estadoJogo == 0) {
            if (Gdx.input.justTouched())
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
            } else {
                if (Gdx.input.justTouched()) {
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    posicaoInicialVertical = alturaDispositivo / 2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                }
            }
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(fundo[nfundo], 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosAleatoria);
        batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosAleatoria);
        batch.draw(doge[(int) variacao], 150, posicaoInicialVertical);
        fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2 - 25, alturaDispositivo - 25);
        if (estadoJogo == 0) {
            batch.draw(hint, larguraDispositivo / 2 - hint.getWidth() / 2, alturaDispositivo / 2 - hint.getHeight() / 2);
        }

        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, (alturaDispositivo / 2 + 200) - gameOver.getHeight() / 2);
            msgGameOver.draw(batch, "Toque para Reiniciar!", larguraDispositivo / 2 - 200, (alturaDispositivo / 2 + 200) - gameOver.getHeight() / 2 - 100);
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

}
