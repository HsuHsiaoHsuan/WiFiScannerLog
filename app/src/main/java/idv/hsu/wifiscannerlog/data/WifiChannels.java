package idv.hsu.wifiscannerlog.data;

import java.io.IOException;

// http://www.javaspecialists.co.za/archive/Issue113.html
public class WifiChannels<E extends Enum<E> & EnumConverter> {
    private final EnumReverseMap<E> reverse;

    public WifiChannels(Class<E> ec) {
        reverse = new EnumReverseMap<E>(ec);
    }

    public E getChannel(int frequency) throws IOException {
        return reverse.get(frequency);
    }
}
