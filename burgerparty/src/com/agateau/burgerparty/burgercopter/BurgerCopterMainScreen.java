package com.agateau.burgerparty.burgercopter;

import java.util.HashSet;

import com.agateau.burgerparty.utils.MaskedDrawable;
import com.agateau.burgerparty.utils.MaskedDrawableAtlas;
import com.agateau.burgerparty.utils.Signal2;
import com.agateau.burgerparty.utils.SpriteImage;
import com.agateau.burgerparty.utils.StageScreen;
import com.agateau.burgerparty.utils.Tile;
import com.agateau.burgerparty.utils.TileActor;
import com.agateau.burgerparty.utils.TileMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

public class BurgerCopterMainScreen extends StageScreen {
	static final float PIXEL_PER_SECOND = 180;
	static final float METERS_PER_SECOND = 4;
	static final int TILE_SIZE = 32;
	static final int GROUND_TILE_WIDTH = 128;
	static final int GROUND_TILE_HEIGHT = 64;
	static final int ENEMY_COUNT = 4;
	public BurgerCopterMainScreen(BurgerCopterMiniGame miniGame) {
		super(miniGame.getAssets().getSkin());
		mMiniGame = miniGame;
		createPools();
		createSky();
		createBg();
		createGround();
		createPlayer();
		createHud();
	}

	@Override
	public void dispose() {
		Gdx.app.log("BurgerCopterMiniGame.dispose" ,"");
		for (Disposable obj: mDisposables) {
			obj.dispose();
		}
	}

	@Override
	public void onBackPressed() {
		mMiniGame.showStartScreen();
	}

	private FPSLogger mLogger = new FPSLogger();
	private boolean mFrozen = false;
	@Override
	public void render(float delta) {
		if (!mFrozen) {
			getStage().act(delta);
		}
		mMeters += METERS_PER_SECOND * delta;
		for(SpriteImage enemy: mEnemies) {
			if (SpriteImage.collide(mPlayer.getActor(), enemy)) {
				mMiniGame.showGameOverScreen();
			}
		}
		updateHud();
		getStage().draw();
		mLogger.log();
	}

	private void createPlayer() {
		mPlayer = new Player(mMiniGame.getAssets(), mGroundActor);
		mPlayer.getActor().setPosition(10, getStage().getHeight() * 3 / 4);
		getStage().addActor(mPlayer.getActor());
	}

	private void createSky() {
		TextureRegion region = mMiniGame.getAssets().getTextureAtlas().findRegion("ui/white-pixel");
		Image bg = new Image(region);
		bg.setColor(0.8f, 0.95f, 1, 1);
		setBackgroundActor(bg);
	}

	private void createBg() {
		TextureRegion bg1Region = mMiniGame.getAssets().getTextureAtlas().findRegion("burgercopter/bg1");
		assert(bg1Region != null);
		Tile bg1 = new Tile(bg1Region);
		TextureRegion bg2Region = mMiniGame.getAssets().getTextureAtlas().findRegion("burgercopter/bg2");
		assert(bg2Region != null);
		Tile bg2 = new Tile(bg2Region);
		TileMap map = new TileMap(5, 1, bg1Region.getRegionWidth());

		for (int col = 0; col < map.getColumnCount(); ++col) {
			map.getColumn(col).set(0, MathUtils.randomBoolean() ? bg1 : bg2);
		}

		TileActor actor;
		actor = new TileActor(map, PIXEL_PER_SECOND / 4);
		actor.setBounds(0, TILE_SIZE * 2, getStage().getWidth(), bg1Region.getRegionWidth());
		actor.setColor(1, 1, 1, 0.5f);
		getStage().addActor(actor);
		mDisposables.add(actor);

		map = new TileMap(7, 1, bg1Region.getRegionWidth());
		for (int col = 0; col < map.getColumnCount(); ++col) {
			map.getColumn(col).set(0, MathUtils.randomBoolean() ? bg1 : bg2);
		}

		actor = new TileActor(map, PIXEL_PER_SECOND / 2);
		actor.setBounds(0, TILE_SIZE - 1, getStage().getWidth(), map.getTileHeight());
		getStage().addActor(actor);
		mDisposables.add(actor);
	}

