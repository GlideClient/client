package eu.shoroa.contrib.gui.cosmetics;

import eu.shoroa.contrib.cosmetic.CosmeticManager;
import eu.shoroa.contrib.gui.CompCosmetic;
import eu.shoroa.contrib.gui.CosmeticScene;
import me.eldodebug.soar.gui.modmenu.category.impl.CosmeticsCategory;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

import java.util.ArrayList;
import java.util.List;

public class OtherCosmeticsScene extends CosmeticScene {

    private List<CompCosmetic> compCosmetics  = new ArrayList<>();

    public OtherCosmeticsScene(CosmeticsCategory parent) {
        super(parent, TranslateText.OTHERS, TranslateText.OTHERS_DESCRIPTION, LegacyIcon.CAT);

        CosmeticManager.getInstance().getCosmetics().forEach(cosmetic -> {
            compCosmetics.add(new CompCosmetic(cosmetic));
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int columns = 5;

        float gap = 12f;
        float ofX = 0f;
        float ofY = 0f;

        for (CompCosmetic compCosmetic : compCosmetics) {
            compCosmetic.translate(getX() + ofX * (compCosmetic.getWidth() + 12f), getY() + ofY * (compCosmetic.getHeight() + 12f));
            compCosmetic.draw(mouseX, mouseY, partialTicks);
            ofX++;
            if (ofX > columns) {
                ofX = 0;
                ofY++;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        compCosmetics.forEach(compCosmetic -> {
            compCosmetic.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }
}
