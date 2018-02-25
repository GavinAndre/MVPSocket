/**
 * Created by gavinandre on 18-2-24.
 */
public interface ResponseCallback {

    void targetIsOffline();

    void targetIsOnline(String clientIp);
}
