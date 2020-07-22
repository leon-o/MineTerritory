package top.leonx.territory.data;

public class PermissionFlag {
    public final static PermissionFlag ENTER=new PermissionFlag(1);
    public final static PermissionFlag BREAK=new PermissionFlag(2);
    public final static PermissionFlag INTERACTE=new PermissionFlag(4);

    private int code;
    public PermissionFlag(int i) {
        this.code=i;
    }

    public PermissionFlag() {
        this.code=0;
    }

    public int getCode(){return code;}
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
