package top.leonx.territory.data;

import net.minecraft.nbt.NbtCompound;

public class Permission {
    private final PermissionFlag allow;
    private final PermissionFlag forbid;

    public static Permission readFromNbt(NbtCompound tag){
        return new Permission(tag.getInt("per_allow"),tag.getInt("per_forbid"));
    }

    public void writeToNbt(NbtCompound tag){
        tag.putInt("per_allow",this.allow.getCode());
        tag.putInt("per_forbid",this.forbid.getCode());
    }

    public Permission(int  allow, int forbid) {
        this.allow = new PermissionFlag(allow);
        this.forbid = new PermissionFlag(forbid);
    }

    public Permission() {
        forbid = PermissionFlag.NONE;
        allow = PermissionFlag.ALL;
    }

    public boolean hasPermission(PermissionFlag flag){
        return allow.contain(flag) && !forbid.contain(flag);
    }

    public void addAllow(PermissionFlag flag){
        allow.combine(flag);
    }

    public void addForbid(PermissionFlag flag){
        forbid.combine(flag);
    }

    public void removeAllow(PermissionFlag flag){
        allow.remove(flag);
    }

    public void removeForbid(PermissionFlag flag){
        forbid.remove(flag);
    }
    public PermissionFlag getAllow() {
        return allow;
    }

    public PermissionFlag getForbid() {
        return forbid;
    }
}
