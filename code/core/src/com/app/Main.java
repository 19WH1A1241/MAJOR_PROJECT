package com.app;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Class
public class Main extends ApplicationAdapter {
    // const
    static final float SCREEN_WIDTH = 1920; // default screen width
    static final float SCREEN_HEIGHT = 1440; // default screen height
    static final float PPM = 32; // pixels per meter in Box2D world
    final boolean SHOW_DEBUG = false; // show debug
    final float BRIGHTNESS_PRESSED = 0.9f; // button brightness when pressed
    final float BG_VOLUME = 0.2f; // background music volume
    final int TIME = 20; // time limit in seconds
    final float ANIMATION_TIME = 0.2f;// numbers animation time in seconds
    final float DELAY_SHOW = 1.5f;// numbers visible delay time in seconds
    final float DELAY_LEVEL = 1f;// time interval in seconds before next level

    // vars
    static Stage stage;
    static World world;
    static AssetManager assetManager;
    static InputListener controlListener;
    static float ratio;
    static Array<Body> destroyBodies;
    static Array<Joint> destroyJoints;
    OrthographicCamera cam;
    JsonValue map;
    float mapWidth;
    float mapHeight;
    boolean isSigned;
    Preferences pref;
    Box2DDebugRenderer debug;
    String screenColor;
    SpriteBatch batch;
    int currentWidth;
    int currentHeight;
    Viewport viewport;
    InterfaceListener nativePlatform;
    Music sndBg;
    float taskDelay;
    boolean gamePaused;
    Act btnSign;
    Act btnSound;
   // Act btnPause;
    Group groupPause;
    int score;
    Vector2 point;
    float currentVolume = 0;
    boolean isForeground = true;
    Task TIMER;
    String screen = ""; // screen

    Group groupGameOver;
    Act txtReady;
    Act progressOver;
    Act progressLine;
    Act progressBg;
    TextureAtlas numbers;
    TextureAtlas itemsTextures;
    int currentTime;
    Array<Act> items;
    int level;
    int numOpened;

    // Constructor
    public Main(InterfaceListener nativePlatform) {
        this.nativePlatform = nativePlatform;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        controlListener = new CONTROL();
        destroyBodies = new Array<Body>();
        destroyJoints = new Array<Joint>();
        currentWidth = Gdx.graphics.getWidth();
        currentHeight = Gdx.graphics.getHeight();
        assetManager = new AssetManager();
        Gdx.input.setCatchKey(Keys.BACK, true); // prevent back on mobile
        items = new Array<Act>();
        TIMER();

        // load assets
        Lib.loadAssets(false);

        // debug
        if (SHOW_DEBUG)
            debug = new Box2DDebugRenderer();

        // preferences
        pref = Gdx.app.getPreferences("preferences");

        // send score
        if (pref.contains("score"))
            nativePlatform.saveScore(pref.getInteger("score"));

        // camera & viewport
        cam = new OrthographicCamera(SCREEN_WIDTH / PPM, SCREEN_HEIGHT / PPM);
        viewport = new FillViewport(SCREEN_WIDTH, SCREEN_HEIGHT);

        // world
        world = new World(new Vector2(0, 0), true);

        // stage
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        // bg music
        sndBg = assetManager.get("sndBg.mp3", Music.class);
        bgSound();

        // numbers
        numbers = assetManager.get("number.atlas", TextureAtlas.class);

        // itemsTextures
        itemsTextures = assetManager.get("item.atlas", TextureAtlas.class);

        showScreen("main");
    }

