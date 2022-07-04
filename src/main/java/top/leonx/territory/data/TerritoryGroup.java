package top.leonx.territory.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerritoryGroup {
    public static final String GROUP_ID = "gp_id";
    public static final String SINGLE_PLAYER = "si_p";
    public static final String SINGLE_PLAYER_ID = "si_pid";
    public static final String MULTI_PLAYER_ID = "mu_pid";
    public int groupId;
    public List<UUID> players = null;
    public UUID player = Util.NIL_UUID;

    public void readFromNbt(NbtCompound tag){
        tag.putInt(GROUP_ID,groupId);
        boolean singlePlayer = tag.getBoolean(SINGLE_PLAYER);
        if (singlePlayer){
            player = tag.getUuid(SINGLE_PLAYER_ID);
        }else{
            NbtList list = tag.getList(MULTI_PLAYER_ID, NbtElement.INT_ARRAY_TYPE);
            players = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                players.set(i,NbtHelper.toUuid(list.get(i)));
            }
        }
    }

    public void writeToNbt(NbtCompound tag){
        groupId = tag.getInt(GROUP_ID);
        if(players==null){
            tag.putBoolean(SINGLE_PLAYER,true);
            tag.putUuid(SINGLE_PLAYER_ID,player);
        }else{
            tag.putBoolean(SINGLE_PLAYER,false);
            NbtList list= new NbtList();
            for (UUID uuid : players) {
                list.add(NbtHelper.fromUuid(uuid));
            }
            tag.put(MULTI_PLAYER_ID,list);
        }
    }
}
