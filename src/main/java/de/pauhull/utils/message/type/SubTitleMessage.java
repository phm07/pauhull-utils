package de.pauhull.utils.message.type;

import de.pauhull.utils.message.NMSClasses;
import de.pauhull.utils.misc.MinecraftVersion;
import lombok.Getter;
import org.bukkit.entity.Player;

import static de.pauhull.utils.misc.MinecraftVersion.v1_11;

/**
 * Sends sub title message (lower text)
 * @author pauhull
 * @version 1.0
 */
public class SubTitleMessage implements MessageType {

    @Getter
    private String subTitle;

    @Getter
    private int fadeIn, stay, fadeOut;

    /**
     * Creates new SubTitleMessage from parameters
     * @param subTitle The title (lower text)
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public SubTitleMessage(String subTitle, int fadeIn, int stay, int fadeOut) {
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    /**
     * Sends subtitle to player
     * @param player The player
     */
    @Override
    public void send(Player player) {
        if(MinecraftVersion.CURRENT_VERSION.isGreaterOrEquals(v1_11)) {
            player.sendTitle(null, subTitle, fadeIn, stay, fadeOut);
        } else {
            NMSClasses.sendTitlesNMS(player, null, subTitle, fadeIn, stay, fadeOut);
        }
    }

}