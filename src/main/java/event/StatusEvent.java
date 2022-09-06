package event;
import java.util.EventObject;
/**
 * 每次我监听的对象（testListenerExecuter ）改变的时候，都会把他的改变传递给我（StatusEvent ），我在把改变传递给需要这个改变的地方（run类）。
 */
public class StatusEvent extends EventObject {

    private Object source;

    private boolean status;

    /**
     * 构造方法
     * @param source 监听的对象
     * @param status 监听的变量
     */
    public StatusEvent(Object source, boolean  status) {
        super(source);
        this.source=source;
        this.status=status;
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
