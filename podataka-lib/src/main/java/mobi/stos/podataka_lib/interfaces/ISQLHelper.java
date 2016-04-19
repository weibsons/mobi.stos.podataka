package mobi.stos.podataka_lib.interfaces;

import java.io.Serializable;

public interface ISQLHelper<T extends Serializable> {

    String table();

}