	private void createGround() {
		int rowCount = 6;
		int columnCount = MathUtils.ceil(getStage().getWidth() / GROUND_TILE_WIDTH) * 2;
		GroundTileMap map = new GroundTileMap(mMiniGame.getAssets().getTextureAtlas(), columnCount, rowCount, GROUND_TILE_WIDTH, GROUND_TILE_HEIGHT);
		mGroundActor = new TileActor(map, PIXEL_PER_SECOND);
		mGroundActor.setBounds(0, 0, getStage().getWidth(), GROUND_TILE_HEIGHT * rowCount);
		mDisposables.add(mGroundActor);
		getStage().addActor(mGroundActor);

		map.groundEnemyRequested.connect(mHandlers, new Signal2.Handler<Integer, Integer>() {
			@Override
			public void handle(Integer column, Integer row) {
				addEnemy(column, row, EnemyType.GROUND);
			}
		});
		map.flyingEnemyRequested.connect(mHandlers, new Signal2.Handler<Integer, Integer>() {
			@Override
			public void handle(Integer column, Integer row) {
				addEnemy(column, row, EnemyType.FLYING);
			}
		});
	}

	private enum EnemyType {
		GROUND,
		FLYING,
	}

	private class Enemy extends SpriteImage {
		public Enemy(MaskedDrawableAtlas atlas) {
			mAtlas = atlas;
		}

		@Override
		public void act(float delta) {
			float x = mGroundActor.xForCol(mCol);
			setPosition(
					x + (GROUND_TILE_WIDTH - getWidth()) / 2,
					mGroundActor.yForRow(mRow));
			if (getRight() < 0) {
				remove();
				mEnemies.removeValue(this, true);
				mGroundEnemyPool.free(this);
			}
		}
		public void init(int col, int row, EnemyType type) {
			setPosition(getStage().getWidth(), -12);
			mCol = col;
			mRow = row;
			MaskedDrawable md = null;
			switch (type) {
			case GROUND:
				md = mAtlas.get("mealitems/0/cheese-inventory");
				break;
			case FLYING:
				md = mAtlas.get("mealitems/0/fish-inventory");
				break;
			}
			setMaskedDrawable(md);
		}

		private int mCol;
		private int mRow;
		private MaskedDrawableAtlas mAtlas;
	}

	private void addEnemy(int col, int row, EnemyType type) {
		Enemy enemy = mGroundEnemyPool.obtain();
		getStage().addActor(enemy);
		enemy.init(col, row, type);
		mEnemies.add(enemy);
	}

	private void createPools() {
		TextureAtlas atlas = mMiniGame.getAssets().getTextureAtlas();
		mMaskedDrawableAtlas = new MaskedDrawableAtlas(atlas);
		mGroundEnemyPool = new Pool<Enemy>() {
			@Override
			protected Enemy newObject() {
				return new Enemy(mMaskedDrawableAtlas);
			}
		};
	}

	private void createHud() {
		mMeterLabel = new Label("0", mMiniGame.getAssets().getSkin(), "lock-star-text");
		getStage().addActor(mMeterLabel);
		mMeterLabel.setX(0);
		mMeterLabel.setY(getStage().getHeight() - mMeterLabel.getPrefHeight());

		mFuelLabel = new Label("0", mMiniGame.getAssets().getSkin(), "lock-star-text");
		getStage().addActor(mFuelLabel);
		mFuelLabel.setX(0);
		mFuelLabel.setY(mMeterLabel.getY() - mFuelLabel.getPrefHeight() + 10);
	}

	private void updateHud() {
		mMeterLabel.setText((int)mMeters + "m");

		float fuel = mPlayer.getJumpFuel();
		mHudStringBuilder.setLength(0);
		for (float f = 0f; f < 1.0f; f += 0.05f) {
			mHudStringBuilder.append(f < fuel ? "|" : ".");
		}
		mFuelLabel.setText(mHudStringBuilder.toString());
	}

	private MaskedDrawableAtlas mMaskedDrawableAtlas;
	private Pool<Enemy> mGroundEnemyPool;

	private StringBuilder mHudStringBuilder = new StringBuilder();
	private BurgerCopterMiniGame mMiniGame;
	private Player mPlayer;
	private TileActor mGroundActor;
	private Array<SpriteImage> mEnemies = new Array<SpriteImage>();
	private float mMeters = 0;
	private Label mMeterLabel;
	private Label mFuelLabel;
	private Array<Disposable> mDisposables = new Array<Disposable>();

	private HashSet<Object> mHandlers = new HashSet<Object>();
}
