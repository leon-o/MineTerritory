package top.leonx.territory.data;

public class PermissionFlag {
    public final static PermissionFlag ENTER=new PermissionFlag(1,"enter");
    public final static PermissionFlag LEFT_CLICK =new PermissionFlag(2,"left_click");
    public final static PermissionFlag RIGHT_CLICK =new PermissionFlag(4,"right_click");
    public final static PermissionFlag MANAGE =new PermissionFlag(8,"use_table");
    public final static PermissionFlag[] basicFlag={
            ENTER,
            LEFT_CLICK,
            RIGHT_CLICK,
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
    public boolean contain(PermissionFlag flag)
    {
        return (code&flag.code)!=0;
    }
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
}