    // showScreen
    void showScreen(String screen) {
        clearScreen();
        this.screen = screen;

        if (screen.equals("main")) { // MAIN
            // load screen
            map = new JsonReader().parse(Gdx.files.internal(screen + ".hmp"));

            // bg
            Lib.addLayer("bg", map, stage.getRoot());

            // menu buttons array
            Array<Act> buttons = new Array<Act>();

            // btnStart
            buttons.add(Lib.addLayer("btnStart", map, stage.getRoot()).first());

            // sign button
          //  btnSign = Lib.addLayer("btnSign", map, stage.getRoot()).first();
         //   buttons.add(btnSign);
          //  setSigned(isSigned);

            // sound buttons
            btnSound = Lib.addLayer("btnSound", map, stage.getRoot()).first();
            btnSound.tex = new TextureRegion(assetManager.get(pref.getBoolean("mute", false) ? "btnSound.png" : "btnMute.png",
                    Texture.class));
            buttons.add(btnSound);

            // btnLeaders
          //  buttons.add(Lib.addLayer("btnLeaders", map, stage.getRoot()).first());

            // btnQuit
         //   buttons.add(Lib.addLayer("btnQuit", map, stage.getRoot()).first());

            // buttons animation
            Vector2 point = stage
                    .screenToStageCoordinates(new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2));
            float animSpeed = 0.3f; // animation speed
            int n = 0;
            for (int i = 0; i < buttons.size; i++) {
                buttons.get(i).setAlpha(0);
                buttons.get(i).setRotation((float) (Math.random() * 360));
                buttons.get(i).setScale(0.5f);
                buttons.get(i)
                        .addAction(
                                Actions.sequence(Actions.moveTo(point.x - buttons.get(i).getWidth() / 2, point.y
                                        - buttons.get(i).getHeight() / 2), Actions.delay(n * animSpeed * 0.5f), Actions.parallel(
                                        Actions.alpha(1, animSpeed), Actions.rotateTo(0, animSpeed), Actions.scaleTo(1, 1,
                                                animSpeed), Actions.moveTo(buttons.get(i).getX(), buttons.get(i).getY(),
                                                animSpeed, Interpolation.swingOut))));
                if (i != 1 && i != 3)
                    n++;
            }
        } else if (screen.equals("game")) { // GAME
            // load screen
            map = new JsonReader().parse(Gdx.files.internal(screen + ".hmp"));

            // bg
            Lib.addLayer("bg", map, stage.getRoot());

            // effect
            Lib.addLayer("effect", map, stage.getRoot()).first().body.setAngularVelocity(-0.1f);

            // txtReady
            txtReady = Lib.addLayer("txtReady", map, stage.getRoot()).first();
            txtReady.addAction(Actions.sequence(Actions.delay(1), Actions.alpha(0, 0.2f), new Action() {
                @Override
                public boolean act(float delta) {
                    // show progress time
                    progressBg.addAction(Actions.alpha(1, 0.2f));
                    progressLine.addAction(Actions.alpha(1, 0.2f));
                    progressOver.addAction(Actions.alpha(1, 0.2f));

                    txtReady.setVisible(false);
                    showItems();
                    return true;
                }
            }));

            // progress
            progressBg = Lib.addLayer("progressBg", map, stage.getRoot()).first();
            progressLine = Lib.addLayer("progressLine", map, stage.getRoot()).first();
            progressOver = Lib.addLayer("progressOver", map, stage.getRoot()).first();
            progressBg.setAlpha(0);
            progressLine.setAlpha(0);
            progressOver.setAlpha(0);

            // btnPause
          //  btnPause = Lib.addLayer("btnPause", map, stage.getRoot()).first();

            // groupGameOver
            groupGameOver = Lib.addGroup("groupGameOver", map, stage.getRoot());

            // groupPause
            groupPause = Lib.addGroup("groupPause", map, stage.getRoot());
            btnSound = groupPause.findActor("btnSound");
            btnSound.tex = new TextureRegion(assetManager.get(pref.getBoolean("mute", false) ? "btnSound.png" : "btnMute.png",
                    Texture.class));
        }

        // map config
        mapWidth = map.getInt("map_width", 0);
        mapHeight = map.getInt("map_height", 0);
        screenColor = map.getString("map_color", null);

        // stage keyboard focus
        Act a = new Act("");
        stage.addActor(a);
        a.addListener(controlListener);
        stage.setKeyboardFocus(a);

