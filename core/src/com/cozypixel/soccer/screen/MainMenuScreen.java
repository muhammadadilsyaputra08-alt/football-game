package com.cozypixel.soccer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cozypixel.soccer.PixelSoccerGame;
import com.cozypixel.soccer.util.SkinFactory;

/**
 * Menu utama — klub kecil cozy.
 * Menampilkan nama klub, koin, jumlah pemain, progress turnamen.
 */
public class MainMenuScreen extends ScreenAdapter {

    private final PixelSoccerGame game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(PixelSoccerGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(PixelSoccerGame.V_WIDTH, PixelSoccerGame.V_HEIGHT, camera);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        skin = SkinFactory.create();

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Header info
        String header = String.format("%s   |   %d koin   |   %d pemain",
                game.saveData.club.name,
                game.saveData.club.coins,
                game.playerTeam.ownedPlayers.size());
        Label headerLabel = new Label(header, skin);
        root.add(headerLabel).padTop(20).colspan(2).row();

        Label title = new Label("PIXEL SOCCER\nCOZY FOOTBALL", skin);
        title.setFontScale(1.5f);
        root.add(title).pad(30).colspan(2).row();

        // Tombol menu sesuai GDD
        addMenuButton(root, "Play / Training", () -> {
            // Untuk alpha: langsung ke training selection atau quick match
            game.setScreen(new TrainingScreen(game));
        });
        addMenuButton(root, "Tournament", () -> game.setScreen(new TournamentScreen(game)));
        addMenuButton(root, "Team", () -> game.setScreen(new TeamScreen(game)));
        addMenuButton(root, "Players", () -> game.setScreen(new PlayersScreen(game)));
        addMenuButton(root, "Shop", () -> game.setScreen(new ShopScreen(game)));
        addMenuButton(root, "Settings", () -> {
            // TODO
        });
    }

    private void addMenuButton(Table table, String text, Runnable action) {
        TextButton btn = new TextButton(text, skin);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        table.add(btn).width(200).height(40).pad(8).colspan(2).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.35f, 0.22f, 1f); // hijau rumput soft
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (skin != null) skin.dispose();
    }
}
