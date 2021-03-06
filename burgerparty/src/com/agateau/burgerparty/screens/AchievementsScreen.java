package com.agateau.burgerparty.screens;

import com.agateau.burgerparty.BurgerPartyGame;
import com.agateau.burgerparty.model.Achievement;
import com.agateau.burgerparty.model.AchievementManager;
import com.agateau.burgerparty.utils.AnchorGroup;
import com.agateau.burgerparty.utils.FileUtils;
import com.agateau.burgerparty.utils.RefreshHelper;
import com.agateau.burgerparty.utils.UiUtils;
import com.agateau.burgerparty.view.AchievementView;
import com.agateau.burgerparty.view.BurgerPartyUiBuilder;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class AchievementsScreen extends BurgerPartyScreen {
    private Screen mReturnScreen;

    public AchievementsScreen(BurgerPartyGame game) {
        super(game);
        Image bgImage = new Image(getTextureAtlas().findRegion("ui/menu-bg"));
        setBackgroundActor(bgImage);
        setupWidgets();
        new RefreshHelper(getStage()) {
            @Override
            protected void refresh() {
                getGame().showStartScreen();
                dispose();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mReturnScreen == null) {
            getGame().showStartScreen();
            return;
        }
        getGame().setScreen(mReturnScreen);
        dispose();
    }

    private void setupWidgets() {
        BurgerPartyUiBuilder builder = new BurgerPartyUiBuilder(getGame().getAssets());
        builder.build(FileUtils.assets("screens/achievements.gdxui"));
        AnchorGroup root = builder.getActor("root");
        getStage().addActor(root);
        root.setFillParent(true);

        builder.<ImageButton>getActor("backButton").addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent Event, Actor actor) {
                onBackPressed();
            }
        });

        ScrollPane pane = builder.<ScrollPane>getActor("scrollPane");
        VerticalGroup group = new VerticalGroup();
        pane.setWidget(group);
        createAchievementViews(group);

        TextureRegion region = getGame().getAssets().getTextureAtlas().findRegion("ui/corner-" + getGame().getDifficulty().name);
        UiUtils.setImageRegion(builder.<Image>getActor("difficultyImage"), region);
    }

    private void createAchievementViews(VerticalGroup parent) {
        AchievementManager manager = getGame().getGameStats().manager;
        boolean first = true;
        for (Achievement achievement: manager.getAchievements()) {
            if (!achievement.isValidForDifficulty(getGame().getDifficulty())) {
                continue;
            }
            // VerticalGroup spacing is buggy and adds spacing on top of the first element.
            // We create spaces manually to work-around this.
            if (first) {
                first = false;
            } else {
                Actor spacer = new Actor();
                spacer.setHeight(20);
                parent.addActor(spacer);
            }
            AchievementView view = new AchievementView(getGame().getAssets(), achievement);
            parent.addActor(view);
            if (achievement.isUnlocked() && !achievement.hasBeenSeen()) {
                achievement.markSeen();
            }
        }
    }

    public void setReturnScreen(Screen screen) {
        mReturnScreen = screen;
    }
}