        render();
    }

    // clearScreen
    void clearScreen() {
        screen = "";
        screenColor = null;
        gamePaused = false;
        score = 0;
        currentTime = 0;
        TIMER.cancel();
        level = 1;

        // clear world
        if (world != null) {
            world.clearForces();
            world.getJoints(destroyJoints);
            world.getBodies(destroyBodies);
        }
        render();

        // clear stage
        stage.clear();
    }

    @Override
    public void render() {
        // bg music volume
        if (!pref.getBoolean("mute", false) && isForeground && currentVolume < BG_VOLUME) {
            currentVolume += 0.001f;
            sndBg.setVolume(currentVolume);
        }

        // screen color
        if (screenColor != null) {
            Color color = Color.valueOf(screenColor);
            Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
        }

        // clear
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // current screen render
        if (screen.equals("game"))
            renderGame();
        else if (!screen.isEmpty()) {
            // world render
            world.step(1 / 30f, 8, 3);

            // camera position
            stage.getRoot().setPosition((SCREEN_WIDTH - mapWidth) * 0.5f, (SCREEN_HEIGHT - mapHeight) * 0.5f);
            cam.position.set((SCREEN_WIDTH * 0.5f - stage.getRoot().getX()) / PPM,
                    (SCREEN_HEIGHT * 0.5f - stage.getRoot().getY()) / PPM, 0);
            cam.update();

            // stage render
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 0.02f));
            stage.draw();
        }

        // destroy
        if (!world.isLocked()) {
            for (int i = 0; i < destroyJoints.size; i++)
                world.destroyJoint(destroyJoints.get(i));
            for (int i = 0; i < destroyBodies.size; i++)
                world.destroyBody(destroyBodies.get(i));
            destroyJoints.clear();
            destroyBodies.clear();
        }

        // debug render
        if (SHOW_DEBUG)
            debug.render(world, cam.combined);
    }

    // renderGame
    void renderGame() {
        if (!gamePaused) {
            // groups
            groupPause.setPosition(-stage.getRoot().getX(), -stage.getRoot().getY());
            groupGameOver.setPosition(-stage.getRoot().getX(), -stage.getRoot().getY());

            // btnPause
            point = stage.screenToStageCoordinates(new Vector2(Gdx.graphics.getWidth(), 0));
          //  btnPause.setPosition(point.x - btnPause.getWidth() - 20 - stage.getRoot().getX(), point.y - btnPause.getHeight() - 20
                 //   - stage.getRoot().getY());

            // render
            world.step(1 / 30f, 8, 3);
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 0.02f));

            // progress
            point = stage.screenToStageCoordinates(new Vector2(0, 0));
            progressBg.setPosition(point.x + 10 - stage.getRoot().getX(), point.y - progressBg.getHeight() - 10
                    - stage.getRoot().getY());
            progressOver.setPosition(progressBg.getX(), progressBg.getY());
            progressLine.setPosition(point.x + 17 - stage.getRoot().getX(), point.y - progressLine.getHeight() - 17
                    - stage.getRoot().getY());
            progressLine.setOrigin(0, 0);
            progressLine.setScaleX((float) currentTime / TIME);

            // camera position
            stage.getRoot().setPosition((SCREEN_WIDTH - mapWidth) * 0.5f, (SCREEN_HEIGHT - mapHeight) * 0.5f);
            cam.position.set((SCREEN_WIDTH * 0.5f - stage.getRoot().getX()) / PPM,
                    (SCREEN_HEIGHT * 0.5f - stage.getRoot().getY()) / PPM, 0);
            cam.update();
        }

        stage.draw();
    }

    @Override
    public void pause() {
        sndBg.pause();
        isForeground = false;
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
        isForeground = true;

        // finish load assets
        if (!assetManager.update())
            assetManager.finishLoading();

        bgSound();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        ratio = Math.max((float) viewport.getScreenWidth() / SCREEN_WIDTH, (float) viewport.getScreenHeight() / SCREEN_HEIGHT);

        if (!Gdx.graphics.isFullscreen()) {
            currentWidth = width;
            currentHeight = height;
        }
    }

    @Override
    public void dispose() {
        clearScreen();
        batch.dispose();
        stage.dispose();
        assetManager.clear();

        if (debug != null)
            debug.dispose();

        if (world != null)
            world.dispose();

        System.gc();
    }

    // setSigned
    public void setSigned(boolean signed) {
        isSigned = signed;
        btnSign.tex = new TextureRegion(assetManager.get(signed ? "btnSignOut.png" : "btnSignIn.png", Texture.class));
    }

    // saveScore
    public boolean saveScore(int score) {
        if (!pref.contains("score") || score > pref.getInteger("score")) {
            pref.putInteger("score", score);
            pref.flush();
            return true;
        }

        return false;
    }

    // bgSound
    void bgSound() {
        if (!pref.getBoolean("mute", false) && isForeground) {
            sndBg.setVolume(currentVolume);
            sndBg.setLooping(true);
            sndBg.play();
        }
    }

    // log
    void log(Object obj) {
        if (Gdx.app.getType().equals(ApplicationType.Desktop))
            System.out.println(obj);
        else
            Gdx.app.log("@", obj.toString());
    }

    // gameOver
    void gameOver() {
        // hide elements
   //     btnPause.enabled = false;
    //    btnPause.removeListener(controlListener);
     //   btnPause.addAction(Actions.alpha(0, 0.2f));
        progressBg.addAction(Actions.alpha(0, 0.2f));
        progressLine.addAction(Actions.alpha(0, 0.2f));
        progressOver.addAction(Actions.alpha(0, 0.2f));
        TIMER.cancel();

        // hide items
        for (int i = 0; i < items.size; i++) {
            items.get(i).enabled = false;
            items.get(i).addAction(Actions.alpha(0, ANIMATION_TIME));
        }

        // sound
        if (!pref.getBoolean("mute", false) && isForeground)
            assetManager.get("sndGameOver.mp3", Sound.class).play(1f);

        showGroup(groupGameOver);
    }

    // showGroup
    void showGroup(Group group) {

        float delay = 0; // delay before show group

        // add score numbers
       // String str = String.valueOf(score);
        String str="";
       // Log.d("The result isssssssss", v);
        nativePlatform.saveScore(score);
        System.out.println("Scoreeeeeeeeeee");
        System.out.println(str);

        Array<Act> actors = new Array<Act>();
        int numbersWidth = 0;
        for (int i = 0; i < str.length(); i++) {
            Act actor = new Act("", 0, 400, numbers.findRegion(str.substring(i, i + 1)));
            actors.add(actor);
            group.addActor(actor);
            numbersWidth += actor.getWidth();
        }

        // set numbers position
        float x_pos = (SCREEN_WIDTH - numbersWidth) / 2;
        for (int i = 0; i < actors.size; i++) {
            actors.get(i).setX(x_pos);
            x_pos += actors.get(i).getWidth();
        }

        // show
        group.setVisible(true);
        SnapshotArray<Actor> allActors = group.getChildren();
        for (int i = 0; i < allActors.size; i++) {
            allActors.get(i).setScale(0, 0);
            if (i != allActors.size - 1)
                allActors.get(i).addAction(
                        Actions.sequence(Actions.delay(delay + i * 0.2f), Actions.scaleBy(1, 1, 1, Interpolation.elasticOut)));
            else
                allActors.get(i).addAction(
                        Actions.sequence(Actions.delay(delay + i * 0.2f), Actions.scaleBy(1, 1, 1, Interpolation.elasticOut), new Action() {
                            @Override
                            public boolean act(float delta) {
                                // show AdMob Interstitial
                                nativePlatform.admobInterstitial();
                                return true;
                            }
                        }));
        }
    }

    // showItems+
    void showItems() {
        // remove items from stage
        for (int i = 0; i < items.size; i++)
            items.get(i).remove();

        items = Lib.addLayer("item", map, stage.getRoot());
        items.shuffle();

        // level
        int numNumbers;
        if (level >= 18)
            numNumbers = 7;
        else if (level >= 12)
            numNumbers = 6;
        else if (level >= 7)
            numNumbers = 5;
        else if (level >= 3)
            numNumbers = 4;
        else
            numNumbers = 3;

        // remove unneeded numbers
        for (int i = numNumbers; i < items.size; i++)
            items.get(i).remove();
        items.truncate(numNumbers);

        // random array
        List<Integer> rand_array = new ArrayList<Integer>();
        for (int i = 1; i <= 9; i++)
            rand_array.add(i);
        Collections.shuffle(rand_array);

        // make numbers
        for (int i = 0; i < items.size; i++) {
            items.get(i).enabled = false;
            items.get(i).num = rand_array.get(i);
            items.get(i).tex = itemsTextures.findRegion(String.valueOf(items.get(i).num));
            items.get(i).setAlpha(0);
            items.get(i).setZIndex(txtReady.getZIndex());

            if (i == items.size - 1)
                items.get(i).addAction(
                        Actions.sequence(Actions.alpha(1, ANIMATION_TIME), Actions.delay(DELAY_SHOW), new Action() {
                            @Override
                            public boolean act(float delta) {
                                // enabled items
                                for (int i = 0; i < items.size; i++) {
                                    items.get(i).enabled = true;
                                    items.get(i).tex = new TextureRegion(assetManager.get("itemEmpty.png", Texture.class));
                                }

                                numOpened = 0;
                                currentTime = TIME;
                                Timer.schedule(TIMER, 1, 1);
                                return true;
                            }
                        }));
            else
                items.get(i).addAction(Actions.alpha(1, ANIMATION_TIME));
        }
    }

    // hideItems
    void hideItems() {
        for (int i = 0; i < items.size; i++) {
            if (i == items.size - 1) // last item
                items.get(i).addAction(
                        Actions.sequence(Actions.alpha(0, ANIMATION_TIME), Actions.delay(DELAY_LEVEL), new Action() {
                            @Override
                            public boolean act(float delta) {
                                level++;
                                showItems();
                                return true;
                            }
                        }));
            else
                items.get(i).addAction(Actions.alpha(0, ANIMATION_TIME));
        }
    }

    // TIMER
    void TIMER() {
        TIMER = new Task() {
            @Override
            public void run() {
                currentTime--;

                if (currentTime == 0) {
                    // game over text
                    ((Act) groupGameOver.findActor("txtGameOver")).tex = new TextureRegion(Main.assetManager.get("txtTimeUp.png",
                            Texture.class));

                    gameOver();
                } else if (isForeground && currentTime <= 3 && !pref.getBoolean("mute", false))
                    assetManager.get("sndTime.mp3", Sound.class).play(0.3f);
            }
        };
    }

    // CONTROL
    class CONTROL extends InputListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (((Act) event.getTarget()).enabled) {
                // each button
                if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn")) {
                    ((Act) event.getTarget()).brightness = BRIGHTNESS_PRESSED;

                    // sound
                    if (!pref.getBoolean("mute", false) && isForeground)
                        assetManager.get("sndBtn.mp3", Sound.class).play(0.9f);
                }

                if (screen.equals("game") && !gamePaused & currentTime > 0) {
                    // item
                    if (event.getTarget().getName().equals("item")) {
                        // animation
                        event.getTarget().addAction(
                                Actions.sequence(Actions.scaleTo(0.9f, 0.9f, 0.1f), Actions.scaleTo(1, 1, 0.1f)));

                        // check item
                        boolean isTrue = true;
                        for (int i = 0; i < items.size; i++)
                            if (items.get(i).enabled && items.get(i).num < ((Act) event.getTarget()).num) {
                                isTrue = false;
                                break;
                            }

                        // disable item
                        ((Act) event.getTarget()).enabled = false;
                        ((Act) event.getTarget()).tex = itemsTextures.findRegion(String.valueOf(((Act) event.getTarget()).num));

                        if (isTrue) {
                            // true
                            numOpened++;
                            score += 5;

                            // save score
                            if (saveScore(score))
                                nativePlatform.saveScore(score);

                            System.out.println("Save scoreee isssss+555555");
                            System.out.println(score);


                            // sound
                            if (!pref.getBoolean("mute", false) && isForeground)
                                assetManager.get("sndYes.mp3", Sound.class).play(1f);

                            // all opened
                            if (numOpened == items.size) {
                                TIMER.cancel();
                                currentTime = 0;
                                hideItems();
                            }
                        } else {
                            // wrong
                            if (!pref.getBoolean("mute", false) && isForeground)
                                assetManager.get("sndNo.mp3", Sound.class).play(1f);

                            gameOver();
                        }

                        return true;
                    }
                }
            }

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            if (((Act) event.getTarget()).enabled) {
                // each button
                if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
                    ((Act) event.getTarget()).brightness = 1;

                // if actor in focus
                if (stage.hit(event.getStageX(), event.getStageY(), true) == event.getTarget()) {
                    // btnPause
                    if (event.getTarget().getName().equals("btnPause")) {
                        gamePaused = true;
                        groupPause.setVisible(true);
                      //  btnPause.setVisible(false);
                        taskDelay = (TIMER.getExecuteTimeMillis() - TimeUtils.nanosToMillis(TimeUtils.nanoTime())) / 1000f;
                        TIMER.cancel();
                        return;
                    }

                    // btnSignIn
                    if (event.getTarget().getName().equals("btnSign")) {
                        if (isSigned)
                            nativePlatform.signOut();
                        else
                            nativePlatform.signIn();
                        return;
                    }

                    // btnLeaders
                    if (event.getTarget().getName().equals("btnLeaders")) {
                        nativePlatform.showLeaders();
                        return;
                    }

                    // btnStart
                    if (event.getTarget().getName().equals("btnStart")) {
                        showScreen("game");
                        return;
                    }

                    // btnSound
                    if (event.getTarget().getName().equals("btnSound")) {
                        if (pref.getBoolean("mute", false)) {
                            // sound
                            pref.putBoolean("mute", false);
                            pref.flush();
                            btnSound.tex = new TextureRegion(assetManager.get("btnMute.png", Texture.class));
                            bgSound();
                        } else {
                            // mute
                            pref.putBoolean("mute", true);
                            pref.flush();
                            btnSound.tex = new TextureRegion(assetManager.get("btnSound.png", Texture.class));
                            sndBg.pause();
                            currentVolume = 0;
                        }
                        return;
                    }

                    // btnQuit, btnBack
                    if (event.getTarget().getName().equals("btnQuit") || event.getTarget().getName().equals("btnBack")) {
                         System.out.println("Back button click");
                       nativePlatform.move();
                       // System.exit(0);
                       /* if (screen.equals("main"))
                            Gdx.app.exit();
                        else if (screen.equals("game"))
                            showScreen("main");
                        return;

                        */
                    }

                    // btnResume
                    if (event.getTarget().getName().equals("btnResume")) {
                        gamePaused = false;
                        groupPause.setVisible(false);
                       // btnPause.setVisible(true);
                        if (currentTime > 0)
                            Timer.schedule(TIMER, taskDelay, 1);
                        return;
                    }

                    // btnRestart
                 /*   if (event.getTarget().getName().equals("btnRestart")) {
                        showScreen("game");
                        return;
                    }

                  */
                }
            }
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);

            // mouse over button
            if (((Act) event.getTarget()).enabled
                    && event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
                ((Act) event.getTarget()).brightness = BRIGHTNESS_PRESSED;
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);

            // mouse out button
            if (event.getTarget().getName().substring(0, Math.min(3, event.getTarget().getName().length())).equals("btn"))
                ((Act) event.getTarget()).brightness = 1;
        }

        @Override
        public boolean keyUp(InputEvent event, int keycode) {
            switch (keycode) {
                case Keys.ESCAPE: // exit from fullscreen mode
                    if (Gdx.graphics.isFullscreen())
                        Gdx.graphics.setWindowedMode(currentWidth, currentHeight);
                    break;
                case Keys.ENTER: // switch to fullscreen mode
                    if (!Gdx.graphics.isFullscreen())
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    break;
                case Keys.BACK: // back
                    System.out.println("Back Press click");
                    nativePlatform.move();
                 //   System.exit(0);
                 /*   if (screen.equals("main"))
                      //  Gdx.app.exit();
                     //  nativePlatform.move();
                       // System.exit(0);

                    else if (screen.equals("game"))
                      //  showScreen("main");

                  */
                    break;
            }

            return true;
        }
    }

}