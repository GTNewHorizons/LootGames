package eu.usrv.legacylootgames.chess.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class TEChessMasterBlock extends TileEntity {
    public void onBlockClickedByPlayer(Object object, EntityPlayer pPlayer) {

    }

    public enum eGameStage {
        UNDEPLOYED, SLEEP, ACTIVE_WAIT_FOR_AI, ACTIVE_WAIT_FOR_PLAYER, PENDING_GAME_START
    }

}
