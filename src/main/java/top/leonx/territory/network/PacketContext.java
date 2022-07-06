package top.leonx.territory.network;

public abstract class PacketContext<T>{

    public abstract void sendPacket(T msg);
    public abstract void setHandled(boolean handled);
}