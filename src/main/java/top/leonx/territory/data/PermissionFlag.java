package top.leonx.territory.data;

public class PermissionFlag {
    public final static PermissionFlag ENTER=new PermissionFlag(1,"enter");
    public final static PermissionFlag BREAK=new PermissionFlag(2,"break");
    public final static PermissionFlag PLACE =new PermissionFlag(4,"place");
    public final static PermissionFlag[] basicFlag={
            ENTER,
            BREAK,
            PLACE
    };
    private int code;
    private String name;
    public PermissionFlag(int i,String name) {
        this.code=i;
        this.name=name;
    }
    public PermissionFlag(int i) {
        this(i,"");
    }

    public PermissionFlag() {
        this(0);
    }

    public int getCode(){return code;}
    public String getName(){return name;}
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
