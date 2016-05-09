package mobi.stos.podataka.test.dao;

import android.content.Context;

import mobi.stos.podataka.test.bean.Carro;
import mobi.stos.podataka_lib.repository.AbstractRepository;

/**
 * Created by links_000 on 19/04/2016.
 */
public class CarroDao extends AbstractRepository<Carro> {

    public CarroDao(Context context) {
        super(context, Carro.class);
    }
}
