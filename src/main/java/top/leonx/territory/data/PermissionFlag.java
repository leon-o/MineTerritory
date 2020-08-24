package top.leonx.territory.data;

public class PermissionFlag {
    public enum Type{
        PLAYER,
        ALL,
    }
    public final static PermissionFlag ENTER=new PermissionFlag(1,"enter");
    public final static PermissionFlag ATTACK_ENTITY=new PermissionFlag(1<<1,"attack_entity");
    public final static PermissionFlag BREAK_BLOCK=new PermissionFlag(1<<2,"break_block");
    public final static PermissionFlag PLACE_BLOCK=new PermissionFlag(1<<3,"place_block");
    public final static PermissionFlag USE_DOOR=new PermissionFlag(1<<4,"use_door");
    public final static PermissionFlag USE_CHEST=new PermissionFlag(1<<5,"use_chest");
    public final static PermissionFlag INTERACT_ENTITY=new PermissionFlag(1<<6,"interact_entity");
    public final static PermissionFlag USE_ITEM_ON_BLOCK=new PermissionFlag(1<<7,"use_item_on_block");
    public final static PermissionFlag CREATE_EXPLOSION=new PermissionFlag(1<<8,"create_explosion");
    public final static PermissionFlag FIRE_SPREAD=new PermissionFlag(1<<8,"fire_spread");
    public final static PermissionFlag MANAGE =new PermissionFlag(1<<31,"use_table");
    public final static PermissionFlag[] basicFlag={
            ENTER,
            ATTACK_ENTITY,
            BREAK_BLOCK,
            PLACE_BLOCK,
            USE_DOOR,
            USE_CHEST,
            INTERACT_ENTITY,
            USE_ITEM_ON_BLOCK,
            MANAGE
    };
    private int code;
    private final String name_key;
    public PermissionFlag(int i,String name_key) {
        this.code=i;
        this.name_key =name_key;
    }

    public PermissionFlag(int i) {
        this(i,"");
    }

    public PermissionFlag() {
        this(0);
    }

    public int getCode(){return code;}
    public String getTranslationKey(){return String.format("permission.territory.%s",name_key);}

    public String getNameKey() {
        return name_key;
    }

    public boolean contain(PermissionFlag flag)
    {
        return (code&flag.code)!=0;
    }
    @SuppressWarnings("unused")
    public static PermissionFlag combine(PermissionFlag ...flags)
    {
        int code=0;
        for (PermissionFlag flag : flags) {
            code |= flag.code;
        }
        return new PermissionFlag(code);
    }
    public void combine(PermissionFlag flag)
    {
        code|=flag.code;
    }
    public void remove(PermissionFlag flag)
    {
        code &= ~flag.code;
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj)
            return true;
        if(obj instanceof PermissionFlag)
        {
            return ((PermissionFlag)obj).getCode()==getCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return code;
    }
}
