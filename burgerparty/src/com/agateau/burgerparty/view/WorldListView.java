package com.agateau.burgerparty.view;

import com.agateau.burgerparty.Assets;
import com.agateau.burgerparty.model.LevelWorld;
import com.agateau.burgerparty.utils.Anchor;
import com.agateau.burgerparty.utils.AnchorGroup;
import com.agateau.burgerparty.utils.HorizontalGroup;
import com.agateau.burgerparty.utils.Signal1;
import com.agateau.burgerparty.utils.UiUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class WorldListView extends HorizontalGroup {
	public Signal1<Integer> currentIndexChanged = new Signal1<Integer>();

	public enum Details {
		SHOW_STARS,
		HIDE_STARS
	}

	public WorldListView(Array<LevelWorld> worlds, int currentIndex, Assets assets, Details details) {
		mAssets = assets;
		mCurrentIndex = currentIndex;
		setSpacing(UiUtils.SPACING);

		int idx = 0;
		for (LevelWorld world: worlds) {
			Actor levelButton = createWorldButton(world, idx, details);
			addActor(levelButton);
			++idx;
		}
	}

	public void addActor(Actor actor) {
		super.addActor(actor);
		setWidth(getPrefWidth());
	}

	private static class WorldButton extends WorldBaseButton {
		public WorldButton(String text, String dirName, Assets assets) {
			super(text, dirName + "preview", assets);
		}
		public int mIndex;
	}

	private Actor createWorldButton(LevelWorld world, int index, Details details) {
		String text = String.valueOf(index + 1);
		if (index == mCurrentIndex) {
			text = "> " + text + " <";
		}
		WorldListView.WorldButton button = new WorldButton(text, world.getDirName(), mAssets);
		if (details == Details.SHOW_STARS) {
			Actor actor = createStarsActor(world);
			AnchorGroup group =	button.getGroup();
			group.addRule(actor, Anchor.BOTTOM_CENTER, group, Anchor.BOTTOM_CENTER, 0, 10);
		}
		button.mIndex = index;
		button.addListener(new ChangeListener() {
			public void changed(ChangeListener.ChangeEvent Event, Actor actor) {
				mAssets.getSoundAtlas().findSound("click").play();
				WorldListView.WorldButton button = (WorldListView.WorldButton)actor;
				currentIndexChanged.emit(button.mIndex);
			}
		});

		return button;
	}

	private Actor createStarsActor(LevelWorld world) {
		int wonStarCount = world.getWonStarCount();
		int totalStarCount = world.getTotalStarCount();
		
		String text = " " + wonStarCount + "/" + totalStarCount;
		Label label = new Label(text, mAssets.getSkin(), "world-button-text");

		Image image = new Image(mAssets.getTextureAtlas().findRegion("ui/star-on"));

		HorizontalGroup group = new HorizontalGroup();
		group.addActor(image);
		group.addActor(label);
		group.pack();
		return group;
	}

	private Assets mAssets;
	private int mCurrentIndex;
}